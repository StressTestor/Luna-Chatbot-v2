package com.luna.chat.hrr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NuggetStoreTest {

    @Test
    fun rememberAndRecallSingleFact() {
        val store = NuggetStore.create("test_topic")
        store.remember("favorite_color", "blue")

        val result = store.recall("favorite_color")
        assertNotNull(result, "Should recall a stored fact")
        assertEquals("blue", result.value)
    }

    @Test
    fun rememberAndRecall20Facts() {
        val store = NuggetStore.create("recall_20")
        val facts = (1..20).map { "key_$it" to "value_$it" }
        for ((k, v) in facts) store.remember(k, v)

        assertEquals(20, store.facts.size, "Should have 20 facts stored")

        for ((k, v) in facts) {
            val result = store.recall(k)
            assertNotNull(result, "Should recall fact '$k'")
            assertEquals(v, result.value, "Recalled value for '$k' should be '$v'")
        }
    }

    @Test
    fun jsonRoundTripPreservesAllFacts() {
        val store = NuggetStore.create("roundtrip")
        for (i in 1..20) store.remember("key_$i", "value_$i")

        val json = store.toJson()
        val restored = NuggetStore.fromJson(json)

        assertEquals(store.facts.size, restored.facts.size, "Fact count should match")

        for (i in 1..20) {
            val result = restored.recall("key_$i")
            assertNotNull(result, "Should recall 'key_$i' after deserialization")
            assertEquals("value_$i", result.value)
        }
    }

    @Test
    fun rebuildProducesIdenticalRecallResults() {
        val store1 = NuggetStore.create("determinism")
        val store2 = NuggetStore.create("determinism")

        for (i in 1..10) {
            store1.remember("k$i", "v$i")
            store2.remember("k$i", "v$i")
        }

        for (i in 1..10) {
            val r1 = store1.recall("k$i")
            val r2 = store2.recall("k$i")
            assertNotNull(r1); assertNotNull(r2)
            assertEquals(r1.value, r2.value, "Same seed should produce same recall")
            assertEquals(r1.confidence, r2.confidence, "Confidence should match")
        }
    }

    @Test
    fun updateExistingFact() {
        val store = NuggetStore.create("update")
        store.remember("color", "blue")
        store.remember("color", "red")

        assertEquals(1, store.facts.size, "Updating should not duplicate facts")
        val result = store.recall("color")
        assertNotNull(result)
        assertEquals("red", result.value)
    }

    @Test
    fun forgetRemovesFact() {
        val store = NuggetStore.create("forget")
        store.remember("temp", "data")
        store.forget("temp")

        assertEquals(0, store.facts.size)
        assertNull(store.recall("temp"))
    }

    @Test
    fun hitTrackingUseCopyOnWrite() {
        val store = NuggetStore.create("hits")
        store.remember("fact", "value")

        // First recall with session — should increment hits
        val r1 = store.recall("fact", sessionId = "s1")
        assertNotNull(r1)
        assertEquals(1, store.facts[0].hits)

        // Same session — should NOT increment
        store.recall("fact", sessionId = "s1")
        assertEquals(1, store.facts[0].hits)

        // Different session — should increment
        store.recall("fact", sessionId = "s2")
        assertEquals(2, store.facts[0].hits)
    }

    @Test
    fun promotedFactsFilterByThreshold() {
        val store = NuggetStore.create("promo")
        store.remember("frequent", "yes")
        store.remember("rare", "no")

        // Recall 'frequent' 3 times from different sessions
        store.recall("frequent", sessionId = "s1")
        store.recall("frequent", sessionId = "s2")
        store.recall("frequent", sessionId = "s3")

        val promoted = store.getPromotedFacts(threshold = 3)
        assertEquals(1, promoted.size)
        assertEquals("frequent", promoted[0].key)
    }

    @Test
    fun fuzzyTagMatching() {
        val store = NuggetStore.create("fuzzy")
        store.remember("favorite_subject", "science")

        // Exact
        assertNotNull(store.recall("favorite_subject"))
        // Substring
        assertNotNull(store.recall("favorite"))
        // Case insensitive
        assertNotNull(store.recall("FAVORITE_SUBJECT"))
    }

    @Test
    fun recallNonExistentKeyReturnsNull() {
        val store = NuggetStore.create("empty")
        assertNull(store.recall("nonexistent"))
    }

    @Test
    fun immutabilityOfReturnedFactsList() {
        val store = NuggetStore.create("immutable")
        store.remember("a", "1")
        val factsBefore = store.facts
        store.remember("b", "2")
        val factsAfter = store.facts

        assertEquals(1, factsBefore.size, "Original list should be unchanged")
        assertEquals(2, factsAfter.size, "New list should have 2 facts")
    }
}
