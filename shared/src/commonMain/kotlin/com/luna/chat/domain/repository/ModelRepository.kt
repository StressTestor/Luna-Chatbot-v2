package com.luna.chat.domain.repository

import com.luna.chat.domain.entity.LunaModel

interface ModelRepository {
    suspend fun getAvailableModels(): List<LunaModel>
}
