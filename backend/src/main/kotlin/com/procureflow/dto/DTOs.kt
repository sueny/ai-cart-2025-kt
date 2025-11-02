package com.procureflow.dto

import com.procureflow.domain.ItemStatus
import com.procureflow.domain.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.*

// Item DTOs
data class ItemDto(
    val id: UUID?,
    val name: String,
    val category: String,
    val description: String?,
    val price: BigDecimal,
    val status: String,
    val createdAt: Instant
)

data class CreateItemRequest(
    val name: String,
    val category: String,
    val description: String? = null,
    val price: BigDecimal,
    val status: String = "ACTIVE"
)

data class UpdateItemRequest(
    val name: String?,
    val category: String?,
    val description: String?,
    val price: BigDecimal?,
    val status: String?
)

// Cart DTOs
data class CartDto(
    val id: UUID?,
    val userId: String?,
    val items: List<CartItemDto>,
    val total: BigDecimal,
    val createdAt: Instant
)

data class CartItemDto(
    val id: UUID?,
    val item: ItemDto,
    val quantity: Int,
    val subtotal: BigDecimal
)

data class AddToCartRequest(
    val itemId: UUID,
    val quantity: Int = 1
)

data class UpdateCartItemRequest(
    val quantity: Int
)

// Order DTOs
data class OrderDto(
    val id: UUID?,
    val cartId: UUID?,
    val items: List<CartItemDto>,
    val total: BigDecimal,
    val status: String,
    val createdAt: Instant
)

data class CreateOrderRequest(
    val cartId: UUID
)

// Agent DTOs
data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatRequest(
    val messages: List<ChatMessage>,
    val cartId: UUID?
)

data class ChatResponse(
    val response: String,
    val cartId: UUID?
)
