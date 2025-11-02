package com.procureflow.integration

import io.mockk.every
import io.mockk.mockk
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for integration tests.
 * Uses Testcontainers to automatically spin up a PostgreSQL instance.
 * The container is shared across all test classes for better performance.
 * Requires Docker to be running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BaseIntegrationTest.MockConfig::class)
abstract class BaseIntegrationTest {

    @TestConfiguration
    class MockConfig {
        @Bean
        @Primary
        fun mockChatModel(): ChatModel {
            val mockModel = mockk<ChatModel>()
            every { mockModel.call(any<Prompt>()) } returns ChatResponse(
                listOf(Generation(AssistantMessage("Mock AI response")))
            )
            return mockModel
        }
    }

    companion object {
        // Singleton PostgreSQL container shared across all test classes
        private val postgres: PostgreSQLContainer<Nothing> by lazy {
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("procureflow_test")
                withUsername("test")
                withPassword("test")
                withReuse(true)
                withStartupTimeout(java.time.Duration.ofSeconds(60))
                withConnectTimeoutSeconds(30)
                start()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            // Ensure container is started and get connection details
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
            // Mock OpenAI API key to prevent autoconfiguration errors
            registry.add("spring.ai.openai.api-key") { "test-mock-api-key" }
        }
    }
}
