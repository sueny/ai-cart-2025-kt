package com.procureflow.repository

import com.procureflow.domain.Cart
import com.procureflow.domain.CartItem
import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
import com.procureflow.integration.BaseIntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for CartRepository using Testcontainers.
 * Extends BaseIntegrationTest which provides @SpringBootTest and PostgreSQL container setup.
 */
@Transactional
class CartRepositoryIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @Autowired
    private lateinit var entityManager: jakarta.persistence.EntityManager

    @AfterEach
    fun cleanup() {
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        itemRepository.deleteAll()
    }

    @Test
    fun `should save and retrieve cart`() {
        // Given
        val cart = Cart(userId = "user123")

        // When
        val saved = cartRepository.save(cart)
        val retrieved = cartRepository.findById(saved.id!!).orElse(null)

        // Then
        assertNotNull(retrieved)
        assertEquals("user123", retrieved.userId)
    }

    @Test
    fun `should save cart with items`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("25.00")
        ))

        val cart = cartRepository.save(Cart())

        // When
        val cartItem = cartItemRepository.save(CartItem(
            cart = cart,
            item = item,
            quantity = 3
        ))

        // Then
        assertNotNull(cartItem.id)
        assertEquals(3, cartItem.quantity)
        assertEquals(item.id, cartItem.item.id)
    }

    @Test
    fun `should find cart by userId`() {
        // Given
        val userId = "user456"
        cartRepository.save(Cart(userId = userId))
        cartRepository.save(Cart(userId = userId))
        cartRepository.save(Cart(userId = "otherUser"))

        // When
        val userCarts = cartRepository.findByUserId(userId)

        // Then
        assertEquals(2, userCarts.size)
        userCarts.forEach { assertEquals(userId, it.userId) }
    }

    @Test
    fun `should find cart items by cartId`() {
        // Given
        val item1 = itemRepository.save(Item(
            name = "Item 1",
            category = "Test",
            price = BigDecimal("10.00")
        ))
        val item2 = itemRepository.save(Item(
            name = "Item 2",
            category = "Test",
            price = BigDecimal("20.00")
        ))

        val cart = cartRepository.save(Cart())

        cartItemRepository.save(CartItem(cart = cart, item = item1, quantity = 2))
        cartItemRepository.save(CartItem(cart = cart, item = item2, quantity = 1))

        // When
        val cartItems = cartItemRepository.findByCartId(cart.id!!)

        // Then
        assertEquals(2, cartItems.size)
    }

    @Test
    fun `should find cart item by cartId and itemId`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("15.00")
        ))
        val cart = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 5))

        // When
        val found = cartItemRepository.findByCartIdAndItemId(cart.id!!, item.id!!)

        // Then
        assertNotNull(found)
        assertEquals(5, found.quantity)
    }

    @Test
    fun `should cascade delete cart items when cart is deleted`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("10.00")
        ))
        val cart = cartRepository.save(Cart())
        val cartItem = cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 1))
        entityManager.flush()
        entityManager.clear()

        // When
        cartRepository.delete(cart)
        entityManager.flush()
        entityManager.clear()

        // Then
        val retrievedCartItem = cartItemRepository.findById(cartItem.id!!).orElse(null)
        assertEquals(null, retrievedCartItem)
    }

    @Test
    fun `should delete all cart items by cartId`() {
        // Given
        val item1 = itemRepository.save(Item(
            name = "Item 1",
            category = "Test",
            price = BigDecimal("10.00")
        ))
        val item2 = itemRepository.save(Item(
            name = "Item 2",
            category = "Test",
            price = BigDecimal("20.00")
        ))
        val cart = cartRepository.save(Cart())

        cartItemRepository.save(CartItem(cart = cart, item = item1, quantity = 2))
        cartItemRepository.save(CartItem(cart = cart, item = item2, quantity = 3))

        // When
        cartItemRepository.deleteByCartId(cart.id!!)
        val remainingItems = cartItemRepository.findByCartId(cart.id!!)

        // Then
        assertEquals(0, remainingItems.size)
    }
}
