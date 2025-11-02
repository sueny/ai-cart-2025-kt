package com.procureflow.service

import com.procureflow.domain.Cart
import com.procureflow.domain.CartItem
import com.procureflow.dto.CartDto
import com.procureflow.dto.CartItemDto
import com.procureflow.dto.ItemDto
import com.procureflow.repository.CartItemRepository
import com.procureflow.repository.CartRepository
import com.procureflow.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
@Transactional
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val itemRepository: ItemRepository
) {

    fun createCart(userId: String? = null): CartDto {
        val cart = Cart(userId = userId)
        val saved = cartRepository.save(cart)
        return saved.toDto()
    }

    fun getCart(cartId: UUID): CartDto? {
        val cart = cartRepository.findById(cartId).orElse(null) ?: return null
        return cart.toDto()
    }

    fun addToCart(cartId: UUID, itemId: UUID, quantity: Int): CartItemDto {
        val cart = cartRepository.findById(cartId).orElseThrow {
            IllegalArgumentException("Cart not found: $cartId")
        }

        val item = itemRepository.findById(itemId).orElseThrow {
            IllegalArgumentException("Item not found: $itemId")
        }

        // Check if item already in cart
        val existingCartItem = cartItemRepository.findByCartIdAndItemId(cartId, itemId)

        val cartItem = if (existingCartItem != null) {
            // Update quantity
            val updated = existingCartItem.copy(quantity = existingCartItem.quantity + quantity)
            cartItemRepository.save(updated)
        } else {
            // Add new item
            val newCartItem = CartItem(
                cart = cart,
                item = item,
                quantity = quantity
            )
            cartItemRepository.save(newCartItem)
        }

        return cartItem.toDto()
    }

    fun updateCartItemQuantity(cartItemId: UUID, quantity: Int): CartItemDto? {
        val cartItem = cartItemRepository.findById(cartItemId).orElse(null) ?: return null

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem)
            return null
        }

        val updated = cartItem.copy(quantity = quantity)
        return cartItemRepository.save(updated).toDto()
    }

    fun removeFromCart(cartItemId: UUID) {
        cartItemRepository.deleteById(cartItemId)
    }

    fun clearCart(cartId: UUID) {
        cartItemRepository.deleteByCartId(cartId)
    }

    private fun Cart.toDto(): CartDto {
        val cartItems = cartItemRepository.findByCartId(this.id!!)
        val total = cartItems.sumOf { it.item.price.multiply(BigDecimal(it.quantity)) }

        return CartDto(
            id = id,
            userId = userId,
            items = cartItems.map { it.toDto() },
            total = total,
            createdAt = createdAt
        )
    }

    private fun CartItem.toDto() = CartItemDto(
        id = id,
        item = ItemDto(
            id = item.id,
            name = item.name,
            category = item.category,
            description = item.description,
            price = item.price,
            status = item.status.name,
            createdAt = item.createdAt
        ),
        quantity = quantity,
        subtotal = item.price.multiply(BigDecimal(quantity))
    )
}
