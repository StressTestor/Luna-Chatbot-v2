package com.luna.chat.hrr

import com.luna.chat.platform.SecureStorage

/**
 * Multi-topic nugget manager. Each topic (personal, interests, school, style, context)
 * gets its own [NuggetStore] with an independent HRR memory tensor.
 *
 * Persistence: each topic is serialized as JSON and stored in [SecureStorage]
 * under the key "nugget_{topic}". Vectors are never serialized — rebuilt on load.
 *
 * Recall can target a specific topic or broadcast across all topics and return
 * the highest-confidence match.
 */
class NuggetShelf(private val secureStorage: SecureStorage) {

    private val stores = mutableMapOf<String, NuggetStore>()

    /** Default topics. New topics can be created on the fly. */
    object Topics {
        const val PERSONAL = "personal"
        const val INTERESTS = "interests"
        const val SCHOOL = "school"
        const val STYLE = "style"
        const val CONTEXT = "context"

        val ALL = listOf(PERSONAL, INTERESTS, SCHOOL, STYLE, CONTEXT)
    }

    init {
        load()
    }

    // -- public API --

    /** Store a fact in the specified topic. Auto-saves. */
    fun remember(topic: String, key: String, value: String) {
        val store = stores.getOrPut(topic) { NuggetStore.create(topic) }
        store.remember(key, value)
        saveTopic(topic)
    }

    /** Recall from a specific topic. */
    fun recallByTopic(topic: String, key: String, sessionId: String = ""): NuggetStore.RecallResult? {
        return stores[topic]?.recall(key, sessionId)
    }

    /** Broadcast recall across all topics. Returns the highest-confidence match. */
    fun recall(key: String, sessionId: String = ""): NuggetStore.RecallResult? {
        var best: NuggetStore.RecallResult? = null
        for (store in stores.values) {
            val result = store.recall(key, sessionId) ?: continue
            if (best == null || result.confidence > best.confidence) {
                best = result
            }
        }
        // Auto-save to persist hit counter changes
        if (best != null) saveAll()
        return best
    }

    /** Remove a fact from a specific topic. */
    fun forget(topic: String, key: String) {
        stores[topic]?.forget(key)
        saveTopic(topic)
    }

    /** Get all facts across all topics with hits >= threshold (for system prompt injection). */
    fun getPromotedFacts(threshold: Int = HrrDefaults.PROMOTION_THRESHOLD): List<Pair<String, Fact>> {
        val promoted = mutableListOf<Pair<String, Fact>>()
        for ((topic, store) in stores) {
            for (fact in store.getPromotedFacts(threshold)) {
                promoted.add(topic to fact)
            }
        }
        return promoted
    }

    /** Get all facts across all topics (for the management UI). */
    fun getAllFacts(): Map<String, List<Fact>> {
        return stores.mapValues { (_, store) -> store.facts }
    }

    /** Number of total facts across all topics. */
    fun totalFactCount(): Int = stores.values.sumOf { it.facts.size }

    // -- persistence --

    /** Save a single topic to SecureStorage. */
    private fun saveTopic(topic: String) {
        val store = stores[topic] ?: return
        secureStorage.putString("nugget_$topic", store.toJson())
    }

    /** Save all topics. */
    fun saveAll() {
        for (topic in stores.keys) saveTopic(topic)
    }

    /** Load all known topics from SecureStorage. */
    private fun load() {
        for (topic in Topics.ALL) {
            val json = secureStorage.getString("nugget_$topic") ?: continue
            try {
                stores[topic] = NuggetStore.fromJson(json)
            } catch (_: Exception) {
                // Corrupted data — start fresh for this topic
            }
        }

        // Also scan for any non-default topics that were created dynamically
        val manifest = secureStorage.getString("nugget__manifest") ?: ""
        for (topic in manifest.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
            if (topic in stores) continue
            val json = secureStorage.getString("nugget_$topic") ?: continue
            try {
                stores[topic] = NuggetStore.fromJson(json)
            } catch (_: Exception) { }
        }
    }

    /** Update the manifest of known topic names. Called after adding a new topic. */
    private fun updateManifest() {
        secureStorage.putString("nugget__manifest", stores.keys.joinToString(","))
    }
}
