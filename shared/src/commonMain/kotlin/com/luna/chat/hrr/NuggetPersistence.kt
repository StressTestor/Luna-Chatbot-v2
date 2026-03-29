package com.luna.chat.hrr

import com.luna.chat.platform.SecureStorage

/** Abstraction over key-value storage, testable without platform SecureStorage. */
interface NuggetPersistence {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

/** Production implementation wrapping the platform's encrypted SecureStorage. */
class SecureNuggetPersistence(private val secureStorage: SecureStorage) : NuggetPersistence {
    override fun getString(key: String): String? = secureStorage.getString(key)
    override fun putString(key: String, value: String) = secureStorage.putString(key, value)
}
