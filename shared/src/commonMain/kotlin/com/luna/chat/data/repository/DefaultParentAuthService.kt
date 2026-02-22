package com.luna.chat.data.repository

import com.luna.chat.platform.SecureStorage

class DefaultParentAuthService(private val secureStorage: SecureStorage) : ParentAuthenticationService {
    companion object {
        private const val KEY_PARENT_PASSWORD = "luna_parent_password"
    }

    override suspend fun authenticateParent(password: String): ParentAuthResult {
        val stored = secureStorage.getString(KEY_PARENT_PASSWORD)
        return if (stored == password) {
            ParentAuthResult(isSuccess = true, message = "Authentication successful!")
        } else {
            ParentAuthResult(isSuccess = false, message = "Incorrect password. Please try again!")
        }
    }

    override suspend fun setupParentPassword(password: String): ParentAuthResult {
        if (password.length < 4) {
            return ParentAuthResult(isSuccess = false, message = "Password must be at least 4 characters!")
        }
        secureStorage.putString(KEY_PARENT_PASSWORD, password)
        return ParentAuthResult(isSuccess = true, message = "Parent password set up successfully!")
    }

    override fun isParentPasswordSetup(): Boolean = secureStorage.contains(KEY_PARENT_PASSWORD)
}
