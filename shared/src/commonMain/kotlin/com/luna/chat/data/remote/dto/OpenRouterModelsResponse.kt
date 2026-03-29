package com.luna.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterModelsResponse(
    @SerialName("data") val data: List<OpenRouterModel> = emptyList(),
)

@Serializable
data class OpenRouterModel(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("context_length") val contextLength: Int = 0,
    @SerialName("architecture") val architecture: ModelArchitecture? = null,
)

@Serializable
data class ModelArchitecture(
    @SerialName("input_modalities") val inputModalities: List<String> = emptyList(),
    @SerialName("output_modalities") val outputModalities: List<String> = emptyList(),
)
