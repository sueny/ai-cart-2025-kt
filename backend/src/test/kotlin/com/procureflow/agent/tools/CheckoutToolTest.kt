package com.procureflow.agent.tools

import com.procureflow.domain.ItemStatus
import com.procureflow.dto.CartItemDto
import com.procureflow.dto.ItemDto
import com.procureflow.dto.OrderDto
import com.procureflow.service.OrderService
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

class CheckoutToolTest {

    private lateinit var orderService: OrderService
    private lateinit var checkoutTool: CheckoutTool

    @BeforeEach
    fun setup() {
        orderService = mockk()
        checkoutTool = CheckoutTool(orderService)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should complete checkout successfully`() {
        // Given
        val cartId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val total = BigDecimal("159.99")

        val order = createOrderDto(orderId, cartId, total)

        every { orderService.checkout(cartId) } returns order

        // When
        val result = checkoutTool.apply(CheckoutRequest(cartId = cartId))

        // Then
        assertTrue(result.success)
        assertEquals(order, result.order)
        assertTrue(result.message.contains(orderId.toString()))
        assertTrue(result.message.contains("159.99"))
        verify(exactly = 1) { orderService.checkout(cartId) }
    }

    @Test
    fun `should generate confirmation message with order details`() {
        // Given
        val cartId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val total = BigDecimal("99.99")

        val order = createOrderDto(orderId, cartId, total)

        every { orderService.checkout(cartId) } returns order

        // When
        val result = checkoutTool.apply(CheckoutRequest(cartId = cartId))

        // Then
        assertEquals("Order $orderId confirmed! Total: \$$total", result.message)
    }

    @Test
    fun `should handle large order totals`() {
        // Given
        val cartId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val total = BigDecimal("9999.99")

        val order = createOrderDto(orderId, cartId, total)

        every { orderService.checkout(cartId) } returns order

        // When
        val result = checkoutTool.apply(CheckoutRequest(cartId = cartId))

        // Then
        assertTrue(result.success)
        assertEquals(total, result.order.total)
    }

    @Test
    fun `should handle order with multiple items`() {
        // Given
        val cartId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val total = BigDecimal("250.00")

        val items = listOf(
            createCartItemDto("Item 1", BigDecimal("100.00"), 1),
            createCartItemDto("Item 2", BigDecimal("50.00"), 2),
            createCartItemDto("Item 3", BigDecimal("25.00"), 2)
        )

        val order = OrderDto(
            id = orderId,
            cartId = cartId,
            items = items,
            total = total,
            status = "CONFIRMED",
            createdAt = Instant.now()
        )

        every { orderService.checkout(cartId) } returns order

        // When
        val result = checkoutTool.apply(CheckoutRequest(cartId = cartId))

        // Then
        assertTrue(result.success)
        assertEquals(3, result.order.items.size)
        assertEquals(total, result.order.total)
    }

    @Test
    fun `should preserve order status`() {
        // Given
        val cartId = UUID.randomUUID()
        val orderId = UUID.randomUUID()

        val order = createOrderDto(orderId, cartId, BigDecimal("50.00"))

        every { orderService.checkout(cartId) } returns order

        // When
        val result = checkoutTool.apply(CheckoutRequest(cartId = cartId))

        // Then
        assertEquals("CONFIRMED", result.order.status)
    }

    private fun createOrderDto(
        orderId: UUID,
        cartId: UUID,
        total: BigDecimal
    ) = OrderDto(
        id = orderId,
        cartId = cartId,
        items = listOf(
            createCartItemDto("Test Item", BigDecimal("15.99"), 10)
        ),
        total = total,
        status = "CONFIRMED",
        createdAt = Instant.now()
    )

    private fun createCartItemDto(
        name: String,
        price: BigDecimal,
        quantity: Int
    ) = CartItemDto(
        id = UUID.randomUUID(),
        item = ItemDto(
            id = UUID.randomUUID(),
            name = name,
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
