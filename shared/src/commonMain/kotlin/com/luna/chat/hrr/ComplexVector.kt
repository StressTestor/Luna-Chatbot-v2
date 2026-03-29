package com.luna.chat.hrr

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A complex-valued vector stored as parallel real/imaginary arrays.
 * Every element lives on the unit circle (magnitude 1) for key vectors,
 * or at arbitrary magnitude for memory (superposition) vectors.
 *
 * Memory footprint: 2 * D * 8 bytes (two DoubleArrays).
 * At D=1024 that's 16 KB per vector.
 */
class ComplexVector(val re: DoubleArray, val im: DoubleArray) {

    val dimension: Int get() = re.size

    init {
        require(re.size == im.size) { "re and im must have the same length" }
    }

    /** Element-wise add [other] into this vector (mutating). */
    fun addInPlace(other: ComplexVector) {
        for (d in re.indices) {
            re[d] += other.re[d]
            im[d] += other.im[d]
        }
    }

    /** Scale every element by [factor] (mutating). */
    fun scaleInPlace(factor: Double) {
        for (d in re.indices) {
            re[d] *= factor
            im[d] *= factor
        }
    }

    companion object {
        /** Zero vector of dimension [d]. */
        fun zero(d: Int) = ComplexVector(DoubleArray(d), DoubleArray(d))

        /**
         * Random unit-phase vector: each element has magnitude 1,
         * phase sampled uniformly from [0, 2π) using the provided [rng].
         */
        fun randomUnitPhase(d: Int, rng: () -> Double): ComplexVector {
            val re = DoubleArray(d)
            val im = DoubleArray(d)
            val twoPi = 2.0 * kotlin.math.PI
            for (i in 0 until d) {
                val phase = rng() * twoPi
                re[i] = cos(phase)
                im[i] = sin(phase)
            }
            return ComplexVector(re, im)
        }
    }
}
