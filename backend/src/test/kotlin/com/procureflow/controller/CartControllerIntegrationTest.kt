package com.procureflow.controller

import com.procureflow.domain.Cart
import com.procureflow.domain.CartItem
import com.procureflow.domain.Item
import com.procureflow.integration.BaseIntegrationTest
import com.procureflow.repository.CartItemRepository
import com.procureflow.repository.CartRepository
import com.procureflow.repository.ItemRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@AutoConfigureWebTestClient
class CartControllerIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @AfterEach
    fun cleanup() {
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        itemRepository.deleteAll()
    }

    @Test
    fun `POST carts should create new cart`() {
        // When/Then
        webTestClient.post()
            .uri("/api/v1/carts")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.items").isEmpty
            .jsonPath("$.total").isEqualTo(0)
    }

    @Test
    fun `POST carts with userId should create cart with user`() {
        // When/Then
        webTestClient.post()
            .uri("/api/v1/carts?userId=user123")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.userId").isEqualTo("user123")
    }

    @Test
    fun `GET cart should return cart with items`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("15.99")
        ))
        val cart = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 2))

        // When/Then
        webTestClient.get()
            .uri("/api/v1/carts/${cart.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(cart.id.toString())
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].quantity").isEqualTo(2)
            .jsonPath("$.total").isEqualTo(31.98)
    }

    @Test
    fun `POST cart items should add item to cart`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("25.00")
        ))
        val cart = cartRepository.save(Cart())

        val request = mapOf(
            "itemId" to item.id.toString(),
            "quantity" to 3
        )

        // When/Then
        webTestClient.post()
            .uri("/api/v1/carts/${cart.id}/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.quantity").isEqualTo(3)
            .jsonPath("$.item.id").isEqualTo(item.id.toString())
            .jsonPath("$.subtotal").isEqualTo(75.00)
    }

    @Test
    fun `PUT cart items should update quantity`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("10.00")
        ))
        val cart = cartRepository.save(Cart())
        val cartItem = cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 2))

        val updateRequest = mapOf("quantity" to 5)

        // When/Then
        webTestClient.put()
            .uri("/api/v1/carts/${cart.id}/items/${cartItem.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.quantity").isEqualTo(5)
            .jsonPath("$.subtotal").isEqualTo(50.00)
    }

    @Test
    fun `DELETE cart items should remove item from cart`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("10.00")
        ))
        val cart = cartRepository.save(Cart())
        val cartItem = cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 1))

        // When/Then
        webTestClient.delete()
            .uri("/api/v1/carts/${cart.id}/items/${cartItem.id}")
            .exchange()
            .expectStatus().isNoContent

        // Verify deletion
        val cartItems = cartItemRepository.findByCartId(cart.id!!)
        assert(cartItems.isEmpty())
    }

    @Test
    fun `DELETE cart should clear all items`() {
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
        cartItemRepository.save(CartItem(cart = cart, item = item1, quantity = 1))
        cartItemRepository.save(CartItem(cart = cart, item = item2, quantity = 2))

        // When/Then
        webTestClient.delete()
            .uri("/api/v1/carts/${cart.id}")
            .exchange()
            .expectStatus().isNoContent

        // Verify all items cleared
        val cartItems = cartItemRepository.findByCartId(cart.id!!)
        assert(cartItems.isEmpty())
    }
}
