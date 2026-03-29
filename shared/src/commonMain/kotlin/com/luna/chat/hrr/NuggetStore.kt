package com.luna.chat.hrr

import kotlin.math.sqrt

/**
 * A single-topic HRR memory store. Encodes key-value facts into fixed-size
 * complex vectors and retrieves them via cosine similarity decoding.
 *
 * Architecture: [banks] independent memory tensors. Facts are distributed
 * round-robin across banks to reduce per-bank interference. All banks are
 * queried on recall and similarity scores are summed.
 *
 * Vectors are NEVER serialized. On load or after any mutation, [rebuild]
 * reconstructs the full memory from the fact list and deterministic PRNG.
 */
class NuggetStore(private val data: NuggetData) {

    private val D = data.dimension
    private val numBanks = data.banks

    // Vocabulary: maps string → index. Populated during rebuild.
    private val vocab = mutableListOf<String>()
    private val vocabIndex = mutableMapOf<String, Int>()

    // Per-bank state
    private val bankMemory = Array(numBanks) { ComplexVector.zero(D) }
    private val bankVocabKeys = mutableListOf<MutableList<ComplexVector>>()
    private val bankVocabNorm = mutableListOf<MutableList<DoubleArray>>()
    private val bankRoleKeys = mutableListOf<MutableList<ComplexVector>>()
    private val bankSentKey = mutableListOf<ComplexVector>()

    val name: String get() = data.name
    val facts: List<Fact> get() = data.facts

    init {
        rebuild()
    }

    // -- public API --

    data class RecallResult(
        val value: String,
        val confidence: Double,
        val margin: Double,
        val fact: Fact?,
    )

    /**
     * Store or update a fact. If [key] already exists, the value is updated.
     * Triggers a full rebuild (cheap at <100 facts).
     */
    fun remember(key: String, value: String) {
        val existing = data.facts.find { it.key.equals(key, ignoreCase = true) }
        if (existing != null) {
            if (existing.value == value) return // no change
            data.facts.remove(existing)
        }
        data.facts.add(Fact(key = key, value = value))
        rebuild()
    }

    /**
     * Recall the value bound to [key].
     *
     * Uses three-phase tag matching (exact → substring → fuzzy) to find the
     * fact index, then decodes via HRR unbinding across all banks.
     *
     * Returns null if no matching tag is found or confidence is too low.
     */
    fun recall(key: String, sessionId: String = ""): RecallResult? {
        val factIdx = resolveTag(key) ?: return null
        val fact = data.facts[factIdx]

        // Track hits (deduplicated per session)
        if (sessionId.isNotEmpty() && fact.lastSession != sessionId) {
            fact.hits++
            fact.lastSession = sessionId
        }

        // HRR decode across all banks
        val bankIdx = factIdx % numBanks
        val roleIdx = factIdx / numBanks

        if (bankIdx >= bankSentKey.size || roleIdx >= bankRoleKeys[bankIdx].size) {
            // Fallback: return the raw fact without HRR decode
            return RecallResult(fact.value, 1.0, 1.0, fact)
        }

        val unbound = HrrOps.unbind(
            HrrOps.unbind(bankMemory[bankIdx], bankSentKey[bankIdx]),
            bankRoleKeys[bankIdx][roleIdx],
        )

        // Cosine similarity against vocab
        val recReal = HrrOps.toUnitReal(unbound)
        val sims = DoubleArray(vocab.size) { i ->
            HrrOps.dot(recReal, bankVocabNorm[bankIdx][i])
        }

        val probs = HrrOps.softmax(sims, HrrDefaults.TEMPERATURE)
        val bestIdx = probs.indices.maxBy { probs[it] }
        val confidence = probs[bestIdx]

        // Margin: difference between top-1 and top-2
        val sorted = probs.sortedDescending()
        val margin = if (sorted.size >= 2) sorted[0] - sorted[1] else sorted[0]

        val decodedValue = vocab[bestIdx]
        // If HRR decode matches the stored fact, return it. Otherwise fall back to raw fact.
        return RecallResult(
            value = if (decodedValue == fact.value) decodedValue else fact.value,
            confidence = confidence,
            margin = margin,
            fact = fact,
        )
    }

    /** Remove a fact by key. Triggers rebuild. */
    fun forget(key: String) {
        data.facts.removeAll { it.key.equals(key, ignoreCase = true) }
        rebuild()
    }

    /** Get all facts with hits >= [threshold]. */
    fun getPromotedFacts(threshold: Int = HrrDefaults.PROMOTION_THRESHOLD): List<Fact> =
        data.facts.filter { it.hits >= threshold }

