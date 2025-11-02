package com.procureflow.agent.tools

import com.procureflow.domain.ItemStatus
import com.procureflow.dto.CreateItemRequest
import com.procureflow.dto.ItemDto
import com.procureflow.service.CatalogService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RegisterItemToolTest {

    private lateinit var catalogService: CatalogService
    private lateinit var registerItemTool: RegisterItemTool

    @BeforeEach
    fun setup() {
        catalogService = mockk()
        registerItemTool = RegisterItemTool(catalogService)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should register new item with all fields`() {
        // Given
        val request = RegisterItemRequest(
            name = "Custom Ergonomic Chair",
            category = "Furniture",
            description = "Adjustable office chair",
            price = BigDecimal("450.00"),
            status = "ACTIVE"
        )

        val registeredItem = ItemDto(
            id = UUID.randomUUID(),
            name = request.name,
            category = request.category,
            description = request.description,
            price = request.price,
            status = ItemStatus.ACTIVE.name,
            createdAt = Instant.now()
        )

        every { catalogService.registerItem(any()) } returns registeredItem

        // When
        val result = registerItemTool.apply(request)

        // Then
        assertNotNull(result.item)
        assertEquals(request.name, result.item.name)
        assertEquals(request.category, result.item.category)
        assertEquals(request.description, result.item.description)
        assertEquals(request.price, result.item.price)
        assertEquals("Item registered successfully", result.message)
        verify(exactly = 1) { catalogService.registerItem(any()) }
    }

    @Test
    fun `should register item with default status`() {
        // Given
        val request = RegisterItemRequest(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("10.00")
        )

        val registeredItem = ItemDto(
            id = UUID.randomUUID(),
            name = request.name,
            category = request.category,
            description = request.description,
            price = request.price,
            status = "ACTIVE",
            createdAt = Instant.now()
        )

        every { catalogService.registerItem(any()) } returns registeredItem

        // When
        val result = registerItemTool.apply(request)

        // Then
        assertEquals("ACTIVE", result.item.status)
    }

    @Test
    fun `should register item without description`() {
        // Given
        val request = RegisterItemRequest(
            name = "Simple Item",
            category = "Test",
            description = null,
            price = BigDecimal("25.00")
        )

        val registeredItem = ItemDto(
            id = UUID.randomUUID(),
            name = request.name,
            category = request.category,
            description = null,
            price = request.price,
            status = "ACTIVE",
            createdAt = Instant.now()
        )

        every { catalogService.registerItem(any()) } returns registeredItem

        // When
        val result = registerItemTool.apply(request)

        // Then
        assertEquals(null, result.item.description)
    }

    @Test
    fun `should handle different item statuses`() {
        // Given
        val statuses = listOf("ACTIVE", "INACTIVE", "OUT_OF_STOCK")

        statuses.forEach { status ->
            val request = RegisterItemRequest(
                name = "Test Item",
                category = "Test",
                price = BigDecimal("10.00"),
                status = status
            )

            val registeredItem = ItemDto(
                id = UUID.randomUUID(),
                name = request.name,
                category = request.category,
                description = null,
                price = request.price,
                status = status,
                createdAt = Instant.now()
            )

            every { catalogService.registerItem(any()) } returns registeredItem

            // When
            val result = registerItemTool.apply(request)

            // Then
            assertEquals(status, result.item.status)
        }
    }

    @Test
    fun `should pass correct CreateItemRequest to service`() {
        // Given
        val request = RegisterItemRequest(
            name = "Monitored Item",
            category = "Electronics",
            description = "Test",
            price = BigDecimal("99.99"),
            status = "ACTIVE"
        )

        val registeredItem = ItemDto(
            id = UUID.randomUUID(),
            name = request.name,
            category = request.category,
            description = request.description,
            price = request.price,
            status = "ACTIVE",
            createdAt = Instant.now()
        )

        var capturedRequest: CreateItemRequest? = null
        every { catalogService.registerItem(any()) } answers {
            capturedRequest = firstArg()
            registeredItem
        }

        // When
        registerItemTool.apply(request)

        // Then
        assertNotNull(capturedRequest)
        assertEquals(request.name, capturedRequest!!.name)
        assertEquals(request.category, capturedRequest!!.category)
        assertEquals(request.description, capturedRequest!!.description)
        assertEquals(request.price, capturedRequest!!.price)
        assertEquals(request.status, capturedRequest!!.status)
    }
}
