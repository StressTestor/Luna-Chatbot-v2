package com.luna.chat.hrr

import kotlin.math.sqrt

/**
 * A single-topic HRR memory store. Encodes key-value facts into fixed-size
 * complex vectors and retrieves them via cosine similarity decoding.
 *
 * Thread safety: all mutable state is accessed through copy-on-write semantics.
 * [data] is replaced atomically via reassignment. The HRR vector state lives in
 * an immutable [Snapshot] that is rebuilt from scratch and swapped atomically
 * via a @Volatile reference. No partial state is ever visible to readers.
 *
 * External callers should still synchronize at the [NuggetShelf] level via Mutex
 * to prevent interleaved read-modify-write sequences on the fact list.
 */
class NuggetStore private constructor(initialData: NuggetData) {

    private val D = initialData.dimension
    private val numBanks = initialData.banks

    /** The authoritative fact list + config. Replaced atomically on mutation. */
    @Volatile
    private var data: NuggetData = initialData

    /** Immutable HRR vector state. Rebuilt from [data] on fact mutations. */
    @Volatile
    private var snapshot: Snapshot = buildSnapshot(initialData)

    val name: String get() = data.name
    val facts: List<Fact> get() = data.facts

    // -- immutable snapshot holding all HRR vectors --

    private class Snapshot(
        val vocab: List<String>,
        val vocabIndex: Map<String, Int>,
        val bankMemory: List<ComplexVector>,
        val bankVocabKeys: List<List<ComplexVector>>,
        val bankVocabNorm: List<List<DoubleArray>>,
        val bankRoleKeys: List<List<ComplexVector>>,
        val bankSentKey: List<ComplexVector>,
    )

    // -- public API --

    data class RecallResult(
        val value: String,
        val confidence: Double,
        val margin: Double,
        val fact: Fact?,
    )

    /**
     * Store or update a fact. If [key] already exists, the value is updated.
     * Creates a new fact list and rebuilds the HRR snapshot.
     */
    fun remember(key: String, value: String) {
        val currentFacts = data.facts
        val newFacts = currentFacts.toMutableList()
        val existingIdx = newFacts.indexOfFirst { it.key.equals(key, ignoreCase = true) }
        if (existingIdx >= 0) {
            if (newFacts[existingIdx].value == value) return // no change
            newFacts.removeAt(existingIdx)
        }
        newFacts.add(Fact(key = key, value = value))
        val newData = data.copy(facts = newFacts.toList())
        data = newData
        snapshot = buildSnapshot(newData)
    }

    /**
     * Recall the value bound to [key].
     *
     * Uses three-phase tag matching (exact -> substring -> fuzzy) to find the
     * fact index, then decodes via HRR unbinding across all banks.
     *
     * Hit tracking uses copy-on-write: a new fact list is created with the
     * updated hit count. The HRR snapshot is NOT rebuilt (hits don't change vectors).
     */
    fun recall(key: String, sessionId: String = ""): RecallResult? {
        val snap = snapshot
        val currentFacts = data.facts
        val factIdx = resolveTag(key, currentFacts) ?: return null
        val fact = currentFacts[factIdx]

        // Track hits via copy-on-write (deduplicated per session)
        if (sessionId.isNotEmpty() && fact.lastSession != sessionId) {
            val updated = fact.copy(hits = fact.hits + 1, lastSession = sessionId)
            val newFacts = currentFacts.toMutableList()
            newFacts[factIdx] = updated
            data = data.copy(facts = newFacts.toList())
            // Don't rebuild snapshot — hit tracking doesn't change HRR vectors
        }

        // HRR decode
        val bankIdx = factIdx % numBanks
        val roleIdx = factIdx / numBanks

        if (bankIdx >= snap.bankSentKey.size ||
            roleIdx >= snap.bankRoleKeys.getOrNull(bankIdx)?.size ?: 0
        ) {
            return RecallResult(fact.value, 1.0, 1.0, fact)
        }

        val unbound = HrrOps.unbind(
            HrrOps.unbind(snap.bankMemory[bankIdx], snap.bankSentKey[bankIdx]),
            snap.bankRoleKeys[bankIdx][roleIdx],
        )

        val recReal = HrrOps.toUnitReal(unbound)
        val sims = DoubleArray(snap.vocab.size) { i ->
            HrrOps.dot(recReal, snap.bankVocabNorm[bankIdx][i])
        }

        val probs = HrrOps.softmax(sims, HrrDefaults.TEMPERATURE)
        val bestIdx = probs.indices.maxBy { probs[it] }
        val confidence = probs[bestIdx]

        val sorted = probs.sortedDescending()
        val margin = if (sorted.size >= 2) sorted[0] - sorted[1] else sorted[0]

        val decodedValue = snap.vocab[bestIdx]
        return RecallResult(
            value = if (decodedValue == fact.value) decodedValue else fact.value,
            confidence = confidence,
            margin = margin,
            fact = if (sessionId.isNotEmpty() && fact.lastSession != sessionId) {
                // Return the updated fact with incremented hits
                data.facts[factIdx]
            } else fact,
        )
    }

    /** Remove a fact by key. Creates new fact list and rebuilds snapshot. */
    fun forget(key: String) {
        val newFacts = data.facts.filter { !it.key.equals(key, ignoreCase = true) }
        val newData = data.copy(facts = newFacts)
        data = newData
        snapshot = buildSnapshot(newData)
    }

    /** Get all facts with hits >= [threshold]. */
    fun getPromotedFacts(threshold: Int = HrrDefaults.PROMOTION_THRESHOLD): List<Fact> =
        data.facts.filter { it.hits >= threshold }

