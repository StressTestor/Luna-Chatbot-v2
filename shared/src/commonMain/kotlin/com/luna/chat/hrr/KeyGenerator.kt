package com.luna.chat.hrr

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Generates deterministic HRR key vectors from a seeded PRNG.
 *
 * Key types:
 * - **Vocab keys**: random unit-phase, one per unique string in the vocabulary.
 *   Optionally orthogonalized to reduce cross-interference.
 * - **Role keys**: DFT basis vectors. Position k gets the k-th harmonic
 *   frequencies, creating ordered, related position identifiers.
 * - **Sentence keys**: single key at index 0 (all facts in one "sentence" per topic).
 *
 * All keys are deterministic from the topic name seed — the same name always
 * produces the same keys. This is why vectors don't need to be serialized.
 */
class KeyGenerator(
    val dimension: Int,
    private val rng: Mulberry32,
) {

    /** Generate a random unit-phase vocab key. */
    fun nextVocabKey(): ComplexVector = ComplexVector.randomUnitPhase(dimension) { rng.next() }

    /**
     * Generate role key at position [k] using the DFT basis.
     *
     * angle[d] = k * 2π * d / D
     * key[d] = cos(angle[d]) + i*sin(angle[d])
     *
     * Position 0 = all ones. Position 1 = base frequencies. Position k = k-th harmonics.
     */
    fun roleKey(k: Int): ComplexVector {
        val re = DoubleArray(dimension)
        val im = DoubleArray(dimension)
        val twoPiOverD = 2.0 * PI / dimension
        for (d in 0 until dimension) {
            val angle = k.toDouble() * twoPiOverD * d
            re[d] = cos(angle)
            im[d] = sin(angle)
        }
        return ComplexVector(re, im)
    }

    /** Sentence key at index [s]. Uses the DFT basis at a high offset to avoid collision with role keys. */
    fun sentenceKey(s: Int): ComplexVector {
        // Offset by a large prime to separate sentence key space from role key space
        return roleKey(s + 7919)
    }

    /**
     * Iterative orthogonalization of vocab keys to reduce cross-interference.
     *
     * Algorithm (from nuggets):
     * 1. Stack vocab keys as [V, 2D] real matrix (concat re+im)
     * 2. Compute Gram matrix G = K @ K^T, zero the diagonal
     * 3. K = K - (step * G @ K) / 2D
     * 4. Re-normalize each row, project back to unit-phase complex
     *
     * One iteration with step=0.4 is sufficient for <100 vocab items at D=1024.
     */
    fun orthogonalize(
        keys: List<ComplexVector>,
        iterations: Int = 1,
        step: Double = 0.4,
    ): List<ComplexVector> {
        if (keys.size <= 1) return keys
        val v = keys.size
        val realDim = dimension * 2

        // Convert to real matrix [V, 2D]
        val matrix = Array(v) { i ->
            val row = DoubleArray(realDim)
            for (d in 0 until dimension) {
                row[d] = keys[i].re[d]
                row[d + dimension] = keys[i].im[d]
            }
            row
        }

        repeat(iterations) {
            // Gram matrix G[i][j] = dot(row_i, row_j)
            val gram = Array(v) { i ->
                DoubleArray(v) { j ->
                    if (i == j) 0.0 // zero diagonal
                    else {
                        var dot = 0.0
                        for (k in 0 until realDim) dot += matrix[i][k] * matrix[j][k]
                        dot
                    }
                }
            }

            // K = K - (step * G @ K) / 2D
            val factor = step / realDim
            for (i in 0 until v) {
                for (k in 0 until realDim) {
                    var correction = 0.0
                    for (j in 0 until v) correction += gram[i][j] * matrix[j][k]
                    matrix[i][k] -= factor * correction
                }
            }

            // Re-normalize each row
            for (i in 0 until v) {
                var norm = 0.0
                for (k in 0 until realDim) norm += matrix[i][k] * matrix[i][k]
                norm = sqrt(norm)
                if (norm > 1e-12) {
                    val invNorm = 1.0 / norm
                    for (k in 0 until realDim) matrix[i][k] *= invNorm
                }
            }
        }

        // Project back to unit-phase complex vectors
        return matrix.map { row ->
            val re = DoubleArray(dimension)
            val im = DoubleArray(dimension)
            for (d in 0 until dimension) {
                val phase = atan2(row[d + dimension], row[d])
                re[d] = cos(phase)
                im[d] = sin(phase)
            }
            ComplexVector(re, im)
        }
    }
}
