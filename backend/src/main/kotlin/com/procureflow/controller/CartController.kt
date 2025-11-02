package com.procureflow.controller

import com.procureflow.dto.AddToCartRequest
import com.procureflow.dto.CartDto
import com.procureflow.dto.CartItemDto
import com.procureflow.dto.UpdateCartItemRequest
import com.procureflow.service.CartService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/carts")
@CrossOrigin(origins = ["*"])
class CartController(
    private val cartService: CartService
) {

    @PostMapping
    fun createCart(@RequestParam(required = false) userId: String?): ResponseEntity<CartDto> {
        val cart = cartService.createCart(userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(cart)
    }

    @GetMapping("/{id}")
    fun getCart(@PathVariable id: UUID): ResponseEntity<CartDto> {
        val cart = cartService.getCart(id)
        return if (cart != null) {
            ResponseEntity.ok(cart)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/items")
    fun addToCart(
        @PathVariable id: UUID,
        @RequestBody request: AddToCartRequest
    ): ResponseEntity<CartItemDto> {
        val cartItem = cartService.addToCart(id, request.itemId, request.quantity)
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItem)
    }

    @PutMapping("/{cartId}/items/{itemId}")
    fun updateCartItem(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<CartItemDto> {
        val updated = cartService.updateCartItemQuantity(itemId, request.quantity)
        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    fun removeFromCart(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID
    ): ResponseEntity<Unit> {
        cartService.removeFromCart(itemId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun clearCart(@PathVariable id: UUID): ResponseEntity<Unit> {
        cartService.clearCart(id)
        return ResponseEntity.noContent().build()
    }
}
