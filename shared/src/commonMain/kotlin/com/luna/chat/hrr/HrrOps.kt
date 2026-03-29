package com.luna.chat.hrr

import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Core HRR (Holographic Reduced Representation) operations on complex vectors.
 *
 * In the Fourier HRR variant used here, binding is element-wise complex
 * multiplication and unbinding is multiplication by the complex conjugate.
 * This is equivalent to circular convolution/correlation in the spatial domain
 * but avoids the FFT entirely since we stay in the frequency domain.
 */
object HrrOps {

    /**
     * Bind two vectors via element-wise complex multiplication.
     *
     * Properties:
     * - Commutative: bind(A, B) == bind(B, A)
     * - Invertible: bind(bind(A, B), conj(B)) == A (for unit-magnitude keys)
     * - Result is dissimilar to both inputs
     */
    fun bind(a: ComplexVector, b: ComplexVector): ComplexVector {
        val d = a.dimension
        val re = DoubleArray(d)
        val im = DoubleArray(d)
        for (i in 0 until d) {
            re[i] = a.re[i] * b.re[i] - a.im[i] * b.im[i]
            im[i] = a.re[i] * b.im[i] + a.im[i] * b.re[i]
        }
        return ComplexVector(re, im)
    }

    /**
     * Unbind by multiplying [memory] by the conjugate of [key].
     *
     * For a single binding memory = bind(A, B):
     *   unbind(memory, B) recovers A exactly (for unit-magnitude B).
     *
     * For a superposition of N bindings:
     *   unbind recovers A + noise from the other N-1 bindings.
     */
    fun unbind(memory: ComplexVector, key: ComplexVector): ComplexVector {
        val d = memory.dimension
        val re = DoubleArray(d)
        val im = DoubleArray(d)
        for (i in 0 until d) {
            // Multiply by conj(key): conj(key).re = key.re, conj(key).im = -key.im
            re[i] = memory.re[i] * key.re[i] + memory.im[i] * key.im[i]
            im[i] = -memory.re[i] * key.im[i] + memory.im[i] * key.re[i]
        }
        return ComplexVector(re, im)
    }

    /**
     * Superpose [vectors] by element-wise summation, scaled by 1/sqrt(N).
     * Returns a new vector; inputs are not modified.
     */
    fun superpose(vectors: List<ComplexVector>): ComplexVector {
        if (vectors.isEmpty()) return ComplexVector.zero(0)
        val d = vectors[0].dimension
        val result = ComplexVector.zero(d)
        for (v in vectors) {
            result.addInPlace(v)
        }
        val scale = 1.0 / sqrt(vectors.size.toDouble())
        result.scaleInPlace(scale)
        return result
    }

    /**
     * Cosine similarity between two complex vectors.
     *
     * Converts each to a real 2D-dimensional vector by concatenating re and im,
     * unit-normalizes both, then computes dot product.
     *
     * Returns a value in [-1, 1]. Higher = more similar.
     */
    fun similarity(a: ComplexVector, b: ComplexVector): Double {
        val d = a.dimension
        // Compute norms
        var normA = 0.0
        var normB = 0.0
        var dot = 0.0
        for (i in 0 until d) {
            normA += a.re[i] * a.re[i] + a.im[i] * a.im[i]
            normB += b.re[i] * b.re[i] + b.im[i] * b.im[i]
            dot += a.re[i] * b.re[i] + a.im[i] * b.im[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom < 1e-12) 0.0 else dot / denom
    }

    /**
     * Convert complex vector to real 2D-dimensional vector (concat re + im),
     * then unit-normalize. Used for cosine similarity decoding.
     */
    fun toUnitReal(v: ComplexVector): DoubleArray {
        val d = v.dimension
        val real = DoubleArray(d * 2)
        var norm = 0.0
        for (i in 0 until d) {
            real[i] = v.re[i]
            real[i + d] = v.im[i]
            norm += v.re[i] * v.re[i] + v.im[i] * v.im[i]
        }
        norm = sqrt(norm)
        if (norm > 1e-12) {
            val invNorm = 1.0 / norm
            for (i in real.indices) real[i] *= invNorm
        }
        return real
    }

    /**
     * Dot product of two real vectors (same length).
     */
    fun dot(a: DoubleArray, b: DoubleArray): Double {
        var sum = 0.0
        for (i in a.indices) sum += a[i] * b[i]
        return sum
    }

    /**
     * Softmax with temperature. Returns probability distribution.
     */
    fun softmax(scores: DoubleArray, temperature: Double = 0.9): DoubleArray {
        val maxScore = scores.max()
        val exps = DoubleArray(scores.size) { exp((scores[it] - maxScore) / temperature) }
        val sum = exps.sum()
        for (i in exps.indices) exps[i] /= sum
        return exps
    }
}
