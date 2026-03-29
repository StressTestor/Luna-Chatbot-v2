package com.luna.chat.hrr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Mulberry32Test {

    @Test
    fun deterministicOutputForKnownSeed() {
        val rng1 = Mulberry32(42)
        val rng2 = Mulberry32(42)

        val seq1 = List(100) { rng1.next() }
        val seq2 = List(100) { rng2.next() }

        assertEquals(seq1, seq2, "Same seed must produce identical sequences")
    }

    @Test
    fun differentSeedsProduceDifferentSequences() {
        val rng1 = Mulberry32(1)
        val rng2 = Mulberry32(2)

        val v1 = rng1.next()
        val v2 = rng2.next()

        assertTrue(v1 != v2, "Different seeds should produce different first values")
    }

    @Test
    fun outputRangeIsZeroToOne() {
        val rng = Mulberry32(12345)
        repeat(1000) {
            val v = rng.next()
            assertTrue(v >= 0.0, "Value $v should be >= 0.0")
            assertTrue(v < 1.0, "Value $v should be < 1.0")
        }
    }

    @Test
    fun seedFromNameIsDeterministic() {
        val s1 = Mulberry32.seedFromName("interests")
        val s2 = Mulberry32.seedFromName("interests")
        assertEquals(s1, s2, "Same name must produce same seed")
    }

    @Test
    fun seedFromNameDiffersForDifferentNames() {
        val s1 = Mulberry32.seedFromName("personal")
        val s2 = Mulberry32.seedFromName("interests")
        assertTrue(s1 != s2, "Different names should produce different seeds")
    }
}
