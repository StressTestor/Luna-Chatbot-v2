package com.luna.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisionChatRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<VisionMessage>,
    @SerialName("temperature") val temperature: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("top_p") val topP: Float? = null,
    @SerialName("presence_penalty") val presencePenalty: Float? = null,
    @SerialName("frequency_penalty") val frequencyPenalty: Float? = null
)

@Serializable
data class VisionMessage(
    @SerialName("role") val role: String = "user",
    @SerialName("content") val content: List<ContentPart>
)

@Serializable
sealed class ContentPart {
    @Serializable
    @SerialName("text")
    data class TextPart(
        @SerialName("text") val text: String
    ) : ContentPart()

    @Serializable
    @SerialName("image_url")
    data class ImageUrlPart(
        @SerialName("image_url") val imageUrl: ImageUrl
    ) : ContentPart()
}

@Serializable
data class ImageUrl(
    @SerialName("url") val url: String
)
