package com.luna.chat.update.downloader

import kotlinx.coroutines.flow.Flow

/**
 * Interface for the Update Downloader component that handles downloading update packages.
 * This downloader is responsible for retrieving update packages from various sources
 * and providing progress updates during the download process.
 */
interface UpdateDownloader {
    /**
     * Download an update package from the given URL
     * 
     * @param url URL of the update package
     * @param destinationPath Path where the update package should be saved
     * @return Flow of download progress from 0.0 to 1.0
     */
    suspend fun downloadUpdate(url: String, destinationPath: String): Flow<Float>
    
    /**
     * Download an update package from the given local path (e.g., ADB pushed file)
     * 
     * @param sourcePath Source path of the update package
     * @param destinationPath Path where the update package should be saved
     * @return Flow of download progress from 0.0 to 1.0
     */
    suspend fun copyLocalUpdate(sourcePath: String, destinationPath: String): Flow<Float>
    
    /**
     * Cancel the current download operation
     * 
     * @return true if the download was canceled successfully, false otherwise
     */
    suspend fun cancelDownload(): Boolean
    
    /**
     * Check if a download is currently in progress
     * 
     * @return true if a download is in progress, false otherwise
     */
    fun isDownloadInProgress(): Boolean
}