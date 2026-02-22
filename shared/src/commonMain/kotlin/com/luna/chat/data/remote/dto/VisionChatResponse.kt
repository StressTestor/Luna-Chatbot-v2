package com.luna.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisionChatResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("created") val created: Long? = null,
    @SerialName("model") val model: String? = null,
    @SerialName("choices") val choices: List<VisionChoice>,
    @SerialName("usage") val VisionUsage: VisionUsage? = null
)

@Serializable
data class VisionChoice(
    @SerialName("index") val index: Int? = null,
    @SerialName("message") val message: VisionAssistantMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class VisionAssistantMessage(
    @SerialName("role") val role: String = "assistant",
    @SerialName("content") val content: String
)

@Serializable
data class VisionUsage(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null
)
