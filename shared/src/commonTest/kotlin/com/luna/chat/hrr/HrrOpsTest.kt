package com.luna.chat.hrr

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class HrrOpsTest {

    @Test
    fun bindThenUnbindRecoversOriginalVector() {
        val rng = Mulberry32(42)
        val a = ComplexVector.randomUnitPhase(1024) { rng.next() }
        val b = ComplexVector.randomUnitPhase(1024) { rng.next() }

        val bound = HrrOps.bind(a, b)
        val recovered = HrrOps.unbind(bound, b)

        val sim = HrrOps.similarity(a, recovered)
        assertTrue(sim > 0.99, "Expected cosine sim > 0.99, got $sim")
    }

    @Test
    fun bindThenUnbindWithOtherKeyGivesLowSimilarity() {
        val rng = Mulberry32(123)
        val a = ComplexVector.randomUnitPhase(1024) { rng.next() }
        val b = ComplexVector.randomUnitPhase(1024) { rng.next() }
        val c = ComplexVector.randomUnitPhase(1024) { rng.next() }

        val bound = HrrOps.bind(a, b)
        val wrongUnbind = HrrOps.unbind(bound, c)

        val sim = HrrOps.similarity(a, wrongUnbind)
        assertTrue(abs(sim) < 0.2, "Expected low similarity with wrong key, got $sim")
    }

    @Test
    fun superposeAndUnbindRecoverWithReasonableConfidence() {
        val rng = Mulberry32(77)
        val keys = List(10) { ComplexVector.randomUnitPhase(1024) { rng.next() } }
        val values = List(10) { ComplexVector.randomUnitPhase(1024) { rng.next() } }

        // Bind each key-value pair
        val bindings = keys.zip(values).map { (k, v) -> HrrOps.bind(k, v) }
        val memory = HrrOps.superpose(bindings)

        // Unbind each key and check similarity to its value
        for (i in 0 until 10) {
            val recovered = HrrOps.unbind(memory, keys[i])
            val sim = HrrOps.similarity(recovered, values[i])
            assertTrue(sim > 0.15, "Fact $i: expected sim > 0.15 after superposition of 10, got $sim")
        }
    }

    @Test
    fun similarityOfIdenticalVectorsIsOne() {
        val rng = Mulberry32(99)
        val v = ComplexVector.randomUnitPhase(512) { rng.next() }
        val sim = HrrOps.similarity(v, v)
        assertTrue(abs(sim - 1.0) < 0.001, "Self-similarity should be ~1.0, got $sim")
    }

    @Test
    fun softmaxProducesValidProbabilityDistribution() {
        val scores = doubleArrayOf(1.0, 2.0, 3.0, 0.5)
        val probs = HrrOps.softmax(scores)
        val sum = probs.sum()
        assertTrue(abs(sum - 1.0) < 0.001, "Softmax should sum to 1.0, got $sum")
        assertTrue(probs.all { it > 0 }, "All probabilities should be positive")
        assertTrue(probs[2] > probs[1], "Highest score should have highest probability")
    }
}