    /** Serialize to JSON string (facts + config only, no vectors). */
    fun toJson(): String = kotlinx.serialization.json.Json.encodeToString(
        NuggetData.serializer(), data,
    )

    // -- internals --

    /**
     * Rebuild all HRR vectors from the fact list and deterministic PRNG.
     *
     * This is the core insight: because the PRNG is seeded from the topic name,
     * the same facts always produce the same vectors. No need to serialize vectors.
     */
    fun rebuild() {
        // Reset state
        vocab.clear()
        vocabIndex.clear()
        bankVocabKeys.clear()
        bankVocabNorm.clear()
        bankRoleKeys.clear()
        bankSentKey.clear()

        // Create per-bank generators (each bank gets its own seed offset)
        val baseSeed = Mulberry32.seedFromName(data.name)

        for (b in 0 until numBanks) {
            val rng = Mulberry32(baseSeed + b * 31337)
            val keyGen = KeyGenerator(D, rng)

            bankSentKey.add(keyGen.sentenceKey(0))
            bankVocabKeys.add(mutableListOf())
            bankVocabNorm.add(mutableListOf())
            bankRoleKeys.add(mutableListOf())
        }

        // Build vocabulary from all fact keys and values
        for (fact in data.facts) {
            ensureVocab(fact.key)
            ensureVocab(fact.value)
        }

        // Generate vocab keys for each bank and orthogonalize
        for (b in 0 until numBanks) {
            val rng = Mulberry32(baseSeed + b * 31337 + 77777)
            val keyGen = KeyGenerator(D, rng)
            val rawKeys = List(vocab.size) { keyGen.nextVocabKey() }
            val orthKeys = keyGen.orthogonalize(rawKeys, iterations = HrrDefaults.ORTH_ITERATIONS)
            bankVocabKeys[b].addAll(orthKeys)
            bankVocabNorm[b].addAll(orthKeys.map { HrrOps.toUnitReal(it) })
        }

        // Generate role keys per bank (one per fact assigned to that bank)
        val factsPerBank = IntArray(numBanks)
        for (i in data.facts.indices) {
            factsPerBank[i % numBanks]++
        }
        for (b in 0 until numBanks) {
            for (r in 0 until factsPerBank[b]) {
                bankRoleKeys[b].add(KeyGenerator(D, Mulberry32(0)).roleKey(r))
            }
        }

        // Encode all facts into bank memories
        for (b in 0 until numBanks) {
            bankMemory[b] = ComplexVector.zero(D)
        }

        val bindings = mutableListOf<MutableList<ComplexVector>>()
        for (b in 0 until numBanks) bindings.add(mutableListOf())

        for ((i, fact) in data.facts.withIndex()) {
            val b = i % numBanks
            val r = i / numBanks
            val valueIdx = vocabIndex[fact.value] ?: continue

            val binding = HrrOps.bind(
                HrrOps.bind(bankSentKey[b], bankRoleKeys[b][r]),
                bankVocabKeys[b][valueIdx],
            )
            bindings[b].add(binding)
        }

        // Superpose bindings into each bank's memory
        for (b in 0 until numBanks) {
            if (bindings[b].isEmpty()) continue
            for (binding in bindings[b]) {
                bankMemory[b].addInPlace(binding)
            }
            val scale = 1.0 / sqrt(bindings[b].size.toDouble())
            bankMemory[b].scaleInPlace(scale)
        }
    }

    private fun ensureVocab(word: String) {
        if (word !in vocabIndex) {
            vocabIndex[word] = vocab.size
            vocab.add(word)
        }
    }

    /**
     * Three-phase tag resolution:
     * 1. Exact match (case-insensitive)
     * 2. Substring match (either direction)
     * 3. Fuzzy match (SequenceMatcher ratio >= 0.55)
     *
     * Returns the fact index or null.
     */
    private fun resolveTag(query: String): Int? {
        val q = query.lowercase()

        // Phase 1: exact
        data.facts.forEachIndexed { i, fact ->
            if (fact.key.lowercase() == q) return i
        }

        // Phase 2: substring
        data.facts.forEachIndexed { i, fact ->
            val tag = fact.key.lowercase()
            if (q in tag || tag in q) return i
        }

        // Phase 3: fuzzy
        data.facts.forEachIndexed { i, fact ->
            if (fuzzyRatio(q, fact.key.lowercase()) >= 0.55) return i
        }

        return null
    }

    /** Simple longest-common-substring based similarity ratio. */
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
                        bestLen = len
                        bestAi = ai
                        bestBi = bi
                    }
                }
            }
            if (bestLen == 0) break
            for (k in 0 until bestLen) {
                aUsed[bestAi + k] = true
                bUsed[bestBi + k] = true
            }
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
