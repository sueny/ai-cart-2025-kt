package com.procureflow.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ItemTest {

    @Test
    fun `should create item with valid data`() {
        // Given
        val name = "USB-C Cable"
        val category = "Electronics"
        val price = BigDecimal("15.99")

        // When
        val item = Item(
            name = name,
            category = category,
            description = "Premium USB-C cable",
            price = price,
            status = ItemStatus.ACTIVE
        )

        // Then
        assertNotNull(item)
        assertEquals(name, item.name)
        assertEquals(category, item.category)
        assertEquals(price, item.price)
        assertEquals(ItemStatus.ACTIVE, item.status)
        assertNotNull(item.createdAt)
        assertNotNull(item.updatedAt)
    }

    @Test
    fun `should have default status as ACTIVE`() {
        // When
        val item = Item(
            name = "Test Item",
            category = "Test",
            price = BigDecimal.TEN
        )

        // Then
        assertEquals(ItemStatus.ACTIVE, item.status)
    }

    @Test
    fun `should handle price with precision`() {
        // Given
        val price = BigDecimal("999.99")

        // When
        val item = Item(
            name = "Expensive Item",
            category = "Premium",
            price = price
        )

        // Then
        assertEquals(0, price.compareTo(item.price))
    }

    @Test
    fun `should support all ItemStatus values`() {
        // Given
        val statuses = listOf(ItemStatus.ACTIVE, ItemStatus.INACTIVE, ItemStatus.OUT_OF_STOCK)

        // When/Then
        statuses.forEach { status ->
            val item = Item(
                name = "Test Item",
                category = "Test",
                price = BigDecimal.ONE,
                status = status
            )
            assertEquals(status, item.status)
        }
    }

    @Test
    fun `should create item copy with updated values`() {
        // Given
        val original = Item(
            name = "Original",
            category = "Test",
            price = BigDecimal.TEN,
            status = ItemStatus.ACTIVE
        )

        // When
        val updated = original.copy(
            name = "Updated",
            price = BigDecimal.valueOf(20)
        )

        // Then
        assertEquals("Updated", updated.name)
        assertEquals(BigDecimal.valueOf(20), updated.price)
        assertEquals(original.category, updated.category)
        assertEquals(original.status, updated.status)
    }

    @Test
    fun `should handle null description`() {
        // When
        val item = Item(
            name = "No Description",
            category = "Test",
            price = BigDecimal.ONE,
            description = null
        )

        // Then
        assertEquals(null, item.description)
    }

    @Test
    fun `should set timestamps on creation`() {
        // Given
        val before = java.time.Instant.now()

        // When
        val item = Item(
            name = "Test",
            category = "Test",
            price = BigDecimal.ONE
        )

        // Then
        val after = java.time.Instant.now()
        assertTrue(item.createdAt >= before && item.createdAt <= after)
        assertTrue(item.updatedAt >= before && item.updatedAt <= after)
    }
}
