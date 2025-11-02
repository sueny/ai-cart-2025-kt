package com.procureflow.service

import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
import com.procureflow.dto.CreateItemRequest
import com.procureflow.repository.ItemRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CatalogServiceTest {

    private lateinit var itemRepository: ItemRepository
    private lateinit var catalogService: CatalogService

    @BeforeEach
    fun setup() {
        itemRepository = mockk()
        catalogService = CatalogService(itemRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `searchItems should return matching items without category filter`() {
        // Given
        val query = "USB"
        val item1 = createTestItem("USB-C Cable")
        val item2 = createTestItem("USB-A Cable")

        every { itemRepository.findByNameContainingIgnoreCase(query) } returns listOf(item1, item2)

        // When
        val results = catalogService.searchItems(query)

        // Then
        assertEquals(2, results.size)
        assertEquals("USB-C Cable", results[0].name)
        assertEquals("USB-A Cable", results[1].name)
        verify(exactly = 1) { itemRepository.findByNameContainingIgnoreCase(query) }
    }

    @Test
    fun `searchItems should filter by category when provided`() {
        // Given
        val query = "cable"
        val category = "Electronics"
        val item = createTestItem("USB Cable", category)

        every {
            itemRepository.findByNameContainingIgnoreCaseAndCategory(query, category)
        } returns listOf(item)

        // When
        val results = catalogService.searchItems(query, category)

        // Then
        assertEquals(1, results.size)
        assertEquals(category, results[0].category)
        verify(exactly = 1) {
            itemRepository.findByNameContainingIgnoreCaseAndCategory(query, category)
        }
    }

    @Test
    fun `searchItems should return empty list when no items found`() {
        // Given
        val query = "nonexistent"
        every { itemRepository.findByNameContainingIgnoreCase(query) } returns emptyList()

        // When
        val results = catalogService.searchItems(query)

        // Then
        assertEquals(0, results.size)
    }

    @Test
    fun `getAllItems should return all items from repository`() {
        // Given
        val items = listOf(
            createTestItem("Item 1"),
            createTestItem("Item 2"),
            createTestItem("Item 3")
        )
        every { itemRepository.findAll() } returns items

        // When
        val results = catalogService.getAllItems()

        // Then
        assertEquals(3, results.size)
        verify(exactly = 1) { itemRepository.findAll() }
    }

    @Test
    fun `getItemById should return item when exists`() {
        // Given
        val itemId = UUID.randomUUID()
        val item = createTestItem("Test Item").copy(id = itemId)

        every { itemRepository.findById(itemId) } returns Optional.of(item)

        // When
        val result = catalogService.getItemById(itemId)

        // Then
        assertNotNull(result)
        assertEquals(itemId, result.id)
        assertEquals("Test Item", result.name)
    }

    @Test
    fun `getItemById should return null when item not found`() {
        // Given
        val itemId = UUID.randomUUID()
        every { itemRepository.findById(itemId) } returns Optional.empty()

        // When
        val result = catalogService.getItemById(itemId)

        // Then
        assertNull(result)
    }

    @Test
    fun `registerItem should create new item`() {
        // Given
        val request = CreateItemRequest(
            name = "New Item",
            category = "Test",
            description = "Test description",
            price = BigDecimal("50.00"),
            status = "ACTIVE"
        )
        val savedItem = createTestItem("New Item", "Test", BigDecimal("50.00"))

        every { itemRepository.save(any()) } returns savedItem

        // When
        val result = catalogService.registerItem(request)

        // Then
        assertNotNull(result)
        assertEquals("New Item", result.name)
        assertEquals("Test", result.category)
        assertEquals(BigDecimal("50.00"), result.price)
        verify(exactly = 1) { itemRepository.save(any()) }
    }

    @Test
    fun `updateItem should update existing item`() {
        // Given
        val itemId = UUID.randomUUID()
        val existingItem = createTestItem("Old Name", "Old Category", BigDecimal.TEN).copy(id = itemId)
        val updatedItem = existingItem.copy(
            name = "New Name",
            price = BigDecimal.valueOf(20),
            updatedAt = java.time.Instant.now()
        )

        every { itemRepository.findById(itemId) } returns Optional.of(existingItem)
        every { itemRepository.save(any()) } returns updatedItem

        // When
        val result = catalogService.updateItem(
            itemId,
            name = "New Name",
            category = null,
            description = null,
            price = BigDecimal.valueOf(20),
            status = null
        )

        // Then
        assertNotNull(result)
        assertEquals("New Name", result.name)
        assertEquals(BigDecimal.valueOf(20), result.price)
        verify(exactly = 1) { itemRepository.findById(itemId) }
        verify(exactly = 1) { itemRepository.save(any()) }
    }

    @Test
    fun `updateItem should return null when item not found`() {
        // Given
        val itemId = UUID.randomUUID()
        every { itemRepository.findById(itemId) } returns Optional.empty()

        // When
        val result = catalogService.updateItem(
            itemId,
            name = "New Name",
            category = null,
            description = null,
            price = null,
            status = null
        )

        // Then
        assertNull(result)
        verify(exactly = 1) { itemRepository.findById(itemId) }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `deleteItem should call repository delete`() {
        // Given
        val itemId = UUID.randomUUID()
        every { itemRepository.deleteById(itemId) } just Runs

        // When
        catalogService.deleteItem(itemId)

        // Then
        verify(exactly = 1) { itemRepository.deleteById(itemId) }
    }

    private fun createTestItem(
        name: String,
        category: String = "Test Category",
        price: BigDecimal = BigDecimal("10.00")
    ) = Item(
        id = UUID.randomUUID(),
        name = name,
        category = category,
        price = price,
        status = ItemStatus.ACTIVE
    )
}
