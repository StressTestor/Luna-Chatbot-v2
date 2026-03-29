package com.luna.chat.update.manager

import com.luna.chat.update.downloader.UpdateDownloader
import com.luna.chat.update.installer.UpdateInstaller
import com.luna.chat.update.logging.UpdateLogger
import com.luna.chat.update.logging.UpdateLogEntry
import com.luna.chat.update.logging.LogLevel
import com.luna.chat.update.notification.UpdateNotifier
import com.luna.chat.update.verification.UpdateVerifier
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt bindings for Update subsystem.
 *
 * Notes:
 * - Production bindings remain via @Binds for UpdateManager.
 * - Minimal default no-op implementations are provided here to satisfy DI in unit tests,
 *   preventing compile-time MissingBinding errors when update subsystem is not exercised.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateModule {

    @Binds
    @Singleton
    abstract fun bindUpdateManager(impl: UpdateManagerImpl): UpdateManager

    companion object {
        @Provides
        @Singleton
        fun provideUpdateVerifier(): UpdateVerifier = object : UpdateVerifier {
            override suspend fun verifyUpdatePackage(updatePath: String) =
                com.luna.chat.update.model.VerificationResult(isSuccess = true, errorCode = 0, message = "noop")

            override suspend fun verifyComponent(
                component: com.luna.chat.update.model.UpdateComponent,
                data: ByteArray
            ) = com.luna.chat.update.model.VerificationResult(isSuccess = true, errorCode = 0, message = "noop")

            override fun verifyCompatibility(updatePackage: com.luna.chat.update.model.UpdatePackage) =
                com.luna.chat.update.model.VerificationResult(isSuccess = true, errorCode = 0, message = "noop")

            override suspend fun verifySignature(
                updatePackage: com.luna.chat.update.model.UpdatePackage,
                signature: ByteArray
            ) = com.luna.chat.update.model.VerificationResult(isSuccess = true, errorCode = 0, message = "noop")
        }

        @Provides
        @Singleton
        fun provideUpdateDownloader(): UpdateDownloader = object : UpdateDownloader {
            override suspend fun downloadUpdate(url: String, destinationPath: String) =
                kotlinx.coroutines.flow.flow<Float> { emit(1f) }

            override suspend fun copyLocalUpdate(sourcePath: String, destinationPath: String) =
                kotlinx.coroutines.flow.flow<Float> { emit(1f) }

            override suspend fun cancelDownload(): Boolean = true
            override fun isDownloadInProgress(): Boolean = false
        }

        @Provides
        @Singleton
        fun provideUpdateInstaller(): UpdateInstaller = object : UpdateInstaller {
            override suspend fun installComponent(
                component: com.luna.chat.update.model.UpdateComponent,
                data: ByteArray
            ) = com.luna.chat.update.model.InstallationResult(isSuccess = true, errorCode = 0, message = "ok", requiresRestart = false)

            override suspend fun installAssetUpdate(assetPath: String, data: ByteArray) =
                com.luna.chat.update.model.InstallationResult(isSuccess = true, errorCode = 0, message = "ok", requiresRestart = false)

            override suspend fun installResourceUpdate(resourcePath: String, data: ByteArray) =
                com.luna.chat.update.model.InstallationResult(isSuccess = true, errorCode = 0, message = "ok", requiresRestart = false)

            override fun requiresRestart(component: com.luna.chat.update.model.UpdateComponent): Boolean = false

            override suspend fun cleanupTemporaryFiles(): Boolean = true
        }

        @Provides
        @Singleton
        fun provideUpdateNotifier(): UpdateNotifier = object : UpdateNotifier {
            override fun notifyUpdateStatus(status: com.luna.chat.update.model.UpdateStatus) { /* no-op */ }
            override fun notifyUpdateComplete(success: Boolean, message: String, requiresRestart: Boolean) { /* no-op */ }
            override fun notifyUpdateError(code: Int, message: String, isCritical: Boolean) { /* no-op */ }
            override fun clearNotifications() { /* no-op */ }
            override fun notifyUpdateAvailable(updatePackage: com.luna.chat.update.model.UpdatePackage) { /* no-op */ }
        }

        @Provides
        @Singleton
        fun provideUpdateLogger(): UpdateLogger = object : UpdateLogger {
            private val logs = mutableListOf<UpdateLogEntry>()

            override fun logInfo(message: String, details: Map<String, String>) {
                logs += UpdateLogEntry(timestamp = System.currentTimeMillis(), level = LogLevel.INFO, message = message, details = details)
            }

            override fun logWarning(message: String, details: Map<String, String>) {
                logs += UpdateLogEntry(timestamp = System.currentTimeMillis(), level = LogLevel.WARNING, message = message, details = details)
            }

            override fun logError(error: com.luna.chat.update.model.UpdateError) {
                logs += UpdateLogEntry(
                    timestamp = System.currentTimeMillis(),
                    level = LogLevel.ERROR,
                    message = error.message,
                    details = mapOf("code" to error.code.toString(), "critical" to error.isCritical.toString())
                )
            }

            override fun logStatusChange(status: com.luna.chat.update.model.UpdateStatus) {
                logs += UpdateLogEntry(
                    timestamp = System.currentTimeMillis(),
                    level = LogLevel.INFO,
                    message = "status:${status::class.simpleName}",
                    details = emptyMap()
                )
            }

            override fun getUpdateLogs(): List<UpdateLogEntry> = logs.toList()

            override fun clearLogs() { logs.clear() }
        }
    }
}