package com.procureflow.service

import com.procureflow.domain.*
import com.procureflow.repository.CartItemRepository
import com.procureflow.repository.CartRepository
import com.procureflow.repository.OrderRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OrderServiceTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var cartRepository: CartRepository
    private lateinit var cartItemRepository: CartItemRepository
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setup() {
        orderRepository = mockk()
        cartRepository = mockk()
        cartItemRepository = mockk()
        orderService = OrderService(orderRepository, cartRepository, cartItemRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `checkout should create order from cart`() {
        // Given
        val cartId = UUID.randomUUID()
        val cart = Cart(id = cartId)
        val item1 = createTestItem(price = BigDecimal("10.00"))
        val item2 = createTestItem(price = BigDecimal("20.00"))
        val cartItem1 = CartItem(
            id = UUID.randomUUID(),
            cart = cart,
            item = item1,
            quantity = 2
        )
        val cartItem2 = CartItem(
            id = UUID.randomUUID(),
            cart = cart,
            item = item2,
            quantity = 1
        )
        val expectedTotal = BigDecimal("40.00")

        val order = Order(
            id = UUID.randomUUID(),
            cart = cart,
            total = expectedTotal,
            status = OrderStatus.CONFIRMED
        )

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { cartItemRepository.findByCartId(cartId) } returns listOf(cartItem1, cartItem2)
        every { orderRepository.save(any()) } returns order

        // When
        val result = orderService.checkout(cartId)

        // Then
        assertNotNull(result)
        assertEquals(expectedTotal, result.total)
        assertEquals(OrderStatus.CONFIRMED.name, result.status)
        assertEquals(2, result.items.size)
        verify(exactly = 1) { orderRepository.save(any()) }
    }

    @Test
    fun `checkout should throw exception when cart not found`() {
        // Given
        val cartId = UUID.randomUUID()
        every { cartRepository.findById(cartId) } returns Optional.empty()

        // When/Then
        assertThrows<IllegalArgumentException> {
            orderService.checkout(cartId)
        }
    }

    @Test
    fun `checkout should throw exception when cart is empty`() {
        // Given
        val cartId = UUID.randomUUID()
        val cart = Cart(id = cartId)

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { cartItemRepository.findByCartId(cartId) } returns emptyList()

        // When/Then
        assertThrows<IllegalArgumentException> {
            orderService.checkout(cartId)
        }
    }

    @Test
    fun `getOrder should return order when exists`() {
        // Given
        val orderId = UUID.randomUUID()
        val cartId = UUID.randomUUID()
        val cart = Cart(id = cartId)
        val order = Order(
            id = orderId,
            cart = cart,
            total = BigDecimal("100.00"),
            status = OrderStatus.CONFIRMED
        )

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { cartItemRepository.findByCartId(cartId) } returns emptyList()

        // When
        val result = orderService.getOrder(orderId)

        // Then
        assertNotNull(result)
        assertEquals(orderId, result.id)
        assertEquals(BigDecimal("100.00"), result.total)
    }

    @Test
    fun `getOrder should return null when order not found`() {
        // Given
        val orderId = UUID.randomUUID()
        every { orderRepository.findById(orderId) } returns Optional.empty()

        // When
        val result = orderService.getOrder(orderId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getAllOrders should return all orders`() {
        // Given
        val cart1 = Cart(id = UUID.randomUUID())
        val cart2 = Cart(id = UUID.randomUUID())
        val order1 = Order(
            id = UUID.randomUUID(),
            cart = cart1,
            total = BigDecimal("50.00"),
            status = OrderStatus.CONFIRMED
        )
        val order2 = Order(
            id = UUID.randomUUID(),
            cart = cart2,
            total = BigDecimal("75.00"),
            status = OrderStatus.PENDING
        )

        every { orderRepository.findAll() } returns listOf(order1, order2)
        every { cartItemRepository.findByCartId(any()) } returns emptyList()

        // When
        val results = orderService.getAllOrders()

        // Then
        assertEquals(2, results.size)
        verify(exactly = 1) { orderRepository.findAll() }
    }

    @Test
    fun `checkout should calculate correct total for single item`() {
        // Given
        val cartId = UUID.randomUUID()
        val cart = Cart(id = cartId)
        val item = createTestItem(price = BigDecimal("99.99"))
        val cartItem = CartItem(
            id = UUID.randomUUID(),
            cart = cart,
            item = item,
            quantity = 3
        )
        val expectedTotal = BigDecimal("299.97")

        val order = Order(
            id = UUID.randomUUID(),
            cart = cart,
            total = expectedTotal,
            status = OrderStatus.CONFIRMED
        )

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { cartItemRepository.findByCartId(cartId) } returns listOf(cartItem)
        every { orderRepository.save(any()) } returns order

        // When
        val result = orderService.checkout(cartId)

        // Then
        assertEquals(expectedTotal, result.total)
    }

    private fun createTestItem(
        id: UUID = UUID.randomUUID(),
        price: BigDecimal = BigDecimal("10.00")
    ) = Item(
        id = id,
        name = "Test Item",
        category = "Test",
        price = price,
        status = ItemStatus.ACTIVE
    )
}
