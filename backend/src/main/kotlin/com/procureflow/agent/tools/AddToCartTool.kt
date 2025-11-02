package com.procureflow.agent.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.procureflow.dto.CartItemDto
import com.procureflow.service.CartService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function


@Description("Add an item to the shopping cart with a specified quantity")
class AddToCartTool(
    private val cartService: CartService
) : Function<AddToCartRequest, AddToCartResponse> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun apply(request: AddToCartRequest): AddToCartResponse {
        logger.info("AddToCartTool invoked with cartId: ${request.cartId}, itemId: ${request.itemId}, quantity: ${request.quantity}")
        return try {
            val cartItem = cartService.addToCart(
                cartId = request.cartId,
                itemId = request.itemId,
                quantity = request.quantity
            )
            logger.info("Item added to cart: ${cartItem.item.name} (qty: ${request.quantity}) for cartId: ${request.cartId}")
            AddToCartResponse(
                success = true,
                cartItem = cartItem,
                message = "Added ${request.quantity} x ${cartItem.item.name} to cart"
            )
        } catch (e: Exception) {
            logger.error("Error adding item to cart for cartId: ${request.cartId}, itemId: ${request.itemId}", e)
            throw e
        }
    }
}

@JsonClassDescription("Request to add an item to the shopping cart")
data class AddToCartRequest(
    @JsonPropertyDescription("Cart ID to add the item to (UUID)")
    val cartId: UUID,

    @JsonPropertyDescription("Item ID to add to the cart (UUID)")
    val itemId: UUID,

    @JsonPropertyDescription("Quantity to add (default is 1)")
    val quantity: Int = 1
)

data class AddToCartResponse(
    val success: Boolean,
    val cartItem: CartItemDto,
    val message: String
)
