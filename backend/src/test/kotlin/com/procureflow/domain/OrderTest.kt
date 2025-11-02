package com.procureflow.domain

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderTest {

    @Test
    fun `should create order with cart and total`() {
        // Given
        val cart = Cart(id = UUID.randomUUID())
        val total = BigDecimal("100.00")

        // When
        val order = Order(
            cart = cart,
            total = total,
            status = OrderStatus.CONFIRMED
        )

        // Then
        assertNotNull(order)
        assertEquals(cart, order.cart)
        assertEquals(total, order.total)
        assertEquals(OrderStatus.CONFIRMED, order.status)
        assertNotNull(order.createdAt)
    }

    @Test
    fun `should have default status as CONFIRMED`() {
        // Given
        val cart = Cart(id = UUID.randomUUID())

        // When
        val order = Order(
            cart = cart,
            total = BigDecimal.TEN
        )

        // Then
        assertEquals(OrderStatus.CONFIRMED, order.status)
    }

    @Test
    fun `should support all OrderStatus values`() {
        // Given
        val cart = Cart(id = UUID.randomUUID())
        val statuses = listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.CANCELLED)

        // When/Then
        statuses.forEach { status ->
            val order = Order(
                cart = cart,
                total = BigDecimal.ONE,
                status = status
            )
            assertEquals(status, order.status)
        }
    }
}
