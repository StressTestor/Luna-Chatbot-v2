package com.luna.chat.domain.util

import com.luna.chat.domain.exception.ChatException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

class ErrorHandlerTest {
    
    @Test
    fun `handleException returns ChatException as-is`() {
        val originalException = ChatException.NetworkException("Network error")
        val handledException = ErrorHandler.handleException(originalException)
        
        assertSame(originalException, handledException)
    }
    
    @Test
    fun `handleException converts generic exception to ChatException`() {
        val ioException = IOException("IO error")
        val handledException = ErrorHandler.handleException(ioException)
        
        assertTrue(handledException is ChatException.NetworkException)
        assertTrue(handledException.message?.contains("IO error") == true)
    }
    
    @Test
    fun `getChildFriendlyMessage returns appropriate message`() {
        val networkException = ChatException.NetworkException()
        val ioException = IOException("IO error")
        
        assertEquals("Oops! Check your internet connection and try again 🌐", 
            ErrorHandler.getChildFriendlyMessage(networkException))
        assertEquals("Oops! Check your internet connection and try again 🌐", 
            ErrorHandler.getChildFriendlyMessage(ioException))
    }
    
    @Test
    fun `isRetryable returns correct values`() {
        val retryableException = ChatException.NetworkException()
        val nonRetryableException = ChatException.ContentFilterException()
        val ioException = IOException("IO error")
        
        assertTrue(ErrorHandler.isRetryable(retryableException))
        assertFalse(ErrorHandler.isRetryable(nonRetryableException))
        assertTrue(ErrorHandler.isRetryable(ioException)) // Converted to NetworkException
    }
    
    @Test
    fun `getRetryDelay returns appropriate delays`() {
        val networkException = ChatException.NetworkException()
        val nonRetryableException = ChatException.ContentFilterException()
        
        assertEquals(1000L, ErrorHandler.getRetryDelay(networkException, 0))
        assertEquals(2000L, ErrorHandler.getRetryDelay(networkException, 1))
        assertEquals(0L, ErrorHandler.getRetryDelay(nonRetryableException, 0))
    }
    
    @Test
    fun `ChatResult Success operations work correctly`() {
        val successResult = ChatResult.Success("test data")
        
        assertTrue(successResult.isSuccess())
        assertFalse(successResult.isError())
        assertFalse(successResult.isLoading())
        assertEquals("test data", successResult.getDataOrNull())
        assertNull(successResult.getExceptionOrNull())
        assertNull(successResult.getChildFriendlyErrorMessage())
    }
    
    @Test
    fun `ChatResult Error operations work correctly`() {
        val exception = ChatException.NetworkException("Network error")
        val errorResult = ChatResult.Error(exception)
        
        assertFalse(errorResult.isSuccess())
        assertTrue(errorResult.isError())
        assertFalse(errorResult.isLoading())
        assertNull(errorResult.getDataOrNull())
        assertSame(exception, errorResult.getExceptionOrNull())
        assertEquals("Oops! Check your internet connection and try again 🌐", 
            errorResult.getChildFriendlyErrorMessage())
    }
    
    @Test
    fun `ChatResult Loading operations work correctly`() {
        val loadingResult = ChatResult.Loading("Processing...")
        
        assertFalse(loadingResult.isSuccess())
        assertFalse(loadingResult.isError())
        assertTrue(loadingResult.isLoading())
        assertNull(loadingResult.getDataOrNull())
        assertNull(loadingResult.getExceptionOrNull())
        assertNull(loadingResult.getChildFriendlyErrorMessage())
    }
    
    @Test
    fun `ChatResult map transforms success data`() {
        val successResult = ChatResult.Success(5)
        val mappedResult = successResult.map { it * 2 }
        
        assertTrue(mappedResult is ChatResult.Success)
        assertEquals(10, (mappedResult as ChatResult.Success).data)
    }
    
    @Test
    fun `ChatResult map preserves error and loading states`() {
        val errorResult = ChatResult.Error(ChatException.NetworkException())
        val loadingResult = ChatResult.Loading()
        
        val mappedError = errorResult.map { "transformed" }
        val mappedLoading = loadingResult.map { "transformed" }
        
        assertTrue(mappedError is ChatResult.Error)
        assertTrue(mappedLoading is ChatResult.Loading)
    }
    
    @Test
    fun `ChatResult fold handles all cases`() {
        val successResult = ChatResult.Success("data")
        val errorResult = ChatResult.Error(ChatException.NetworkException())
        val loadingResult = ChatResult.Loading("Loading...")
        
        var successCalled = false
        var errorCalled = false
        var loadingCalled = false
        
        successResult.fold(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true },
            onLoading = { loadingCalled = true }
        )
        
        assertTrue(successCalled)
        assertFalse(errorCalled)
        assertFalse(loadingCalled)
        
        successCalled = false
        errorResult.fold(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true },
            onLoading = { loadingCalled = true }
        )
        
        assertFalse(successCalled)
        assertTrue(errorCalled)
        assertFalse(loadingCalled)
        
        errorCalled = false
        loadingResult.fold(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true },
            onLoading = { loadingCalled = true }
        )
        
        assertFalse(successCalled)
        assertFalse(errorCalled)
        assertTrue(loadingCalled)
    }
    
    @Test
    fun `toChatResult extension converts Flow to ChatResult Flow`() = runTest {
        val successFlow = flowOf("data1", "data2")
        val resultFlow = successFlow.toChatResult()
        val results = resultFlow.toList()
        
        assertEquals(2, results.size)
        assertTrue(results[0] is ChatResult.Success)
        assertTrue(results[1] is ChatResult.Success)
        assertEquals("data1", (results[0] as ChatResult.Success).data)
        assertEquals("data2", (results[1] as ChatResult.Success).data)
    }
    
    @Test
    fun `toChatResult extension handles errors`() = runTest {
        val errorFlow = flowOf<String>().apply {
            // This is a bit tricky to test with flow errors
            // In a real scenario, we'd use a flow that throws an exception
        }
        
        // For this test, we'll create a flow that throws an exception
        val throwingFlow = kotlinx.coroutines.flow.flow<String> {
            throw IOException("Test error")
        }
        
        val resultFlow = throwingFlow.toChatResult()
        val results = resultFlow.toList()
        
        assertEquals(1, results.size)
        assertTrue(results[0] is ChatResult.Error)
        val error = (results[0] as ChatResult.Error).exception
        assertTrue(error is ChatException.NetworkException)
    }
    
    @Test
    fun `handleChatErrors extension converts exceptions to ChatException`() = runTest {
        val throwingFlow = kotlinx.coroutines.flow.flow<String> {
            throw IOException("Test error")
        }
        
        try {
            throwingFlow.handleChatErrors().toList()
            fail("Expected exception to be thrown")
        } catch (e: ChatException.NetworkException) {
            assertTrue(e.message?.contains("Test error") == true)
        }
    }
}