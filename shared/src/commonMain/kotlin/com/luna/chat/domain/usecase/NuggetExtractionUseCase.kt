package com.luna.chat.domain.usecase

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.UserPreferences
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.hrr.NuggetShelf
import com.luna.chat.security.PiiRedactor
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Extracts persistent facts from a completed conversation and stores them
 * in the HRR nugget shelf.
 *
 * Called at session end (when the user starts a new chat). Sends the conversation
 * to the LLM with an extraction prompt, parses the JSON response into
 * topic/key/value triples, and binds them into the appropriate NuggetStore.
 */
class NuggetExtractionUseCase(
    private val apiClient: LunaApiClient,
    private val apiKeyProvider: ApiKeyProvider,
    private val nuggetShelf: NuggetShelf,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val piiRedactor: PiiRedactor,
    private val contentFilter: ContentFilterUseCase,
) {

    @Serializable
    data class ExtractedFact(
        @SerialName("topic") val topic: String,
        @SerialName("key") val key: String,
        @SerialName("value") val value: String,
    )

    /**
     * Extract facts from conversation messages and store in the nugget shelf.
     * Silently returns on any failure — extraction is best-effort.
     */
    suspend fun extractAndStore(messages: List<ChatMessage>) {
        if (messages.size < 4) return // too short to extract meaningful facts

        val apiKey = apiKeyProvider.getApiKey() ?: return
        val selectedModel = userPreferencesRepository.userPreferencesFlow.first().selectedModel

        val conversationText = messages.takeLast(10).joinToString("\n") { msg ->
            val role = if (msg.isFromUser) "User" else "Luna"
            val scrubbed = piiRedactor.redact(msg.content.take(500))
            "$role: $scrubbed"
        }.take(6000) // stay within MAX_CONTENT_LENGTH with room for the prompt

        val extractionPrompt = """Analyze this conversation and extract persistent facts about the user.
Return a JSON array of objects with "topic", "key", and "value" fields.

Topics: personal, interests, school, style, context
- personal: name, pets, family details
- interests: hobbies, favorites, things they like/dislike
- school: classes, subjects, homework, grades
- style: communication preferences, how they like responses
- context: current projects, events, plans

Rules:
- Only extract factual, persistent information. Skip ephemeral details.
- Keys should be short descriptive labels (e.g. "favorite_color", "pet_name")
- Values should be concise (1-5 words)
- If nothing new is learned, return an empty array []
- Maximum 5 facts per conversation

Example output:
[{"topic":"personal","key":"pet_name","value":"Mochi the cat"},{"topic":"interests","key":"favorite_subject","value":"science"}]

Conversation:
$conversationText

Return ONLY the JSON array, no other text."""

        try {
            val request = GroqChatRequest.create(
                messages = listOf(
                    GroqMessage.createSystemMessage("You are a fact extraction assistant. Return only valid JSON."),
                    GroqMessage.createUserMessage(extractionPrompt),
                ),
                model = selectedModel,
                maxTokens = 500,
            )

            val response = apiClient.sendChatMessage(apiKey, request)
            val rawJson = response.getAssistantMessage() ?: return

            // Parse the JSON, stripping any markdown code fences the LLM might add
            val cleanJson = rawJson
                .replace("```json", "").replace("```", "")
                .trim()

            val facts = Json { ignoreUnknownKeys = true }
                .decodeFromString<List<ExtractedFact>>(cleanJson)

            for (fact in facts) {
                val topic = fact.topic.lowercase().trim()
                val key = fact.key.trim().take(50).replace("\n", " ")
                val value = fact.value.trim().take(100).replace("\n", " ")
                if (key.isEmpty() || value.isEmpty()) continue
                if (topic !in NuggetShelf.Topics.ALL) continue
                // Extracted facts are later injected into the system prompt as
                // trusted context. Reject anything that looks like an attempt
                // to smuggle instructions into that channel.
                if (contentFilter.isJailbreakLike(key) || contentFilter.isJailbreakLike(value)) {
                    println("Luna:NuggetExtraction: dropped suspicious fact ($topic/$key)")
                    continue
                }
                nuggetShelf.remember(topic, key, value)
            }
        } catch (e: Exception) {
            println("Luna:NuggetExtraction: ${e::class.simpleName}: ${e.message}")
        }
    }
}
