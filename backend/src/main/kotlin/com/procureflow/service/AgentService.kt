package com.procureflow.service

import com.procureflow.agent.tools.SearchItemsTool
import com.procureflow.agent.tools.RegisterItemTool
import com.procureflow.agent.tools.AddToCartTool
import com.procureflow.agent.tools.CheckoutTool
import com.procureflow.dto.ChatMessage
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class AgentService(
    @Qualifier("openAiProvider")
    private val openAiChatModel: ChatModel,
    @Qualifier("anthropicProvider")
    private val anthropicChatModel: ChatModel,
    @Qualifier("geminiProvider")
    private val geminiChatModel: ChatModel,
    private val searchItemsTool: SearchItemsTool,
    private val registerItemTool: RegisterItemTool,
    private val addToCartTool: AddToCartTool,
    private val checkoutTool: CheckoutTool,
    @Value("\${spring.ai.provider:openai}") private val aiProvider: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val chatModel: ChatModel by lazy {
        when (aiProvider.lowercase()) {
            "claude", "anthropic" -> {
                logger.info("Using Anthropic Claude as AI provider")
                anthropicChatModel
            }
            "gemini" -> {
                logger.info("Using Google Vertex AI Gemini as AI provider")
                geminiChatModel
            }
            else -> {
                logger.info("Using OpenAI as AI provider")
                openAiChatModel
            }
        }
    }

    private val systemPrompt = """
        You are ProcureBot, an AI procurement assistant helping users search for and purchase items.

        Your capabilities:
        1. Search for items in the catalog by name or keyword
        2. Register new items when not found in the catalog
        3. Add items to the user's cart with specified quantities
        4. Complete checkout and create orders

        Guidelines:
        - Be concise, helpful, and professional
        - When searching, show relevant results and ask which ones to add
        - Always confirm before adding expensive items (>$500) or large quantities (>10)
        - If an item is not found, offer to register it with appropriate details
        - Ask clarifying questions when the user's intent is unclear
        - Provide clear summaries after each action
        - Always mention the cart ID when adding items

        Example interactions:
        - User: "I need 10 USB-C cables"
          → Search for USB-C cables, show results, confirm quantity and price, add to cart
        - User: "I need ergonomic keyboards"
          → Search, show options with prices, ask which one or how many
        - User: "Add standing desk"
          → Search for standing desks, if not found offer to register it
    """.trimIndent()

    fun chat(messages: List<ChatMessage>, cartId: UUID?): String {
        logger.info("Agent chat invoked with ${messages.size} messages, cartId: $cartId, aiProvider: $aiProvider")
        val chatMessages = mutableListOf<org.springframework.ai.chat.messages.Message>()

        // Combine system prompt with cart context into a single system message
        val fullSystemPrompt = if (cartId != null) {
            "$systemPrompt\n\nThe user's cart ID is: $cartId. Use this cart ID when adding items."
        } else {
            systemPrompt
        }
        chatMessages.add(SystemMessage(fullSystemPrompt))

        // Add conversation history
        messages.forEach { msg ->
            when (msg.role.lowercase()) {
                "user" -> {
                    chatMessages.add(UserMessage(msg.content))
                    logger.debug("Added user message: ${msg.content}")
                }
                "assistant" -> {
                    chatMessages.add(
                        org.springframework.ai.chat.messages.AssistantMessage(msg.content)
                    )
                    logger.debug("Added assistant message: ${msg.content}")
                }
            }
        }

        try {
            logger.info("Calling ChatClient with ${chatMessages.size} messages and 4 tools (search, register, addToCart, checkout)")

            // Create ChatClient with tools for this request
            val chatClientWithTools = ChatClient.builder(chatModel)
                .defaultTools(
                    searchItemsTool,
                    registerItemTool,
                    addToCartTool,
                    checkoutTool
                )
                .build()

            val response = chatClientWithTools
                .prompt()
                .messages(chatMessages)
                .call()
                .content()

            logger.info("Chat completed successfully with response length: ${response?.length ?: 0}")
            return response ?: "I apologize, but I couldn't process that request. Please try again."
        } catch (e: Exception) {
            logger.error("Error during agent chat", e)
            throw e
        }
    }
}
