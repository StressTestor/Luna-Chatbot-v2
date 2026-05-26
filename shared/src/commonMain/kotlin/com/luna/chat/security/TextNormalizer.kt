package com.luna.chat.security

/**
 * Common Unicode/text normalization used by the content filter before
 * matching patterns. Catches the easy bypass tricks:
 *
 *  - Zero-width characters inserted between letters ("p​orn" -> "porn")
 *  - Cyrillic/Greek lookalikes ("pрrn" -> "porn")
 *  - Compatibility-equivalent characters (full-width, ligatures, etc.)
 *  - Case differences
 *
 * Returns a normalized lowercase form. Pattern matching should run on
 * this, and `compressNonLetters` should be applied as a second pass when
 * checking for short obfuscated keywords like "p.o.r.n".
 */
internal expect fun nfkdNormalize(s: String): String

internal fun normalizeForFilter(s: String): String {
    val nfkd = nfkdNormalize(s)
    val sb = StringBuilder(nfkd.length)
    for (c in nfkd) {
        if (c in INVISIBLE_CHARS) continue
        sb.append(CONFUSABLE_MAP[c] ?: c)
    }
    return sb.toString().lowercase()
}

internal fun compressNonLetters(s: String): String =
    s.filter { it.isLetterOrDigit() }

private val INVISIBLE_CHARS: Set<Char> = buildSet {
    addAll('​'..'‏') // ZWSP, ZWNJ, ZWJ, LRM, RLM
    addAll('⁠'..'⁤') // word joiner, invisible operators
    add('﻿') // BOM / zero-width no-break space
    add('­') // soft hyphen
}

private val CONFUSABLE_MAP: Map<Char, Char> = mapOf(
    // Cyrillic lookalikes
    'а' to 'a', 'А' to 'a',
    'е' to 'e', 'Е' to 'e',
    'о' to 'o', 'О' to 'o',
    'р' to 'p', 'Р' to 'p',
    'с' to 'c', 'С' to 'c',
    'у' to 'y', 'У' to 'y',
    'х' to 'x', 'Х' to 'x',
    'і' to 'i', 'І' to 'i',
    'ј' to 'j', 'Ј' to 'j',
    'ѕ' to 's', 'Ѕ' to 's',
    'к' to 'k', 'К' to 'k',
    'н' to 'h', 'Н' to 'h',
    'в' to 'b', 'В' to 'b',
    'т' to 't', 'Т' to 't',
    'м' to 'm', 'М' to 'm',
    // Greek lookalikes
    'ο' to 'o', 'Ο' to 'o',
    'α' to 'a', 'Α' to 'a',
    'β' to 'b', 'Β' to 'b',
    'ρ' to 'p', 'Ρ' to 'p',
    'ε' to 'e', 'Ε' to 'e',
)
