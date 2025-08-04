package com.luna.chat.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for filtering content to ensure child safety
 */
@Singleton
class ContentFilteringService @Inject constructor(
    private val securityConfig: SecurityConfig,
    private val secureLogger: SecureLogger
) {
    companion object {
        // List of inappropriate content patterns
        private val INAPPROPRIATE_PATTERNS = listOf(
            // Violence-related terms
            Regex("\\b(kill|murder|weapon|gun|bomb|attack|fight|hurt|violent|torture)\\b", RegexOption.IGNORE_CASE),
            
            // Adult content terms
            Regex("\\b(sex|porn|nude|naked|explicit|adult|xxx)\\b", RegexOption.IGNORE_CASE),
            
            // Drugs and alcohol
            Regex("\\b(drug|cocaine|heroin|marijuana|alcohol|cigarette|smoking|vape)\\b", RegexOption.IGNORE_CASE),
            
            // Hate speech and bullying
            Regex("\\b(hate|racist|bully|stupid|idiot|dumb)\\b", RegexOption.IGNORE_CASE),
            
            // Personal information requests
            Regex("\\b(address|phone|where.*live|school name|parent.*name)\\b", RegexOption.IGNORE_CASE)
        )
        
        // List of educational topics that are allowed
        private val EDUCATIONAL_TOPICS = listOf(
            "science", "math", "history", "geography", "literature", 
            "art", "music", "space", "animals", "plants", "environment",
            "technology", "coding", "robotics", "astronomy", "biology",
            "chemistry", "physics", "geology", "weather", "climate",
            "dinosaurs", "oceans", "planets", "solar system", "stars",
            "languages", "culture", "sports", "health", "nutrition"
        )
    }
    
    /**
     * Filter user input to ensure it's appropriate for children
     * @param userInput The user input to filter
     * @return FilterResult containing the filtered input and whether it was modified
     */
    fun filterUserInput(userInput: String): FilterResult {
        // Check if content filtering is enabled
        if (!securityConfig.areParentalControlsEnabled()) {
            return FilterResult(userInput, false, null)
        }
        
        // Check for inappropriate content
        val matchedPatterns = INAPPROPRIATE_PATTERNS.filter { pattern ->
            pattern.containsMatchIn(userInput)
        }
        
        // If inappropriate content is found
        if (matchedPatterns.isNotEmpty()) {
            secureLogger.warn("Inappropriate content detected in user input")
            
            // Log the filtered content for review
            logInappropriateContent(userInput, matchedPatterns)
            
            // Return a safe response
            return FilterResult(
                input = "[Content filtered]",
                wasFiltered = true,
                reason = "The message contained inappropriate content"
            )
        }
        
        return FilterResult(userInput, false, null)
    }
    
    /**
     * Filter AI response to ensure it's appropriate for children
     * @param aiResponse The AI response to filter
     * @return FilterResult containing the filtered response and whether it was modified
     */
    fun filterAiResponse(aiResponse: String): FilterResult {
        // Check if content filtering is enabled
        if (!securityConfig.areParentalControlsEnabled()) {
            return FilterResult(aiResponse, false, null)
        }
        
        // Check for inappropriate content
        val matchedPatterns = INAPPROPRIATE_PATTERNS.filter { pattern ->
            pattern.containsMatchIn(aiResponse)
        }
        
        // If inappropriate content is found
        if (matchedPatterns.isNotEmpty()) {
            secureLogger.warn("Inappropriate content detected in AI response")
            
            // Log the filtered content for review
            logInappropriateContent(aiResponse, matchedPatterns)
            
            // Return a safe response
            return FilterResult(
                input = "I'm sorry, but I can't provide that information. Let's talk about something else!",
                wasFiltered = true,
                reason = "The AI response contained inappropriate content"
            )
        }
        
        return FilterResult(aiResponse, false, null)
    }
    
    /**
     * Get AI model parameters based on security configuration
     * @return Map of model parameters
     */
    fun getModelParameters(): Map<String, Any> {
        return mapOf(
            "temperature" to securityConfig.getTemperatureParameter(),
            "top_p" to securityConfig.getTopPParameter(),
            "safe_mode" to true
        )
    }
    
    /**
     * Check if a topic is educational and allowed
     * @param topic The topic to check
     * @return true if the topic is educational and allowed, false otherwise
     */
    fun isEducationalTopic(topic: String): Boolean {
        return EDUCATIONAL_TOPICS.any { 
            topic.lowercase().contains(it.lowercase()) 
        }
    }
    
    /**
     * Log inappropriate content for review
     * @param content The content that was filtered
     * @param matchedPatterns The patterns that matched
     */
    private fun logInappropriateContent(content: String, matchedPatterns: List<Regex>) {
        // In a production app, you might want to send this to a secure server for review
        // For now, we'll just log it locally
        secureLogger.warn("Filtered content: ${secureLogger.sanitizeMessage(content)}")
        secureLogger.warn("Matched patterns: ${matchedPatterns.map { it.pattern }}")
    }
    
    /**
     * Result of content filtering
     * @param input The filtered input
     * @param wasFiltered Whether the input was filtered
     * @param reason The reason for filtering, if any
     */
    data class FilterResult(
        val input: String,
        val wasFiltered: Boolean,
        val reason: String?
    )
}