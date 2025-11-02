package com.procureflow.agent.tools

import com.procureflow.domain.ItemStatus
import com.procureflow.dto.CartItemDto
import com.procureflow.dto.ItemDto
import com.procureflow.service.CartService
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
import kotlin.test.assertTrue

class AddToCartToolTest {

    private lateinit var cartService: CartService
    private lateinit var addToCartTool: AddToCartTool

    @BeforeEach
    fun setup() {
        cartService = mockk()
        addToCartTool = AddToCartTool(cartService)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should add item to cart`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val quantity = 3

        val cartItem = createCartItemDto(itemId, quantity)

        every { cartService.addToCart(cartId, itemId, quantity) } returns cartItem

        // When
        val result = addToCartTool.apply(
            AddToCartRequest(cartId = cartId, itemId = itemId, quantity = quantity)
        )

        // Then
        assertTrue(result.success)
        assertEquals(quantity, result.cartItem.quantity)
        assertEquals(itemId, result.cartItem.item.id)
        assertTrue(result.message.contains("3"))
        assertTrue(result.message.contains("Test Item"))
        verify(exactly = 1) { cartService.addToCart(cartId, itemId, quantity) }
    }

    @Test
    fun `should add single item with default quantity`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()

        val cartItem = createCartItemDto(itemId, 1)

        every { cartService.addToCart(cartId, itemId, 1) } returns cartItem

        // When
        val result = addToCartTool.apply(
            AddToCartRequest(cartId = cartId, itemId = itemId)
        )

        // Then
        assertTrue(result.success)
        assertEquals(1, result.cartItem.quantity)
    }

    @Test
    fun `should generate descriptive message`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val quantity = 5

        val cartItem = createCartItemDto(itemId, quantity, "Wireless Mouse")

        every { cartService.addToCart(cartId, itemId, quantity) } returns cartItem

        // When
        val result = addToCartTool.apply(
            AddToCartRequest(cartId = cartId, itemId = itemId, quantity = quantity)
        )

        // Then
        assertEquals("Added 5 x Wireless Mouse to cart", result.message)
    }

    @Test
    fun `should handle large quantities`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val quantity = 100

        val cartItem = createCartItemDto(itemId, quantity)

        every { cartService.addToCart(cartId, itemId, quantity) } returns cartItem

        // When
        val result = addToCartTool.apply(
            AddToCartRequest(cartId = cartId, itemId = itemId, quantity = quantity)
        )

        // Then
        assertTrue(result.success)
        assertEquals(100, result.cartItem.quantity)
    }

    @Test
    fun `should calculate subtotal correctly`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val quantity = 4
        val price = BigDecimal("25.50")
        val expectedSubtotal = BigDecimal("102.00")

        val itemDto = ItemDto(
            id = itemId,
            name = "Test Item",
            category = "Test",
            description = null,
            price = price,
            status = "ACTIVE",
            createdAt = Instant.now()
        )

        val cartItem = CartItemDto(
            id = UUID.randomUUID(),
            item = itemDto,
            quantity = quantity,
            subtotal = expectedSubtotal
        )

        every { cartService.addToCart(cartId, itemId, quantity) } returns cartItem

        // When
        val result = addToCartTool.apply(
            AddToCartRequest(cartId = cartId, itemId = itemId, quantity = quantity)
        )

        // Then
        assertEquals(expectedSubtotal, result.cartItem.subtotal)
    }

    private fun createCartItemDto(
        itemId: UUID,
        quantity: Int,
        itemName: String = "Test Item"
    ): CartItemDto {
        val price = BigDecimal("15.99")
        return CartItemDto(
            id = UUID.randomUUID(),
            item = ItemDto(
                id = itemId,
                name = itemName,
                category = "Test",
                description = null,
                price = price,
                status = ItemStatus.ACTIVE.name,
                createdAt = Instant.now()
            ),
            quantity = quantity,
            subtotal = price.multiply(BigDecimal(quantity))
        )
    }
}
