package com.procureflow.service

import com.procureflow.domain.Order
import com.procureflow.domain.OrderStatus
import com.procureflow.dto.CartItemDto
import com.procureflow.dto.ItemDto
import com.procureflow.dto.OrderDto
import com.procureflow.repository.CartItemRepository
import com.procureflow.repository.CartRepository
import com.procureflow.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository
) {

    fun checkout(cartId: UUID): OrderDto {
        val cart = cartRepository.findById(cartId).orElseThrow {
            IllegalArgumentException("Cart not found: $cartId")
        }

        val cartItems = cartItemRepository.findByCartId(cartId)
        if (cartItems.isEmpty()) {
            throw IllegalArgumentException("Cannot checkout empty cart")
        }

        val total = cartItems.sumOf { it.item.price.multiply(BigDecimal(it.quantity)) }

        val order = Order(
            cart = cart,
            total = total,
            status = OrderStatus.CONFIRMED
        )

        val savedOrder = orderRepository.save(order)
        return savedOrder.toDto()
    }

    fun getOrder(orderId: UUID): OrderDto? {
        return orderRepository.findById(orderId).map { it.toDto() }.orElse(null)
    }

    fun getAllOrders(): List<OrderDto> {
        return orderRepository.findAll().map { it.toDto() }
    }

    private fun Order.toDto(): OrderDto {
        val cartItems = cartItemRepository.findByCartId(cart.id!!)

        return OrderDto(
            id = id,
            cartId = cart.id,
            items = cartItems.map { cartItem ->
                CartItemDto(
                    id = cartItem.id,
                    item = ItemDto(
                        id = cartItem.item.id,
                        name = cartItem.item.name,
                        category = cartItem.item.category,
                        description = cartItem.item.description,
                        price = cartItem.item.price,
                        status = cartItem.item.status.name,
                        createdAt = cartItem.item.createdAt
                    ),
                    quantity = cartItem.quantity,
                    subtotal = cartItem.item.price.multiply(BigDecimal(cartItem.quantity))
                )
            },
            total = total,
            status = status.name,
            createdAt = createdAt
        )
    }
}
