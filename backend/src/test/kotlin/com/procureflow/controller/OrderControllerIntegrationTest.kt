package com.procureflow.controller

import com.procureflow.domain.Cart
import com.procureflow.domain.CartItem
import com.procureflow.domain.Item
import com.procureflow.integration.BaseIntegrationTest
import com.procureflow.repository.CartItemRepository
import com.procureflow.repository.CartRepository
import com.procureflow.repository.ItemRepository
import com.procureflow.repository.OrderRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@AutoConfigureWebTestClient
class OrderControllerIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @AfterEach
    fun cleanup() {
        orderRepository.deleteAll()
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        itemRepository.deleteAll()
    }

    @Test
    fun `POST orders should create order from cart`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("50.00")
        ))
        val cart = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 2))

        val request = mapOf("cartId" to cart.id.toString())

        // When/Then
        webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.cartId").isEqualTo(cart.id.toString())
            .jsonPath("$.total").isEqualTo(100.00)
            .jsonPath("$.status").isEqualTo("CONFIRMED")
            .jsonPath("$.items.length()").isEqualTo(1)
    }

    @Test
    fun `POST orders should fail with empty cart`() {
        // Given
        val cart = cartRepository.save(Cart())
        val request = mapOf("cartId" to cart.id.toString())

        // When/Then
        webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is5xxServerError
    }

    @Test
    fun `GET order should return order details`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("25.00")
        ))
        val cart = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart, item = item, quantity = 4))

        val createRequest = mapOf("cartId" to cart.id.toString())
        val createResponse = webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .returnResult()

        val orderId = createResponse.toString().split("\"id\":\"")[1].split("\"")[0]

        // When/Then
        webTestClient.get()
            .uri("/api/v1/orders/$orderId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(orderId)
            .jsonPath("$.total").isEqualTo(100.00)
    }

    @Test
    fun `GET all orders should return all orders`() {
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

        val cart1 = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart1, item = item1, quantity = 1))

        val cart2 = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart2, item = item2, quantity = 2))

        // Create two orders
        webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("cartId" to cart1.id.toString()))
            .exchange()
            .expectStatus().isCreated

        webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("cartId" to cart2.id.toString()))
            .exchange()
            .expectStatus().isCreated

        // When/Then
        webTestClient.get()
            .uri("/api/v1/orders")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `POST orders should calculate correct total for multiple items`() {
        // Given
        val item1 = itemRepository.save(Item(
            name = "Item 1",
            category = "Test",
            price = BigDecimal("15.00")
        ))
        val item2 = itemRepository.save(Item(
            name = "Item 2",
            category = "Test",
            price = BigDecimal("25.50")
        ))

        val cart = cartRepository.save(Cart())
        cartItemRepository.save(CartItem(cart = cart, item = item1, quantity = 3))
        cartItemRepository.save(CartItem(cart = cart, item = item2, quantity = 2))

        val request = mapOf("cartId" to cart.id.toString())

        // Expected: (15.00 * 3) + (25.50 * 2) = 45.00 + 51.00 = 96.00

        // When/Then
        webTestClient.post()
            .uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.total").isEqualTo(96.00)
            .jsonPath("$.items.length()").isEqualTo(2)
    }
}
