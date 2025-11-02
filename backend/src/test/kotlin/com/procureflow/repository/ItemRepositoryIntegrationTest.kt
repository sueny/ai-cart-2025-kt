package com.procureflow.repository

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
import kotlin.test.assertTrue

/**
 * Integration tests for ItemRepository using Testcontainers.
 * Extends BaseIntegrationTest which provides @SpringBootTest and PostgreSQL container setup.
 */
@Transactional
class ItemRepositoryIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @AfterEach
    fun cleanup() {
        itemRepository.deleteAll()
    }

    @Test
    fun `should save and retrieve item`() {
        // Given
        val item = Item(
            name = "Test Laptop",
            category = "Electronics",
            description = "High-performance laptop",
            price = BigDecimal("1299.99"),
            status = ItemStatus.ACTIVE
        )

        // When
        val savedItem = itemRepository.save(item)
        val retrievedItem = itemRepository.findById(savedItem.id!!).orElse(null)

        // Then
        assertNotNull(retrievedItem)
        assertEquals(item.name, retrievedItem.name)
        assertEquals(item.category, retrievedItem.category)
        assertEquals(0, item.price.compareTo(retrievedItem.price))
        assertEquals(item.status, retrievedItem.status)
    }

    @Test
    fun `should find items by name containing ignore case`() {
        // Given
        itemRepository.save(Item(
            name = "USB-C Cable",
            category = "Electronics",
            price = BigDecimal("15.99")
        ))
        itemRepository.save(Item(
            name = "USB-A Cable",
            category = "Electronics",
            price = BigDecimal("9.99")
        ))
        itemRepository.save(Item(
            name = "HDMI Cable",
            category = "Electronics",
            price = BigDecimal("12.99")
        ))

        // When
        val results = itemRepository.findByNameContainingIgnoreCase("usb")

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.name.contains("USB", ignoreCase = true) })
    }

    @Test
    fun `should find items by category`() {
        // Given
        itemRepository.save(Item(
            name = "Laptop",
            category = "Electronics",
            price = BigDecimal("999.99")
        ))
        itemRepository.save(Item(
            name = "Office Chair",
            category = "Furniture",
            price = BigDecimal("299.99")
        ))
        itemRepository.save(Item(
            name = "Desk",
            category = "Furniture",
            price = BigDecimal("449.99")
        ))

        // When
        val furnitureItems = itemRepository.findByCategory("Furniture")

        // Then
        assertEquals(2, furnitureItems.size)
        assertTrue(furnitureItems.all { it.category == "Furniture" })
    }

    @Test
    fun `should find items by name and category`() {
        // Given
        itemRepository.save(Item(
            name = "Wireless Keyboard",
            category = "Electronics",
            price = BigDecimal("79.99")
        ))
        itemRepository.save(Item(
            name = "Mechanical Keyboard",
            category = "Electronics",
            price = BigDecimal("129.99")
        ))
        itemRepository.save(Item(
            name = "Wireless Mouse",
            category = "Electronics",
            price = BigDecimal("49.99")
        ))
        itemRepository.save(Item(
            name = "Wireless Headphones",
            category = "Audio",
            price = BigDecimal("199.99")
        ))

        // When
        val results = itemRepository.findByNameContainingIgnoreCaseAndCategory("wireless", "Electronics")

        // Then
        assertEquals(2, results.size)
        assertTrue(results.all { it.category == "Electronics" })
        assertTrue(results.all { it.name.contains("Wireless", ignoreCase = true) })
    }

    @Test
    fun `should find items by status`() {
        // Given
        itemRepository.save(Item(
            name = "Active Item",
            category = "Test",
            price = BigDecimal("10.00"),
            status = ItemStatus.ACTIVE
        ))
        itemRepository.save(Item(
            name = "Out of Stock Item",
            category = "Test",
            price = BigDecimal("20.00"),
            status = ItemStatus.OUT_OF_STOCK
        ))
        itemRepository.save(Item(
            name = "Inactive Item",
            category = "Test",
            price = BigDecimal("30.00"),
            status = ItemStatus.INACTIVE
        ))

        // When
        val activeItems = itemRepository.findByStatus(ItemStatus.ACTIVE)

        // Then
        assertEquals(1, activeItems.size)
        assertEquals(ItemStatus.ACTIVE, activeItems[0].status)
    }

    @Test
    fun `should update item status`() {
        // Given
        val item = itemRepository.save(Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal("50.00"),
            status = ItemStatus.ACTIVE
        ))

        // When
        val updatedItem = item.copy(status = ItemStatus.OUT_OF_STOCK)
        itemRepository.save(updatedItem)
        val retrieved = itemRepository.findById(item.id!!).orElse(null)

        // Then
        assertEquals(ItemStatus.OUT_OF_STOCK, retrieved?.status)
    }

    @Test
    fun `should delete item`() {
        // Given
        val item = itemRepository.save(Item(
            name = "To Delete",
            category = "Test",
            price = BigDecimal("10.00")
        ))

        // When
        itemRepository.deleteById(item.id!!)
        val retrieved = itemRepository.findById(item.id!!).orElse(null)

        // Then
        assertEquals(null, retrieved)
    }

    @Test
    fun `should handle price precision correctly`() {
        // Given
        val item = Item(
            name = "Precision Test",
            category = "Test",
            price = BigDecimal("999.99")
        )

        // When
        val saved = itemRepository.save(item)
        val retrieved = itemRepository.findById(saved.id!!).orElse(null)

        // Then
        assertNotNull(retrieved)
        assertEquals(0, item.price.compareTo(retrieved.price))
    }
}
