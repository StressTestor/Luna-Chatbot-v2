package com.luna.chat.domain.usecase

import com.luna.chat.domain.exception.ChatException

class ContentFilterUseCase constructor() {
    
    // Inappropriate content patterns for children
    private val inappropriatePatterns = listOf(
        // Violence and harmful content
        "kill", "murder", "death", "suicide", "hurt", "pain", "blood", "weapon", "gun", "knife",
        "violence", "fight", "attack", "destroy", "bomb", "explosive", "war", "battle",
        
        // Adult content
        "sex", "sexual", "porn", "naked", "nude", "adult", "mature", "intimate", "romance",
        "dating", "kiss", "love", "relationship", "boyfriend", "girlfriend",
        
        // Inappropriate language (mild filtering for children)
        "stupid", "dumb", "idiot", "hate", "shut up", "damn", "hell", "crap",
        
        // Scary/disturbing content
        "scary", "horror", "ghost", "monster", "nightmare", "creepy", "evil", "demon",
        "witch", "vampire", "zombie", "dead", "grave", "cemetery",
        
        // Drugs and substances
        "drug", "alcohol", "beer", "wine", "cigarette", "smoke", "tobacco", "weed", "marijuana",
        
        // Personal information requests
        "address", "phone number", "full name", "school name", "parent", "mom", "dad",
        "where do you live", "what's your name", "how old are you"
    )
    
    // Educational and positive keywords that should be encouraged
    private val positiveKeywords = listOf(
        "learn", "study", "homework", "math", "science", "reading", "writing", "history",
        "geography", "art", "music", "sports", "game", "fun", "friend", "family",
        "school", "teacher", "book", "story", "adventure", "explore", "discover",
        "create", "build", "imagine", "dream", "help", "kind", "nice", "good"
    )
    
    /**
     * Filter user input for inappropriate content and safety violations
     * @param input The user's message to filter
     * @return FilterResult indicating if content was filtered and why
     * @throws ChatException.ContentFilterException if content is inappropriate
     */
    fun filterUserInput(input: String): FilterResult {
        val cleanInput = input.trim().lowercase()
        
        // Check for empty or whitespace-only input
        if (cleanInput.isBlank()) {
            throw ChatException.ValidationException(
                ChatException.ValidationException.ValidationType.MESSAGE_EMPTY,
                "User input is empty or contains only whitespace"
            )
        }
        
        // Check for excessively long input
        if (input.length > MAX_INPUT_LENGTH) {
            throw ChatException.ValidationException(
                ChatException.ValidationException.ValidationType.MESSAGE_TOO_LONG,
                "User input exceeds maximum length of $MAX_INPUT_LENGTH characters"
            )
        }
        
        // Check for inappropriate content
        val inappropriatePattern = inappropriatePatterns.find { pattern ->
            cleanInput.contains(pattern, ignoreCase = true)
        }
        
        if (inappropriatePattern != null) {
            return FilterResult(
                content = input,
                isFiltered = true,
                reason = "Inappropriate content detected: $inappropriatePattern",
                filterType = FilterType.INAPPROPRIATE_INPUT,
                childFriendlyMessage = getChildFriendlyRedirection(FilterType.INAPPROPRIATE_INPUT)
            )
        }
        
        // Check for personal information sharing
        if (containsPersonalInfoSharing(cleanInput)) {
            return FilterResult(
                content = input,
                isFiltered = true,
                reason = "Personal information sharing detected",
                filterType = FilterType.PERSONAL_INFO,
                childFriendlyMessage = getChildFriendlyRedirection(FilterType.PERSONAL_INFO)
            )
        }
        
        // Check for attempts to bypass the filter
        if (containsFilterBypass(cleanInput)) {
            return FilterResult(
                content = input,
                isFiltered = true,
                reason = "Filter bypass attempt detected",
                filterType = FilterType.INAPPROPRIATE_INPUT,
                childFriendlyMessage = getChildFriendlyRedirection(FilterType.INAPPROPRIATE_INPUT)
            )
        }
        
        return FilterResult(
            content = input,
            isFiltered = false,
            reason = null,
            filterType = null,
            childFriendlyMessage = null
        )
    }
    
