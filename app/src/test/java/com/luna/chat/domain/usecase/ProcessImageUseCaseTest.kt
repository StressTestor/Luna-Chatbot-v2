package com.luna.chat.domain.usecase

import com.luna.chat.domain.entity.VisionAnalysisResult
import com.luna.chat.domain.repository.VisionRepository
import com.luna.chat.security.ContentFilteringService
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.security.SecureLogger
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ProcessImageUseCaseTest {

    private val mockVisionRepository = mock<VisionRepository> {}
    private val mockFiltering = mock<ContentFilteringService> {}
    private val mockApiKeyProvider = mock<ApiKeyProvider> {}
    private val mockLogger = mock<SecureLogger> {}

    @Test
    fun invoke_success_returns_result() = runBlocking {
        val bytes = ByteArray(1024) { 1 }
        val mime = "image/png"

        val visionRepo = mock<VisionRepository> {
            onBlocking { analyze(any(), any(), any()) } doReturn VisionAnalysisResult.Success("summary ok")
        }

        val useCase = ProcessImageUseCase(
            visionRepository = visionRepo,
            contentFilteringService = mockFiltering,
            apiKeyProvider = mockApiKeyProvider,
            secureLogger = mockLogger
        )

        val result = useCase.invoke(bytes, mime, "hi")
        assertEquals("summary ok", result.getOrNull())
    }

    @Test
    fun invoke_unavailable_maps_to_failure() = runBlocking {
        val bytes = ByteArray(1024) { 1 }
        val mime = "image/png"

        val visionRepo = mock<VisionRepository> {
            onBlocking { analyze(any(), any(), any()) } doReturn VisionAnalysisResult.Unavailable("nope")
        }

        val useCase = ProcessImageUseCase(
            visionRepository = visionRepo,
            contentFilteringService = mockFiltering,
            apiKeyProvider = mockApiKeyProvider,
            secureLogger = mockLogger
        )

        val result = useCase.invoke(bytes, mime, null)
        assertEquals(null, result.getOrNull())
        assertEquals("nope", result.exceptionOrNull()?.message)
    }

    @Test
    fun invoke_error_maps_to_failure() = runBlocking {
        val bytes = ByteArray(1024) { 1 }
        val mime = "image/png"

        val visionRepo = mock<VisionRepository> {
            onBlocking { analyze(any(), any(), any()) } doReturn VisionAnalysisResult.Error("bad")
        }

        val useCase = ProcessImageUseCase(
            visionRepository = visionRepo,
            contentFilteringService = mockFiltering,
            apiKeyProvider = mockApiKeyProvider,
            secureLogger = mockLogger
        )

        val result = useCase.invoke(bytes, mime, null)
        assertEquals(null, result.getOrNull())
        assertEquals("bad", result.exceptionOrNull()?.message)
    }

    @Test
    fun invoke_rejects_invalid_image() = runBlocking {
        val bytes = ByteArray(5 * 1024 * 1024) { 1 } // over size cap
        val mime = "image/png"

        val useCase = ProcessImageUseCase(
            visionRepository = mockVisionRepository,
            contentFilteringService = mockFiltering,
            apiKeyProvider = mockApiKeyProvider,
            secureLogger = mockLogger
        )

        val result = useCase.invoke(bytes, mime, null)
        // Expect failure due to ImageValidator rejecting oversize
        assertEquals(null, result.getOrNull())
    }
}