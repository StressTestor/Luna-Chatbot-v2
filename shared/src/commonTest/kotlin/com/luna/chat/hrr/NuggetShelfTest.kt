package com.luna.chat.hrr

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** In-memory storage stub implementing the NuggetPersistence interface. */
class FakeNuggetPersistence : NuggetPersistence {
    private val store = mutableMapOf<String, String>()
    override fun getString(key: String): String? = store[key]
    override fun putString(key: String, value: String) { store[key] = value }
}

class NuggetShelfTest {

    @Test
    fun rememberAndRecallAcrossTopics() = runTest {
        val shelf = NuggetShelf(FakeNuggetPersistence())
        shelf.remember("interests", "favorite_color", "purple")
        shelf.remember("personal", "pet_name", "Mochi")

        val color = shelf.recallByTopic("interests", "favorite_color")
        assertNotNull(color)
        assertEquals("purple", color.value)

        val pet = shelf.recallByTopic("personal", "pet_name")
        assertNotNull(pet)
        assertEquals("Mochi", pet.value)
    }

    @Test
    fun broadcastRecallFindsAcrossTopics() = runTest {
        val shelf = NuggetShelf(FakeNuggetPersistence())
        shelf.remember("school", "math_grade", "A")

        val result = shelf.recall("math_grade")
        assertNotNull(result)
        assertEquals("A", result.value)
    }

    @Test
    fun persistenceRoundTrip() = runTest {
        val storage = FakeNuggetPersistence()

        val shelf1 = NuggetShelf(storage)
        shelf1.remember("interests", "sport", "soccer")
        shelf1.remember("personal", "name", "Luna")

        // New shelf instance with same storage — should load persisted facts
        val shelf2 = NuggetShelf(storage)
        val sport = shelf2.recallByTopic("interests", "sport")
        assertNotNull(sport, "Should persist and reload 'sport'")
        assertEquals("soccer", sport.value)

        val name = shelf2.recallByTopic("personal", "name")
        assertNotNull(name, "Should persist and reload 'name'")
        assertEquals("Luna", name.value)
    }

    @Test
    fun persistencePreservesHitCounts() = runTest {
        val storage = FakeNuggetPersistence()

        val shelf1 = NuggetShelf(storage)
        shelf1.remember("interests", "game", "minecraft")
        shelf1.recallByTopic("interests", "game", sessionId = "s1")
        shelf1.recallByTopic("interests", "game", sessionId = "s2")
        shelf1.saveAll()

        val shelf2 = NuggetShelf(storage)
        val facts = shelf2.getAllFacts()
        val gameFact = facts["interests"]?.find { it.key == "game" }
        assertNotNull(gameFact)
        assertEquals(2, gameFact.hits, "Hit count should survive persistence round-trip")
    }

    @Test
    fun concurrentRememberAndRecallDoesNotThrow() = runTest {
        val shelf = NuggetShelf(FakeNuggetPersistence())
        for (i in 1..5) shelf.remember("interests", "fact_$i", "value_$i")

        // Run concurrent remember + recall from multiple coroutines
        val jobs = (1..20).map { i ->
            async(Dispatchers.Default) {
                if (i % 2 == 0) {
                    shelf.remember("interests", "concurrent_$i", "val_$i")
                } else {
                    shelf.recall("fact_${(i % 5) + 1}")
                }
            }
        }

        // Should not throw ConcurrentModificationException or IndexOutOfBounds
        val results = jobs.awaitAll()
        assertTrue(results.isNotEmpty(), "All coroutines should complete")
    }

    @Test
    fun totalFactCountAcrossTopics() = runTest {
        val shelf = NuggetShelf(FakeNuggetPersistence())
        shelf.remember("personal", "a", "1")
        shelf.remember("interests", "b", "2")
        shelf.remember("school", "c", "3")

        assertEquals(3, shelf.totalFactCount())
    }

    @Test
    fun forgetRemovesAndPersists() = runTest {
        val storage = FakeNuggetPersistence()
        val shelf = NuggetShelf(storage)
        shelf.remember("personal", "temp", "data")
        shelf.forget("personal", "temp")

        assertEquals(0, shelf.totalFactCount())

        val shelf2 = NuggetShelf(storage)
        assertEquals(0, shelf2.totalFactCount())
    }
}
