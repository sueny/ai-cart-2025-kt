package com.procureflow.domain

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CartTest {

    @Test
    fun `should create empty cart`() {
        // When
        val cart = Cart()

        // Then
        assertTrue(cart.items.isEmpty())
        assertNotNull(cart.createdAt)
        assertEquals(null, cart.userId)
    }

    @Test
    fun `should create cart with userId`() {
        // Given
        val userId = "user123"

        // When
        val cart = Cart(userId = userId)

        // Then
        assertEquals(userId, cart.userId)
    }

    @Test
    fun `should support adding items to cart`() {
        // Given
        val cart = Cart()
        val item = Item(
            id = UUID.randomUUID(),
            name = "Test Item",
            category = "Test",
            price = BigDecimal.TEN
        )
        val cartItem = CartItem(cart = cart, item = item, quantity = 2)

        // When
        cart.items.add(cartItem)

        // Then
        assertEquals(1, cart.items.size)
        assertEquals(cartItem, cart.items[0])
    }

    @Test
    fun `should calculate total for multiple items`() {
        // Given
        val cart = Cart()
        val item1 = Item(
            id = UUID.randomUUID(),
            name = "Item 1",
            category = "Test",
            price = BigDecimal("10.00")
        )
        val item2 = Item(
            id = UUID.randomUUID(),
            name = "Item 2",
            category = "Test",
            price = BigDecimal("20.00")
        )

        val cartItem1 = CartItem(cart = cart, item = item1, quantity = 2)
        val cartItem2 = CartItem(cart = cart, item = item2, quantity = 1)

        cart.items.add(cartItem1)
        cart.items.add(cartItem2)

        // When
        val total = cart.items.sumOf {
            it.item.price.multiply(BigDecimal(it.quantity))
        }

        // Then
        assertEquals(BigDecimal("40.00"), total)
    }
}
