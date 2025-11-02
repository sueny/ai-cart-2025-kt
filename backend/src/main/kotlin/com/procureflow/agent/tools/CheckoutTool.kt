package com.procureflow.agent.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.procureflow.dto.OrderDto
import com.procureflow.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function


@Description("Complete checkout and create an order from the cart")
class CheckoutTool(
    private val orderService: OrderService
) : Function<CheckoutRequest, CheckoutResponse> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun apply(request: CheckoutRequest): CheckoutResponse {
        logger.info("CheckoutTool invoked with cartId: ${request.cartId}")
        return try {
            val order = orderService.checkout(request.cartId)
            logger.info("Checkout successful for cartId: ${request.cartId}, orderId: ${order.id}")
            CheckoutResponse(
                success = true,
                order = order,
                message = "Order ${order.id} confirmed! Total: \$${order.total}"
            )
        } catch (e: Exception) {
            logger.error("Error during checkout for cartId: ${request.cartId}", e)
            throw e
        }
    }
}

@JsonClassDescription("Request to complete checkout for a cart")
data class CheckoutRequest(
    @JsonPropertyDescription("Cart ID to checkout (UUID)")
    val cartId: UUID
)

data class CheckoutResponse(
    val success: Boolean,
    val order: OrderDto,
    val message: String
)
