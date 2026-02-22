package com.luna.chat.data.repository

interface ParentAuthenticationService {
    suspend fun authenticateParent(password: String): ParentAuthResult
    suspend fun setupParentPassword(password: String): ParentAuthResult
    fun isParentPasswordSetup(): Boolean
}

data class ParentAuthResult(
    val isSuccess: Boolean,
    val message: String
)
