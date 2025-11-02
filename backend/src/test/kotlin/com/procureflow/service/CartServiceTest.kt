package com.procureflow.service

import com.procureflow.domain.Cart
import com.procureflow.domain.CartItem
import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
import com.procureflow.repository.CartItemRepository
import com.procureflow.repository.CartRepository
import com.procureflow.repository.ItemRepository
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

class CartServiceTest {

    private lateinit var cartRepository: CartRepository
    private lateinit var cartItemRepository: CartItemRepository
    private lateinit var itemRepository: ItemRepository
    private lateinit var cartService: CartService

    @BeforeEach
    fun setup() {
        cartRepository = mockk()
        cartItemRepository = mockk()
        itemRepository = mockk()
        cartService = CartService(cartRepository, cartItemRepository, itemRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `createCart should create new cart without userId`() {
        // Given
        val cart = Cart(id = UUID.randomUUID())
        every { cartRepository.save(any()) } returns cart
        every { cartItemRepository.findByCartId(any()) } returns emptyList()

        // When
        val result = cartService.createCart()

        // Then
        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(0, result.items.size)
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `createCart should create cart with userId`() {
        // Given
        val userId = "user123"
        val cart = Cart(id = UUID.randomUUID(), userId = userId)
        every { cartRepository.save(any()) } returns cart
        every { cartItemRepository.findByCartId(any()) } returns emptyList()

        // When
        val result = cartService.createCart(userId)

        // Then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `getCart should return cart with items`() {
        // Given
        val cartId = UUID.randomUUID()
        val cart = Cart(id = cartId)
        val item = createTestItem()
        val cartItem = CartItem(id = UUID.randomUUID(), cart = cart, item = item, quantity = 2)

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { cartItemRepository.findByCartId(cartId) } returns listOf(cartItem)

        // When
        val result = cartService.getCart(cartId)

        // Then
        assertNotNull(result)
        assertEquals(cartId, result.id)
        assertEquals(1, result.items.size)
        assertEquals(2, result.items[0].quantity)
    }

    @Test
    fun `getCart should return null when cart not found`() {
        // Given
        val cartId = UUID.randomUUID()
        every { cartRepository.findById(cartId) } returns Optional.empty()

        // When
        val result = cartService.getCart(cartId)

        // Then
        assertNull(result)
    }

    @Test
    fun `addToCart should add new item to cart`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val quantity = 3

        val cart = Cart(id = cartId)
        val item = createTestItem(id = itemId)
        val cartItem = CartItem(id = UUID.randomUUID(), cart = cart, item = item, quantity = quantity)

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { itemRepository.findById(itemId) } returns Optional.of(item)
        every { cartItemRepository.findByCartIdAndItemId(cartId, itemId) } returns null
        every { cartItemRepository.save(any()) } returns cartItem

        // When
        val result = cartService.addToCart(cartId, itemId, quantity)

        // Then
        assertNotNull(result)
        assertEquals(quantity, result.quantity)
        assertEquals(itemId, result.item.id)
        verify(exactly = 1) { cartItemRepository.save(any()) }
    }

    @Test
    fun `addToCart should update quantity when item already in cart`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val existingQuantity = 2
        val addQuantity = 3

        val cart = Cart(id = cartId)
        val item = createTestItem(id = itemId)
        val existingCartItem = CartItem(
            id = UUID.randomUUID(),
            cart = cart,
            item = item,
            quantity = existingQuantity
        )
        val updatedCartItem = existingCartItem.copy(quantity = existingQuantity + addQuantity)

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { itemRepository.findById(itemId) } returns Optional.of(item)
        every { cartItemRepository.findByCartIdAndItemId(cartId, itemId) } returns existingCartItem
        every { cartItemRepository.save(any()) } returns updatedCartItem

        // When
        val result = cartService.addToCart(cartId, itemId, addQuantity)

        // Then
        assertNotNull(result)
        assertEquals(5, result.quantity)
        verify(exactly = 1) { cartItemRepository.save(any()) }
    }

    @Test
    fun `addToCart should throw exception when cart not found`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()

        every { cartRepository.findById(cartId) } returns Optional.empty()

        // When/Then
        assertThrows<IllegalArgumentException> {
            cartService.addToCart(cartId, itemId, 1)
        }
    }

    @Test
    fun `addToCart should throw exception when item not found`() {
        // Given
        val cartId = UUID.randomUUID()
        val itemId = UUID.randomUUID()
        val cart = Cart(id = cartId)

        every { cartRepository.findById(cartId) } returns Optional.of(cart)
        every { itemRepository.findById(itemId) } returns Optional.empty()

        // When/Then
        assertThrows<IllegalArgumentException> {
            cartService.addToCart(cartId, itemId, 1)
        }
    }

    @Test
    fun `updateCartItemQuantity should update quantity`() {
        // Given
        val cartItemId = UUID.randomUUID()
        val newQuantity = 5
        val cartItem = CartItem(
            id = cartItemId,
            cart = mockk(relaxed = true),
            item = createTestItem(),
            quantity = 2
        )
        val updatedCartItem = cartItem.copy(quantity = newQuantity)

        every { cartItemRepository.findById(cartItemId) } returns Optional.of(cartItem)
        every { cartItemRepository.save(any()) } returns updatedCartItem

        // When
        val result = cartService.updateCartItemQuantity(cartItemId, newQuantity)

        // Then
        assertNotNull(result)
        assertEquals(newQuantity, result.quantity)
    }

    @Test
    fun `updateCartItemQuantity should delete item when quantity is zero or negative`() {
        // Given
        val cartItemId = UUID.randomUUID()
        val cartItem = CartItem(
            id = cartItemId,
            cart = mockk(relaxed = true),
            item = createTestItem(),
            quantity = 2
        )

        every { cartItemRepository.findById(cartItemId) } returns Optional.of(cartItem)
        every { cartItemRepository.delete(cartItem) } just Runs

        // When
        val result = cartService.updateCartItemQuantity(cartItemId, 0)

        // Then
        assertNull(result)
        verify(exactly = 1) { cartItemRepository.delete(cartItem) }
        verify(exactly = 0) { cartItemRepository.save(any()) }
    }

    @Test
    fun `removeFromCart should delete cart item`() {
        // Given
        val cartItemId = UUID.randomUUID()
        every { cartItemRepository.deleteById(cartItemId) } just Runs

        // When
        cartService.removeFromCart(cartItemId)

        // Then
        verify(exactly = 1) { cartItemRepository.deleteById(cartItemId) }
    }

    @Test
    fun `clearCart should delete all items from cart`() {
        // Given
        val cartId = UUID.randomUUID()
        every { cartItemRepository.deleteByCartId(cartId) } just Runs

        // When
        cartService.clearCart(cartId)

        // Then
        verify(exactly = 1) { cartItemRepository.deleteByCartId(cartId) }
    }

    private fun createTestItem(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Item",
        price: BigDecimal = BigDecimal("10.00")
    ) = Item(
        id = id,
        name = name,
        category = "Test",
        price = price,
        status = ItemStatus.ACTIVE
    )
}
