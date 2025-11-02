package com.procureflow.controller

import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
import com.procureflow.dto.CreateItemRequest
import com.procureflow.integration.BaseIntegrationTest
import com.procureflow.repository.ItemRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@AutoConfigureWebTestClient
class ItemControllerIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @AfterEach
    fun cleanup() {
        itemRepository.deleteAll()
    }

    @Test
    fun `GET items should return all items`() {
        // Given
        itemRepository.save(Item(
            name = "Item 1",
            category = "Test",
            price = BigDecimal("10.00")
        ))
        itemRepository.save(Item(
            name = "Item 2",
            category = "Test",
            price = BigDecimal("20.00")
        ))

        // When/Then
        webTestClient.get()
            .uri("/api/v1/items")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].name").isNotEmpty
            .jsonPath("$[1].name").isNotEmpty
    }

    @Test
    fun `GET items search should filter by query`() {
        // Given
        itemRepository.save(Item(
            name = "USB-C Cable",
            category = "Electronics",
            price = BigDecimal("15.99")
        ))
        itemRepository.save(Item(
            name = "HDMI Cable",
            category = "Electronics",
            price = BigDecimal("12.99")
        ))

        // When/Then
        webTestClient.get()
            .uri("/api/v1/items/search?q=USB")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].name").isEqualTo("USB-C Cable")
    }

    @Test
    fun `GET items search should filter by category`() {
        // Given
        itemRepository.save(Item(
            name = "Laptop",
            category = "Electronics",
            price = BigDecimal("999.99")
        ))
        itemRepository.save(Item(
            name = "Chair",
            category = "Furniture",
            price = BigDecimal("299.99")
        ))

        // When/Then
        webTestClient.get()
            .uri("/api/v1/items/search?q=&category=Furniture")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].category").isEqualTo("Furniture")
    }

    @Test
    fun `GET item by id should return item`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("50.00")
        ))

        // When/Then
        webTestClient.get()
            .uri("/api/v1/items/${item.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(item.id.toString())
            .jsonPath("$.name").isEqualTo("Test Item")
            .jsonPath("$.price").isEqualTo(50.00)
    }

    @Test
    fun `GET item by id should return 404 when not found`() {
        // Given
        val nonExistentId = "00000000-0000-0000-0000-000000000000"

        // When/Then
        webTestClient.get()
            .uri("/api/v1/items/$nonExistentId")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `POST items should create new item`() {
        // Given
        val request = CreateItemRequest(
            name = "New Item",
            category = "Electronics",
            description = "Test description",
            price = BigDecimal("99.99")
        )

        // When/Then
        webTestClient.post()
            .uri("/api/v1/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.name").isEqualTo("New Item")
            .jsonPath("$.category").isEqualTo("Electronics")
            .jsonPath("$.price").isEqualTo(99.99)
            .jsonPath("$.status").isEqualTo("ACTIVE")
    }

    @Test
    fun `PUT items should update existing item`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Old Name",
            category = "Old Category",
            price = BigDecimal("10.00")
        ))

        val updateRequest = mapOf(
            "name" to "New Name",
            "price" to 20.00
        )

        // When/Then
        webTestClient.put()
            .uri("/api/v1/items/${item.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("New Name")
            .jsonPath("$.price").isEqualTo(20.00)
            .jsonPath("$.category").isEqualTo("Old Category")
    }

    @Test
    fun `DELETE items should delete item`() {
        // Given
        val item = itemRepository.save(Item(
            name = "To Delete",
            category = "Test",
            price = BigDecimal("10.00")
        ))

        // When/Then
        webTestClient.delete()
            .uri("/api/v1/items/${item.id}")
            .exchange()
            .expectStatus().isNoContent

        // Verify deletion
        webTestClient.get()
            .uri("/api/v1/items/${item.id}")
            .exchange()
            .expectStatus().isNotFound
    }
}
