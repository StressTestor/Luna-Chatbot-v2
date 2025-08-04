package com.luna.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GroqChatResponse(
    @SerializedName("choices")
    val choices: List<GroqChoice>,
    @SerializedName("usage")
    val usage: GroqUsage,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("object")
    val objectType: String? = null,
    @SerializedName("created")
    val created: Long? = null,
    @SerializedName("model")
    val model: String? = null
) {
    init {
        validateResponse()
    }
    
    private fun validateResponse() {
        require(choices.isNotEmpty()) { "Response must contain at least one choice" }
        require(usage.isValid()) { "Usage information must be valid" }
        
        // Validate all choices
        choices.forEach { choice ->
            require(choice.isValid()) { "All choices must be valid" }
        }
    }
    
    fun isValid(): Boolean {
        return try {
            validateResponse()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    fun getFirstChoice(): GroqChoice? = choices.firstOrNull()
    
    fun getAssistantMessage(): String? = getFirstChoice()?.message?.content
    
    fun isComplete(): Boolean = getFirstChoice()?.finishReason == FINISH_REASON_STOP
    
    companion object {
        const val FINISH_REASON_STOP = "stop"
        const val FINISH_REASON_LENGTH = "length"
        const val FINISH_REASON_CONTENT_FILTER = "content_filter"
    }
}

data class GroqChoice(
    @SerializedName("message")
    val message: GroqMessage,
    @SerializedName("finish_reason")
    val finishReason: String,
    @SerializedName("index")
    val index: Int? = null
) {
    init {
        validateChoice()
    }
    
    private fun validateChoice() {
        require(message.isValid()) { "Choice message must be valid" }
        require(finishReason.isNotBlank()) { "Finish reason cannot be blank" }
        require(finishReason in VALID_FINISH_REASONS) { 
            "Finish reason must be one of: ${VALID_FINISH_REASONS.joinToString()}" 
        }
    }
    
    fun isValid(): Boolean {
        return try {
            validateChoice()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    fun isComplete(): Boolean = finishReason == GroqChatResponse.FINISH_REASON_STOP
    
    fun wasContentFiltered(): Boolean = finishReason == GroqChatResponse.FINISH_REASON_CONTENT_FILTER
    
    companion object {
        val VALID_FINISH_REASONS = setOf(
            GroqChatResponse.FINISH_REASON_STOP,
            GroqChatResponse.FINISH_REASON_LENGTH,
            GroqChatResponse.FINISH_REASON_CONTENT_FILTER
        )
    }
}

data class GroqUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
) {
    init {
        validateUsage()
    }
    
    private fun validateUsage() {
        require(promptTokens >= 0) { "Prompt tokens must be non-negative" }
        require(completionTokens >= 0) { "Completion tokens must be non-negative" }
        require(totalTokens >= 0) { "Total tokens must be non-negative" }
        require(totalTokens == promptTokens + completionTokens) { 
            "Total tokens must equal prompt tokens plus completion tokens" 
        }
    }
    
    fun isValid(): Boolean {
        return try {
            validateUsage()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    fun getCostEstimate(inputCostPer1K: Double = 0.0002, outputCostPer1K: Double = 0.0002): Double {
        return (promptTokens * inputCostPer1K / 1000) + (completionTokens * outputCostPer1K / 1000)
    }
}