    /**
     * Filter AI response for inappropriate content and safety violations
     * @param response The AI's response to filter
     * @return FilterResult indicating if content was filtered and why
     */
    fun filterAiResponse(response: String): FilterResult {
        val cleanResponse = response.trim().lowercase()
        
        // Check for empty response
        if (cleanResponse.isBlank()) {
            return FilterResult(
                content = "I'm having trouble thinking of a good answer. Can you ask me something else? 🤔",
                isFiltered = true,
                reason = "AI response was empty",
                filterType = FilterType.INAPPROPRIATE_RESPONSE,
                childFriendlyMessage = "I'm having trouble thinking of a good answer. Can you ask me something else? 🤔"
            )
        }
        
        // Check for inappropriate content in AI response
        val inappropriatePattern = inappropriatePatterns.find { pattern ->
            cleanResponse.contains(pattern, ignoreCase = true)
        }
        
        if (inappropriatePattern != null) {
            return FilterResult(
                content = response,
                isFiltered = true,
                reason = "Inappropriate content detected in AI response: $inappropriatePattern",
                filterType = FilterType.INAPPROPRIATE_RESPONSE,
                childFriendlyMessage = getChildFriendlyRedirection(FilterType.INAPPROPRIATE_RESPONSE)
            )
        }
        
        // Check if response asks for personal information
        if (containsPersonalInfoRequest(cleanResponse)) {
            return FilterResult(
                content = response,
                isFiltered = true,
                reason = "AI response requests personal information",
                filterType = FilterType.PERSONAL_INFO,
                childFriendlyMessage = getChildFriendlyRedirection(FilterType.PERSONAL_INFO)
            )
        }
        
        // Check for responses that might be too complex or scary for children
        if (containsComplexOrScaryContent(cleanResponse)) {
            return FilterResult(
                content = response,
                isFiltered = true,
                reason = "AI response contains complex or potentially scary content",
                filterType = FilterType.INAPPROPRIATE_RESPONSE,
                childFriendlyMessage = "Let me try to explain that in a simpler way! 😊"
            )
        }
        
        // Check response length (keep responses reasonable for children)
        if (response.length > MAX_RESPONSE_LENGTH) {
            return FilterResult(
                content = response.take(MAX_RESPONSE_LENGTH) + "... Would you like me to tell you more? 😊",
                isFiltered = false,
                reason = "Response truncated for readability",
                filterType = null,
                childFriendlyMessage = null
            )
        }
        
        return FilterResult(
            content = response,
            isFiltered = false,
            reason = null,
            filterType = null,
            childFriendlyMessage = null
        )
    }
    
    private fun containsPersonalInfoRequest(text: String): Boolean {
        val personalInfoPatterns = listOf(
            "what is your name",
            "what's your name",
            "where do you live",
            "what's your address",
            "phone number",
            "how old are you",
            "what school do you go to",
            "where is your school",
            "tell me about your parents",
            "what's your mom's name",
            "what's your dad's name"
        )
        
        return personalInfoPatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
    }
    
    fun isContentAppropriate(content: String): Boolean {
        return !filterUserInput(content).isFiltered
    }
    
    private fun containsPersonalInfoSharing(text: String): Boolean {
        val personalInfoSharingPatterns = listOf(
            "my name is", "i am", "i'm", "my address", "i live at", "my phone",
            "my school is", "i go to", "my mom", "my dad", "my parent",
            "my age is", "i am \\d+ years old", "my birthday"
        )
        
        return personalInfoSharingPatterns.any { pattern ->
            text.contains(pattern.toRegex(RegexOption.IGNORE_CASE))
        }
    }
    
    private fun containsFilterBypass(text: String): Boolean {
        val bypassPatterns = listOf(
            "ignore previous", "forget the rules", "bypass filter", "don't filter",
            "pretend you're", "act like", "roleplay", "ignore safety",
            "tell me anyway", "just this once", "between you and me"
        )
        
        return bypassPatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
    }
    
    private fun containsComplexOrScaryContent(text: String): Boolean {
        val complexScaryPatterns = listOf(
            "existential", "philosophy", "meaning of life", "death", "dying",
            "depression", "anxiety", "mental health", "therapy", "medication",
            "politics", "religion", "controversial", "debate", "argument",
            "advanced mathematics", "quantum", "nuclear", "chemistry formulas"
        )
        
        return complexScaryPatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
    }
    
    private fun getChildFriendlyRedirection(filterType: FilterType): String {
        return when (filterType) {
            FilterType.INAPPROPRIATE_INPUT -> 
                "Let's talk about something else! What would you like to learn today? 📚"
            FilterType.INAPPROPRIATE_RESPONSE -> 
                "The AI said something silly. Let me try a better answer! 🤖"
            FilterType.PERSONAL_INFO -> 
                "Let's keep our personal information private! What else can I help with? 🔒"
            FilterType.GENERAL -> 
                "Let's try a different topic! What would you like to explore? 🌟"
        }
    }
    
    /**
     * Validate and filter content, throwing appropriate exceptions for safety violations
     */
    fun validateAndFilterInput(input: String): String {
        val result = filterUserInput(input)
        
        if (result.isFiltered) {
            throw ChatException.ContentFilterException(
                when (result.filterType) {
                    FilterType.INAPPROPRIATE_INPUT -> ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT
                    FilterType.PERSONAL_INFO -> ChatException.ContentFilterException.FilterType.PERSONAL_INFO
                    else -> ChatException.ContentFilterException.FilterType.GENERAL
                },
                result.reason ?: "Content filtered for safety"
            )
        }
        
        return result.content
    }
    
    /**
     * Get a safe, child-friendly replacement for filtered AI responses
     */
    fun getSafeResponseReplacement(originalResponse: String): String {
        val result = filterAiResponse(originalResponse)
        
        return if (result.isFiltered) {
            result.childFriendlyMessage ?: "I'm having trouble with that answer. Can you ask me something else? 😊"
        } else {
            result.content
        }
    }
    
    companion object {
        private const val MAX_RESPONSE_LENGTH = 1000
        private const val MAX_INPUT_LENGTH = 500
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