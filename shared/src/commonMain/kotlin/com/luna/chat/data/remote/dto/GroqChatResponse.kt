package com.luna.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroqChatResponse(
    @SerialName("choices") val choices: List<GroqChoice>,
    @SerialName("usage") val usage: GroqUsage? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("object") val objectType: String? = null,
    @SerialName("created") val created: Long? = null,
    @SerialName("model") val model: String? = null
) {
    fun getFirstChoice(): GroqChoice? = choices.firstOrNull()
    fun getAssistantMessage(): String? {
        val msg = getFirstChoice()?.message ?: return null
        // Prefer content, fall back to reasoning (for reasoning models like nemotron)
        return msg.content?.takeIf { it.isNotBlank() }
            ?: msg.reasoning?.takeIf { it.isNotBlank() }
    }
    fun isComplete(): Boolean = getFirstChoice()?.finishReason == FINISH_REASON_STOP

    companion object {
        const val FINISH_REASON_STOP = "stop"
        const val FINISH_REASON_LENGTH = "length"
        const val FINISH_REASON_CONTENT_FILTER = "content_filter"
    }
}

@Serializable
data class GroqChoice(
    @SerialName("message") val message: ResponseMessage,
    @SerialName("finish_reason") val finishReason: String? = null,
    @SerialName("index") val index: Int? = null,
) {
    fun isComplete(): Boolean = finishReason == GroqChatResponse.FINISH_REASON_STOP
    fun wasContentFiltered(): Boolean = finishReason == GroqChatResponse.FINISH_REASON_CONTENT_FILTER
}

/**
 * Response message from the API. Unlike [GroqMessage] (used for requests),
 * content can be null — reasoning models put their output in the reasoning
 * field instead.
 */
@Serializable
data class ResponseMessage(
    @SerialName("role") val role: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("reasoning") val reasoning: String? = null,
    @SerialName("refusal") val refusal: String? = null,
)

@Serializable
data class GroqUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)
