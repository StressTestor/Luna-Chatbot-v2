package com.luna.chat.data.remote.dto

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VisionChatDtoTest {

    private val gson: Gson = GsonBuilder().create()

    @Test
    fun gson_roundTrip_request_and_response() {
        // Build a sample request with data URL image
        val dataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB" // truncated sample
        val request = VisionChatRequest(
            model = "test/vision-model",
            messages = listOf(
                Message(
                    role = "user",
                    content = listOf(
                        ContentPart.TextPart(text = "Describe this image briefly."),
                        ContentPart.ImageUrlPart(imageUrl = ImageUrl(url = dataUrl))
                    )
                )
            ),
            temperature = 0.5f,
            maxTokens = 64,
            topP = 0.9f,
            presencePenalty = 0.0f,
            frequencyPenalty = 0.0f
        )

        val json = gson.toJson(request)
        assertNotNull(json)

        val parsed = gson.fromJson(json, VisionChatRequest::class.java)
        assertEquals("test/vision-model", parsed.model)
        assertEquals(1, parsed.messages.size)
        val parts = parsed.messages[0].content
        assertEquals(2, parts.size)
        val text = parts[0] as ContentPart.TextPart
        val img = parts[1] as ContentPart.ImageUrlPart
        assertEquals("Describe this image briefly.", text.text)
        assertEquals(dataUrl, img.imageUrl.url)

        // Build a sample response with a simple choice
        val response = VisionChatResponse(
            id = "resp_123",
            created = 1234567890L,
            model = "test/vision-model",
            choices = listOf(
                Choice(
                    index = 0,
                    message = AssistantMessage(
                        role = "assistant",
                        content = "A small cat sitting on a windowsill."
                    ),
                    finishReason = "stop"
                )
            ),
            usage = Usage(promptTokens = 50, completionTokens = 20, totalTokens = 70)
        )

        val responseJson = gson.toJson(response)
        assertNotNull(responseJson)

        val parsedResp = gson.fromJson(responseJson, VisionChatResponse::class.java)
        assertEquals("resp_123", parsedResp.id)
        assertEquals("test/vision-model", parsedResp.model)
        assertEquals(1, parsedResp.choices.size)
        assertEquals("A small cat sitting on a windowsill.", parsedResp.choices[0].message.content)
        assertEquals(70, parsedResp.usage?.totalTokens)
    }
}