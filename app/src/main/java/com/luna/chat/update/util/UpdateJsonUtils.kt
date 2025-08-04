package com.luna.chat.update.util

import com.luna.chat.update.model.UpdateManifest
import com.luna.chat.update.model.UpdatePackage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Utility class for JSON serialization and deserialization of update-related objects.
 */
object UpdateJsonUtils {
    /**
     * JSON serializer/deserializer with lenient configuration for update objects
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
    
    /**
     * Serialize an UpdatePackage to JSON string
     *
     * @param updatePackage The update package to serialize
     * @return JSON string representation of the update package
     */
    fun serializeUpdatePackage(updatePackage: UpdatePackage): String {
        return json.encodeToString(updatePackage)
    }
    
    /**
     * Deserialize an UpdatePackage from JSON string
     *
     * @param jsonString JSON string to deserialize
     * @return The deserialized UpdatePackage
     */
    fun deserializeUpdatePackage(jsonString: String): UpdatePackage {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Serialize an UpdateManifest to JSON string
     *
     * @param manifest The update manifest to serialize
     * @return JSON string representation of the update manifest
     */
    fun serializeManifest(manifest: UpdateManifest): String {
        return json.encodeToString(manifest)
    }
    
    /**
     * Deserialize an UpdateManifest from JSON string
     *
     * @param jsonString JSON string to deserialize
     * @return The deserialized UpdateManifest
     */
    fun deserializeManifest(jsonString: String): UpdateManifest {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Read an UpdateManifest from a file
     *
     * @param file File containing the JSON manifest
     * @return The deserialized UpdateManifest
     * @throws IOException If the file cannot be read or contains invalid JSON
     */
    @Throws(IOException::class)
    fun readManifestFromFile(file: File): UpdateManifest {
        return try {
            val jsonString = file.readText()
            deserializeManifest(jsonString)
        } catch (e: Exception) {
            throw IOException("Failed to read manifest from file: ${e.message}", e)
        }
    }
    
    /**
     * Write an UpdateManifest to a file
     *
     * @param manifest The manifest to write
     * @param file The file to write to
     * @throws IOException If the file cannot be written
     */
    @Throws(IOException::class)
    fun writeManifestToFile(manifest: UpdateManifest, file: File) {
        try {
            val jsonString = serializeManifest(manifest)
            file.writeText(jsonString)
        } catch (e: Exception) {
            throw IOException("Failed to write manifest to file: ${e.message}", e)
        }
    }
    
    /**
     * Read an UpdatePackage from a file
     *
     * @param file File containing the JSON update package
     * @return The deserialized UpdatePackage
     * @throws IOException If the file cannot be read or contains invalid JSON
     */
    @Throws(IOException::class)
    fun readUpdatePackageFromFile(file: File): UpdatePackage {
        return try {
            val jsonString = file.readText()
            val updatePackage = deserializeUpdatePackage(jsonString)
            updatePackage.packageFile = file
            updatePackage
        } catch (e: Exception) {
            throw IOException("Failed to read update package from file: ${e.message}", e)
        }
    }
    
    /**
     * Write an UpdatePackage to a file
     *
     * @param updatePackage The update package to write
     * @param file The file to write to
     * @throws IOException If the file cannot be written
     */
    @Throws(IOException::class)
    fun writeUpdatePackageToFile(updatePackage: UpdatePackage, file: File) {
        try {
            val jsonString = serializeUpdatePackage(updatePackage)
            file.writeText(jsonString)
            updatePackage.packageFile = file
        } catch (e: Exception) {
            throw IOException("Failed to write update package to file: ${e.message}", e)
        }
    }
}