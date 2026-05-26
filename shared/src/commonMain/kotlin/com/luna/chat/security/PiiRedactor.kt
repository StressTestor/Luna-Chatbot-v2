package com.luna.chat.security

/**
 * Redacts personally-identifiable information from text before it leaves
 * the device. Used to scrub the conversation payload sent to the fact
 * extraction LLM call — that path doesn't need exact PII to extract
 * useful facts ("user has a pet" works the same whether the dog's
 * vaccination address is in the message or not).
 *
 * Scope is intentionally narrow: catches the obvious leaks (email,
 * phone, SSN, credit-card-like numbers, IP addresses) without false-
 * positive churn from name detection.
 */
class PiiRedactor {

    fun redact(text: String): String {
        var out = text
        for ((pattern, replacement) in REDACTIONS) {
            out = pattern.replace(out, replacement)
        }
        return out
    }

    companion object {
        private val REDACTIONS: List<Pair<Regex, String>> = listOf(
            Regex("""\b[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}\b""") to "[email]",
            Regex("""\b(?:\+?\d{1,2}[\s.\-])?\(?\d{3}\)?[\s.\-]\d{3}[\s.\-]\d{4}\b""") to "[phone]",
            Regex("""\b\d{3}-\d{2}-\d{4}\b""") to "[ssn]",
            Regex("""\b(?:\d[ \-]?){13,16}\b""") to "[card]",
            Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b""") to "[ip]",
        )
    }
}
