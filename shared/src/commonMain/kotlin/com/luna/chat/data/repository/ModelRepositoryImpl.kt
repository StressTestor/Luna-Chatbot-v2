package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.domain.entity.LunaModel
import com.luna.chat.domain.entity.categorizeModel
import com.luna.chat.domain.repository.ModelRepository

class ModelRepositoryImpl(
    private val apiClient: LunaApiClient,
) : ModelRepository {

    private var cache: List<LunaModel>? = null

    override suspend fun getAvailableModels(): List<LunaModel> {
        cache?.let { return it }

        val response = apiClient.fetchModels()
        val models = response.data
            .filter { it.id.endsWith(":free") }
            .map { raw ->
                val cleanName = raw.name.ifBlank {
                    raw.id.substringBefore(":free")
                        .substringAfter("/")
                        .replace("-", " ")
                }
                LunaModel(
                    id = raw.id,
                    displayName = cleanName,
                    contextLength = raw.contextLength,
                    category = categorizeModel(
                        id = raw.id,
                        inputModalities = raw.architecture?.inputModalities.orEmpty(),
                    ),
                    provider = raw.id.substringBefore("/"),
                )
            }
            .sortedBy { it.displayName.lowercase() }

        cache = models
        return models
    }
}
