package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.VisionApiService
import com.luna.chat.data.remote.dto.AssistantMessage
import com.luna.chat.data.remote.dto.Choice
import com.luna.chat.data.remote.dto.Message
import com.luna.chat.data.remote.dto.VisionChatRequest
import com.luna.chat.data.remote.dto.VisionChatResponse
import com.luna.chat.domain.entity.VisionAnalysisResult
import com.luna.chat.security.ContentFilteringService
import com.luna.chat.security.SecureLogger
import com.luna.chat.security.SecurityConfig
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class VisionRepositoryImplTest {

    private val apiKeyProvider = mock<ApiKeyProvider> {
        on { getApiKey() } doReturn "sk-test"
    }
    private val filteringService = mock<ContentFilteringService> {
        on { getModelParameters() } doReturn ContentFilteringService.ModelParams(temperature = 0.4f, topP = 0.8f)
        on { filterAiResponse(any()) } doReturn ContentFilteringService.FilterResult(isFiltered = false, text = null)
    }
    private val securityConfig = mock<SecurityConfig> {}
    private val secureLogger = mock<SecureLogger> {}
    private val visionApi = mock<VisionApiService> {}

    private fun repo(): VisionRepositoryImpl =
        VisionRepositoryImpl(visionApi, apiKeyProvider, filteringService, securityConfig, secureLogger)

    @Test
    fun limited_mode_enabled_returns_unavailable() = runBlocking {
        whenever(securityConfig.isLimitedModeEnabled()).thenReturn(true)
        whenever(securityConfig.isVisionModelApproved()).thenReturn(true) // even if approved, limited mode wins

        val result = repo().analyze("Zm9vYmFy", "image/png", "hi")
        assert(result is VisionAnalysisResult.Unavailable)
    }

    @Test
    fun governance_disabled_returns_unavailable() = runBlocking {
        whenever(securityConfig.isLimitedModeEnabled()).thenReturn(false)
        whenever(securityConfig.isVisionModelApproved()).thenReturn(false)

        val result = repo().analyze("Zm9vYmFy", "image/png", "hi")
        assert(result is VisionAnalysisResult.Unavailable)
    }

    @Test
    fun success_non_empty_content_returns_success_after_scrub_and_filter() = runBlocking {
        whenever(securityConfig.isLimitedModeEnabled()).thenReturn(false)
        whenever(securityConfig.isVisionModelApproved()).thenReturn(true)

        val responseBody = VisionChatResponse(
            id = "id",
            created = 1L,
            model = "m",
            choices = listOf(Choice(index = 0, message = AssistantMessage(content = "A cat on a mat"))),
            usage = null
        )
        whenever(visionApi.analyzeImage(any(), any())).thenReturn(Response.success(responseBody))

        val result = repo().analyze("Zm9vYmFy", "image/png", "hi")
        assert(result is VisionAnalysisResult.Success)
        assertEquals("A cat on a mat", (result as VisionAnalysisResult.Success).summary)
    }

    @Test
    fun http_error_returns_error() = runBlocking {
        whenever(securityConfig.isLimitedModeEnabled()).thenReturn(false)
        whenever(securityConfig.isVisionModelApproved()).thenReturn(true)

        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"bad"}""")
        whenever(visionApi.analyzeImage(any(), any())).thenReturn(Response.error(500, errorBody))

        val result = repo().analyze("Zm9vYmFy", "image/png", "hi")
        assert(result is VisionAnalysisResult.Error)
    }

    @Test
    fun empty_content_returns_error() = runBlocking {
        whenever(securityConfig.isLimitedModeEnabled()).thenReturn(false)
        whenever(securityConfig.isVisionModelApproved()).thenReturn(true)

        val responseBody = VisionChatResponse(
            id = "id",
            created = 1L,
            model = "m",
            choices = listOf(Choice(index = 0, message = AssistantMessage(content = ""))),
            usage = null
        )
        whenever(visionApi.analyzeImage(any(), any())).thenReturn(Response.success(responseBody))

        val result = repo().analyze("Zm9vYmFy", "image/png", "hi")
        assert(result is VisionAnalysisResult.Error)
    }
}