package com.procureflow.agent.tools

import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
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

class SearchItemsToolTest {

    private lateinit var catalogService: CatalogService
    private lateinit var searchItemsTool: SearchItemsTool

    @BeforeEach
    fun setup() {
        catalogService = mockk()
        searchItemsTool = SearchItemsTool(catalogService)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should search items by query only`() {
        // Given
        val query = "laptop"
        val items = listOf(
            createItemDto("Gaming Laptop"),
            createItemDto("Business Laptop")
        )

        every { catalogService.searchItems(query, null) } returns items

        // When
        val result = searchItemsTool.apply(SearchItemsRequest(query = query))

        // Then
        assertEquals(2, result.items.size)
        assertEquals("Gaming Laptop", result.items[0].name)
        assertEquals("Business Laptop", result.items[1].name)
        verify(exactly = 1) { catalogService.searchItems(query, null) }
    }

    @Test
    fun `should search items by query and category`() {
        // Given
        val query = "keyboard"
        val category = "Electronics"
        val items = listOf(createItemDto("Mechanical Keyboard", category))

        every { catalogService.searchItems(query, category) } returns items

        // When
        val result = searchItemsTool.apply(
            SearchItemsRequest(query = query, category = category)
        )

        // Then
        assertEquals(1, result.items.size)
        assertEquals(category, result.items[0].category)
        verify(exactly = 1) { catalogService.searchItems(query, category) }
    }

    @Test
    fun `should return empty list when no items found`() {
        // Given
        val query = "nonexistent"
        every { catalogService.searchItems(query, null) } returns emptyList()

        // When
        val result = searchItemsTool.apply(SearchItemsRequest(query = query))

        // Then
        assertEquals(0, result.items.size)
    }

    @Test
    fun `should handle case-insensitive search`() {
        // Given
        val query = "USB"
        val items = listOf(
            createItemDto("USB-C Cable"),
            createItemDto("usb-a cable")
        )

        every { catalogService.searchItems(query, null) } returns items

        // When
        val result = searchItemsTool.apply(SearchItemsRequest(query = query))

        // Then
        assertEquals(2, result.items.size)
    }

    private fun createItemDto(
        name: String,
        category: String = "Test Category"
    ) = ItemDto(
        id = UUID.randomUUID(),
        name = name,
        category = category,
        description = "Test description",
        price = BigDecimal("99.99"),
        status = ItemStatus.ACTIVE.name,
        createdAt = Instant.now()
    )
}
