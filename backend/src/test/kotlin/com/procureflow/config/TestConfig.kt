package com.procureflow.config

import io.mockk.every
import io.mockk.mockk
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Test configuration that provides mock beans for Spring AI components.
 * This prevents integration tests from requiring actual OpenAI API keys.
 */
@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun mockChatModel(): ChatModel {
        val mockModel = mockk<ChatModel>()

        // Default mock behavior - returns a simple response
        every { mockModel.call(any<Prompt>()) } returns ChatResponse(
            listOf(
                Generation(AssistantMessage("Mock AI response for testing purposes"))
            )
        )

        return mockModel
    }
}
