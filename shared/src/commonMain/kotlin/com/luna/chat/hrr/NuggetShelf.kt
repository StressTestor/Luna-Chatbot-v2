package com.luna.chat.hrr

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Multi-topic nugget manager. Each topic gets its own [NuggetStore].
 *
 * Thread safety: all public methods are `suspend` and protected by a [Mutex].
 * Concurrent coroutines (e.g., fact extraction on Dispatchers.IO vs. promoted
 * fact reads on Main) are serialized through the lock. The lock is non-blocking
 * (suspending Mutex, not a JVM ReentrantLock) so it won't block the main thread.
 *
 * Persistence: each topic is serialized as JSON and stored in [SecureStorage]
 * under the key "nugget_{topic}".
 */
class NuggetShelf(private val secureStorage: NuggetPersistence) {

    private val mutex = Mutex()
    private val stores = mutableMapOf<String, NuggetStore>()

    object Topics {
        const val PERSONAL = "personal"
        const val INTERESTS = "interests"
        const val SCHOOL = "school"
        const val STYLE = "style"
        const val CONTEXT = "context"

        val ALL = listOf(PERSONAL, INTERESTS, SCHOOL, STYLE, CONTEXT)
    }

    init {
        // Constructor runs before any concurrent access is possible (Koin
        // creates singletons sequentially), so no mutex needed here.
        load()
    }

    // -- public API (all suspend, all mutex-protected) --

    suspend fun remember(topic: String, key: String, value: String) = mutex.withLock {
        val isNew = topic !in stores
        val store = stores.getOrPut(topic) { NuggetStore.create(topic) }
        store.remember(key, value)
        saveTopic(topic)
        if (isNew) updateManifest()
    }

    suspend fun recallByTopic(topic: String, key: String, sessionId: String = ""): NuggetStore.RecallResult? =
        mutex.withLock {
            val result = stores[topic]?.recall(key, sessionId)
            if (result != null) saveTopic(topic)
            result
        }

    suspend fun recall(key: String, sessionId: String = ""): NuggetStore.RecallResult? =
        mutex.withLock {
            var best: NuggetStore.RecallResult? = null
            var bestTopic: String? = null
            for ((topic, store) in stores) {
                val result = store.recall(key, sessionId) ?: continue
                if (best == null || result.confidence > best.confidence) {
                    best = result
                    bestTopic = topic
                }
            }
            if (bestTopic != null) saveTopic(bestTopic)
            best
        }

    suspend fun forget(topic: String, key: String) = mutex.withLock {
        stores[topic]?.forget(key)
        saveTopic(topic)
    }

    suspend fun getPromotedFacts(threshold: Int = HrrDefaults.PROMOTION_THRESHOLD): List<Pair<String, Fact>> =
        mutex.withLock {
            val promoted = mutableListOf<Pair<String, Fact>>()
            for ((topic, store) in stores) {
                for (fact in store.getPromotedFacts(threshold)) {
                    promoted.add(topic to fact)
                }
            }
            promoted
        }

    suspend fun getAllFacts(): Map<String, List<Fact>> = mutex.withLock {
        stores.mapValues { (_, store) -> store.facts }
    }

    suspend fun totalFactCount(): Int = mutex.withLock {
        stores.values.sumOf { it.facts.size }
    }

    suspend fun saveAll() = mutex.withLock {
        for (topic in stores.keys) saveTopic(topic)
    }

    // -- persistence (called inside mutex, no additional locking) --

    private fun saveTopic(topic: String) {
        val store = stores[topic] ?: return
        secureStorage.putString("nugget_$topic", store.toJson())
    }

    private fun load() {
        for (topic in Topics.ALL) {
            val json = secureStorage.getString("nugget_$topic") ?: continue
            try {
                stores[topic] = NuggetStore.fromJson(json)
            } catch (_: Exception) { }
        }

        val manifest = secureStorage.getString("nugget__manifest") ?: ""
        for (topic in manifest.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
            if (topic in stores) continue
            val json = secureStorage.getString("nugget_$topic") ?: continue
            try {
                stores[topic] = NuggetStore.fromJson(json)
            } catch (_: Exception) { }
        }
    }

    private fun updateManifest() {
        secureStorage.putString("nugget__manifest", stores.keys.joinToString(","))
    }
}
