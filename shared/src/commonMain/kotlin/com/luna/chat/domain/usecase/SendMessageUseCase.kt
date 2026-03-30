package com.luna.chat.domain.usecase

import com.luna.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch

class SendMessageUseCase constructor(
    private val chatRepository: ChatRepository,
    private val contentFilterUseCase: ContentFilterUseCase,
) {
    suspend operator fun invoke(userMessage: String, conversationId: String): Flow<Result<String>> = flow {
        try {
            println("Luna:SendMsg: invoke called, msg='${userMessage.take(20)}', convId=$conversationId")
            val filteredInput = contentFilterUseCase.filterUserInput(userMessage)

            if (filteredInput.isFiltered) {
                emit(Result.failure(ContentFilterException(
                    filteredInput.childFriendlyMessage ?: "Let's talk about something else!"
                )))
                return@flow
            }

            chatRepository.sendMessage(filteredInput.content, conversationId)
                .catch { exception -> emit(Result.failure(exception)) }
                .collect { apiResult ->
                    apiResult.fold(
                        onSuccess = { aiResponse ->
                            val filteredResponse = contentFilterUseCase.filterAiResponse(aiResponse)
                            if (filteredResponse.isFiltered) {
                                emit(Result.success(
                                    filteredResponse.childFriendlyMessage
                                        ?: "I'd rather talk about something else!"
                                ))
                            } else {
                                emit(Result.success(filteredResponse.content))
                            }
                        },
                        onFailure = { exception -> emit(Result.failure(exception)) },
                    )
                }
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }
}

class ContentFilterException(message: String) : Exception(message)
