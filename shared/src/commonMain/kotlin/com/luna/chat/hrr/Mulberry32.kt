package com.luna.chat.hrr

/**
 * Mulberry32 PRNG — exact port of the JavaScript implementation from NeoVertex1/nuggets.
 *
 * Deterministic: same seed always produces the same sequence. This is critical
 * because HRR vectors are never serialized — they're rebuilt from the seed on
 * every load. The seed is derived from the nugget topic name so the same name
 * always produces the same key vectors.
 *
 * Returns values in [0, 1).
 */
class Mulberry32(seed: Int) {

    private var state: Int = seed

    /** Next value in [0.0, 1.0). */
    fun next(): Double {
        state = (state + 0x6D2B79F5)
        var t = state
        t = imul(t xor (t ushr 15), 1 or t)
        t = (t + imul(t xor (t ushr 7), 61 or t)) xor t
        return ((t xor (t ushr 14)) ushr 0).toUInt().toDouble() / 4294967296.0
    }

    companion object {
        /**
         * Derive a 32-bit seed from a topic name string.
         * Exact port of seedFromName() from nuggets/core.ts:
         * takes the first 8 bytes of UTF-8, interprets bytes 0-3 as little-endian u32.
         */
        fun seedFromName(name: String): Int {
            val bytes = name.encodeToByteArray()
            val padded = ByteArray(8)
            bytes.copyInto(padded, endIndex = minOf(bytes.size, 8))
            return (padded[0].toInt() and 0xFF) or
                ((padded[1].toInt() and 0xFF) shl 8) or
                ((padded[2].toInt() and 0xFF) shl 16) or
                ((padded[3].toInt() and 0xFF) shl 24)
        }

        /**
         * 32-bit integer multiply matching JavaScript's Math.imul().
         * Kotlin's Int multiplication already wraps at 32 bits, but we
         * keep this explicit for clarity and to match the JS port exactly.
         */
        private fun imul(a: Int, b: Int): Int = a * b
    }
}
