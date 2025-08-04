package com.luna.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request payloads for OpenRouter/Groq-style vision chat completions.
 *
 * Security posture:
 * - Callers must ensure any text has been scrubbed for sensitive data upstream when appropriate.
 * - Images are supplied as in-memory data URLs (data:<mime>;base64,...) only; no file paths are used.
 */
data class VisionChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("temperature") val temperature: Float? = null,
    @SerializedName("max_tokens") val maxTokens: Int? = null,
    @SerializedName("top_p") val topP: Float? = null,
    @SerializedName("presence_penalty") val presencePenalty: Float? = null,
    @SerializedName("frequency_penalty") val frequencyPenalty: Float? = null
)

data class Message(
    @SerializedName("role") val role: String = "user",
    @SerializedName("content") val content: List<ContentPart>
)

/**
 * Top-level sealed class for multi-part content entries following OpenAI/Groq/OpenRouter schema.
 * Use TextPart for textual prompts and ImageUrlPart with a data URL for in-memory image content.
 */
sealed class ContentPart {
    data class TextPart(
        @SerializedName("type") val type: String = "text",
        @SerializedName("text") val text: String
    ) : ContentPart()

    data class ImageUrlPart(
        @SerializedName("type") val type: String = "image_url",
        @SerializedName("image_url") val imageUrl: ImageUrl
    ) : ContentPart()
}

/**
 * Image URL wrapper that carries a data URL (data:<mime>;base64,...) for in-memory images.
 */
data class ImageUrl(
    @SerializedName("url") val url: String
)