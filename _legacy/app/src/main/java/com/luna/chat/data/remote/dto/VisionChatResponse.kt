package com.luna.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Minimal response models aligned with existing DTO patterns.
 * Logging of raw responses should follow existing redaction policies upstream.
 */
data class VisionChatResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("created") val created: Long? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("choices") val choices: List<Choice>,
    @SerializedName("usage") val usage: Usage? = null
)

data class Choice(
    @SerializedName("index") val index: Int? = null,
    @SerializedName("message") val message: AssistantMessage,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class AssistantMessage(
    @SerializedName("role") val role: String = "assistant",
    @SerializedName("content") val content: String
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int? = null,
    @SerializedName("completion_tokens") val completionTokens: Int? = null,
    @SerializedName("total_tokens") val totalTokens: Int? = null
)