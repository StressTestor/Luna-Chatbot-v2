package com.luna.chat.domain.usecase

import com.luna.chat.domain.exception.ChatException

class ContentFilterUseCase constructor() {

    // Hard-block list: only content that has zero legitimate use in conversation.
    // Everything else is handled by the LLM system prompt which is far better at
    // understanding context than a keyword list ever will be.
    private val blockedPatterns = listOf(
        "porn", "pornography", "hentai",
        "how to make a bomb", "how to make a weapon",
        "how to kill", "how to hurt",
    ).map { "\\b${Regex.escape(it)}\\b".toRegex(RegexOption.IGNORE_CASE) }

    // Prompt injection / jailbreak patterns — prevent the LLM system prompt
    // from being overridden.
    private val jailbreakPatterns = listOf(
        "ignore previous instructions",
        "ignore your instructions",
        "forget the rules",
        "forget your rules",
        "bypass filter",
        "ignore safety",
        "ignore all previous",
        "disregard your programming",
        "new system prompt",
        "you are now",
    ).map { it.toRegex(RegexOption.IGNORE_CASE) }

    fun filterUserInput(input: String): FilterResult {
        val cleanInput = input.trim()

        if (cleanInput.isBlank()) {
            throw ChatException.ValidationException(
                ChatException.ValidationException.ValidationType.MESSAGE_EMPTY,
                "User input is empty or contains only whitespace"
            )
        }

        if (cleanInput.length > MAX_INPUT_LENGTH) {
            throw ChatException.ValidationException(
                ChatException.ValidationException.ValidationType.MESSAGE_TOO_LONG,
                "User input exceeds maximum length of $MAX_INPUT_LENGTH characters"
            )
        }

        val lower = cleanInput.lowercase()

        if (blockedPatterns.any { it.containsMatchIn(lower) }) {
            return FilterResult(
                content = input,
                isFiltered = true,
                reason = "Blocked content",
                filterType = FilterType.INAPPROPRIATE_INPUT,
                childFriendlyMessage = "I can't help with that one. Try asking something else?"
            )
        }

        if (jailbreakPatterns.any { it.containsMatchIn(lower) }) {
            return FilterResult(
                content = input,
                isFiltered = true,
                reason = "Prompt injection attempt",
                filterType = FilterType.INAPPROPRIATE_INPUT,
                childFriendlyMessage = "Nice try, but I can't do that."
            )
        }

        return FilterResult(content = input, isFiltered = false, reason = null)
    }

    fun filterAiResponse(response: String): FilterResult {
        val cleanResponse = response.trim()

        if (cleanResponse.isBlank()) {
            return FilterResult(
                content = "Hmm, I blanked on that one. Try asking again?",
                isFiltered = true,
                reason = "AI response was empty",
                filterType = FilterType.INAPPROPRIATE_RESPONSE,
                childFriendlyMessage = "Hmm, I blanked on that one. Try asking again?"
            )
        }

        // Only hard-block the same explicit content in responses
        if (blockedPatterns.any { it.containsMatchIn(cleanResponse.lowercase()) }) {
            return FilterResult(
                content = response,
                isFiltered = true,
                reason = "Blocked content in AI response",
                filterType = FilterType.INAPPROPRIATE_RESPONSE,
                childFriendlyMessage = "I got a weird answer there. Ask me something else?"
            )
        }

        return FilterResult(content = response, isFiltered = false, reason = null)
    }

    fun isContentAppropriate(content: String): Boolean {
        return !filterUserInput(content).isFiltered
    }

    fun validateAndFilterInput(input: String): String {
        val result = filterUserInput(input)

        if (result.isFiltered) {
            throw ChatException.ContentFilterException(
                when (result.filterType) {
                    FilterType.INAPPROPRIATE_INPUT -> ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT
                    FilterType.PERSONAL_INFO -> ChatException.ContentFilterException.FilterType.PERSONAL_INFO
                    else -> ChatException.ContentFilterException.FilterType.GENERAL
                },
                result.reason ?: "Content filtered"
            )
        }

        return result.content
    }

    fun getSafeResponseReplacement(originalResponse: String): String {
        val result = filterAiResponse(originalResponse)
        return if (result.isFiltered) {
            result.childFriendlyMessage ?: "Something went wrong with that answer. Try again?"
        } else {
            result.content
        }
    }

    companion object {
        private const val MAX_INPUT_LENGTH = 2000
    }
}

enum class FilterType {
    INAPPROPRIATE_INPUT,
    INAPPROPRIATE_RESPONSE,
    PERSONAL_INFO,
    GENERAL
}

data class FilterResult(
    val content: String,
    val isFiltered: Boolean,
    val reason: String?,
    val filterType: FilterType? = null,
    val childFriendlyMessage: String? = null
)