    /** Serialize to JSON string (facts + config only, no vectors). */
    fun toJson(): String = kotlinx.serialization.json.Json.encodeToString(
        NuggetData.serializer(), data,
    )

    // -- snapshot builder --

    /**
     * Build an immutable HRR snapshot from [nuggetData]. All vectors are
     * deterministically generated from the topic name seed.
     */
    private fun buildSnapshot(nuggetData: NuggetData): Snapshot {
        val vocab = mutableListOf<String>()
        val vocabIndex = mutableMapOf<String, Int>()

        fun ensureVocab(word: String) {
            if (word !in vocabIndex) {
                vocabIndex[word] = vocab.size
                vocab.add(word)
            }
        }

        val baseSeed = Mulberry32.seedFromName(nuggetData.name)

        // Build vocabulary
        for (fact in nuggetData.facts) {
            ensureVocab(fact.key)
            ensureVocab(fact.value)
        }

        // Per-bank state
        val sentKeys = mutableListOf<ComplexVector>()
        val allVocabKeys = mutableListOf<List<ComplexVector>>()
        val allVocabNorm = mutableListOf<List<DoubleArray>>()
        val allRoleKeys = mutableListOf<List<ComplexVector>>()
        val memories = mutableListOf<ComplexVector>()

        for (b in 0 until numBanks) {
            val rng = Mulberry32(baseSeed + b * HrrDefaults.BANK_SEED_STRIDE)
            val keyGen = KeyGenerator(D, rng)
            sentKeys.add(keyGen.sentenceKey(0))

            // Vocab keys
            val vocabRng = Mulberry32(baseSeed + b * HrrDefaults.BANK_SEED_STRIDE + HrrDefaults.VOCAB_SEED_OFFSET)
            val vocabKeyGen = KeyGenerator(D, vocabRng)
            val rawKeys = List(vocab.size) { vocabKeyGen.nextVocabKey() }
            val orthKeys = vocabKeyGen.orthogonalize(rawKeys, iterations = HrrDefaults.ORTH_ITERATIONS)
            allVocabKeys.add(orthKeys)
            allVocabNorm.add(orthKeys.map { HrrOps.toUnitReal(it) })

            // Role keys
            val factsInBank = nuggetData.facts.indices.count { it % numBanks == b }
            val roleKeyGen = KeyGenerator(D, Mulberry32(0))
            allRoleKeys.add(List(factsInBank) { r -> roleKeyGen.roleKey(r) })

            // Memory: encode facts into bank
            val memory = ComplexVector.zero(D)
            val bindingCount = mutableListOf<ComplexVector>()
            for ((i, fact) in nuggetData.facts.withIndex()) {
                if (i % numBanks != b) continue
                val r = i / numBanks
                val valueIdx = vocabIndex[fact.value] ?: continue
                val binding = HrrOps.bind(
                    HrrOps.bind(sentKeys[b], allRoleKeys[b][r]),
                    allVocabKeys[b][valueIdx],
                )
                memory.addInPlace(binding)
                bindingCount.add(binding)
            }
            if (bindingCount.isNotEmpty()) {
                memory.scaleInPlace(1.0 / sqrt(bindingCount.size.toDouble()))
            }
            memories.add(memory)
        }

        return Snapshot(
            vocab = vocab.toList(),
            vocabIndex = vocabIndex.toMap(),
            bankMemory = memories.toList(),
            bankVocabKeys = allVocabKeys,
            bankVocabNorm = allVocabNorm,
            bankRoleKeys = allRoleKeys,
            bankSentKey = sentKeys.toList(),
        )
    }

    // -- tag resolution --

    private fun resolveTag(query: String, facts: List<Fact>): Int? {
        val q = query.lowercase()

        facts.forEachIndexed { i, fact ->
            if (fact.key.lowercase() == q) return i
        }
        facts.forEachIndexed { i, fact ->
            val tag = fact.key.lowercase()
            if (q in tag || tag in q) return i
        }
        facts.forEachIndexed { i, fact ->
            if (fuzzyRatio(q, fact.key.lowercase()) >= HrrDefaults.FUZZY_MATCH_THRESHOLD) return i
        }
        return null
    }

    private fun fuzzyRatio(a: String, b: String): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        var totalMatch = 0
        val aUsed = BooleanArray(a.length)
        val bUsed = BooleanArray(b.length)

        while (true) {
            var bestLen = 0
            var bestAi = 0
            var bestBi = 0
            for (ai in a.indices) {
                if (aUsed[ai]) continue
                for (bi in b.indices) {
                    if (bUsed[bi]) continue
                    var len = 0
                    while (ai + len < a.length && bi + len < b.length &&
                        !aUsed[ai + len] && !bUsed[bi + len] &&
                        a[ai + len] == b[bi + len]
                    ) len++
                    if (len > bestLen) {
                        bestLen = len; bestAi = ai; bestBi = bi
                    }
                }
            }
            if (bestLen == 0) break
            for (k in 0 until bestLen) { aUsed[bestAi + k] = true; bUsed[bestBi + k] = true }
            totalMatch += bestLen
        }

        return (2.0 * totalMatch) / (a.length + b.length)
    }

    companion object {
        fun fromJson(json: String): NuggetStore {
            val data = kotlinx.serialization.json.Json.decodeFromString(NuggetData.serializer(), json)
            return NuggetStore(data)
        }

        fun create(name: String, dimension: Int = HrrDefaults.DIMENSION, banks: Int = HrrDefaults.BANKS): NuggetStore {
            return NuggetStore(NuggetData(name = name, dimension = dimension, banks = banks))
        }
    }
}
