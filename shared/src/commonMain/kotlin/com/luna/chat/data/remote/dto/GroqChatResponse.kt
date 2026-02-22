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
    fun getAssistantMessage(): String? = getFirstChoice()?.message?.content
    fun isComplete(): Boolean = getFirstChoice()?.finishReason == FINISH_REASON_STOP

    companion object {
        const val FINISH_REASON_STOP = "stop"
        const val FINISH_REASON_LENGTH = "length"
        const val FINISH_REASON_CONTENT_FILTER = "content_filter"
    }
}

@Serializable
data class GroqChoice(
    @SerialName("message") val message: GroqMessage,
    @SerialName("finish_reason") val finishReason: String? = null,
    @SerialName("index") val index: Int? = null
) {
    fun isComplete(): Boolean = finishReason == GroqChatResponse.FINISH_REASON_STOP
    fun wasContentFiltered(): Boolean = finishReason == GroqChatResponse.FINISH_REASON_CONTENT_FILTER
}

@Serializable
data class GroqUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)
