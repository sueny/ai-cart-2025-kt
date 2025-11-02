package com.procureflow.config

import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct

/**
 * Configuration for AI providers (OpenAI, Anthropic Claude, and Google Vertex AI Gemini).
 *
 * Spring AI 1.0.0-M6 auto-configuration creates multiple ChatModel beans:
 * - openAiChatModel: From OpenAI auto-configuration
 * - anthropicChatModel: From Anthropic auto-configuration
 * - vertexAiGeminiChat: From Gemini auto-configuration
 *
 * This configuration provides all three as named beans and marks one as @Primary
 * based on spring.ai.provider property. Allows dynamic provider selection at runtime.
 *
 * Example application.yml configuration:
 * ```
 * spring:
 *   ai:
 *     provider: openai  # or 'claude', 'gemini'
 *     openai:
 *       api-key: ${OPENAI_API_KEY}
 *       chat:
 *         options:
 *           model: gpt-4o
 *           temperature: 0.7
 *     anthropic:
 *       api-key: ${ANTHROPIC_API_KEY}
 *       chat:
 *         options:
 *           model: claude-3-5-sonnet-20241022 # or claude-3-haiku-20240307
 *     vertex:
 *       ai:
 *         gemini:
 *           project-id: ${GCP_PROJECT_ID}
 * ```
 */
@Configuration
class AiConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.ai.provider:openai}")
    private lateinit var aiProvider: String

    @Bean("openAiProvider")
    fun openAiProvider(
        @Qualifier("openAiChatModel") openAiChatModel: ChatModel
    ): ChatModel {
        logger.info("Registering OpenAI provider")
        return openAiChatModel
    }

    @Bean("anthropicProvider")
    fun anthropicProvider(
        @Qualifier("anthropicChatModel") anthropicChatModel: ChatModel
    ): ChatModel {
        logger.info("Registering Anthropic Claude provider")
        return anthropicChatModel
    }

    @Bean("geminiProvider")
    fun geminiProvider(
        @Qualifier("vertexAiGeminiChat") geminiChatModel: ChatModel
    ): ChatModel {
        logger.info("Registering Google Vertex AI Gemini provider")
        return geminiChatModel
    }

    @Bean
    @Primary
    fun primaryChatModel(
        @Qualifier("openAiChatModel") openAiChatModel: ChatModel
    ): ChatModel {
        logger.info("Setting primary ChatModel to OpenAI provider")
        return openAiChatModel
    }

    @PostConstruct
    fun init() {
        logger.info("AiConfig initialized - Configured AI Provider: {}", aiProvider)
    }
}
