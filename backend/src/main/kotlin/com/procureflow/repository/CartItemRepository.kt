package com.procureflow.repository

import com.procureflow.domain.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CartItemRepository : JpaRepository<CartItem, UUID> {
    fun findByCartId(cartId: UUID): List<CartItem>
    fun findByCartIdAndItemId(cartId: UUID, itemId: UUID): CartItem?
    fun deleteByCartId(cartId: UUID)
}
