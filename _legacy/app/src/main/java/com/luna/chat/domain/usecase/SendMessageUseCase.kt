package com.luna.chat.domain.usecase

import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val contentFilterUseCase: ContentFilterUseCase
) {
    
    suspend operator fun invoke(userMessage: String): Flow<Result<ChatMessage>> = flow {
        try {
            // Pre-filter user input for inappropriate content
            val filteredInput = contentFilterUseCase.filterUserInput(userMessage)
            
            if (filteredInput.isFiltered) {
                emit(Result.failure(ContentFilterException("Let's talk about something else! What would you like to learn today? 📚")))
                return@flow
            }
            
            // Create user message
            val userChatMessage = ChatMessage.create(
                content = filteredInput.content,
                isFromUser = true,
                status = MessageStatus.SENDING
            )
            
            emit(Result.success(userChatMessage))
            
            // Send message to API and get response
            chatRepository.sendMessage(filteredInput.content)
                .catch { exception ->
                    emit(Result.failure(exception))
                }
                .collect { apiResult ->
                    apiResult.fold(
                        onSuccess = { aiResponse ->
                            // Post-filter AI response for child safety
                            val filteredResponse = contentFilterUseCase.filterAiResponse(aiResponse)
                            
                            val aiMessage = if (filteredResponse.isFiltered) {
                                ChatMessage.create(
                                    content = "I'd rather talk about something else! What's your favorite subject in school? 🎓",
                                    isFromUser = false,
                                    status = MessageStatus.DELIVERED
                                )
                            } else {
                                ChatMessage.create(
                                    content = filteredResponse.content,
                                    isFromUser = false,
                                    status = MessageStatus.DELIVERED
                                )
                            }
                            
                            emit(Result.success(aiMessage))
                        },
                        onFailure = { exception ->
                            emit(Result.failure(exception))
                        }
                    )
                }
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }
}

class ContentFilterException(message: String) : Exception(message)