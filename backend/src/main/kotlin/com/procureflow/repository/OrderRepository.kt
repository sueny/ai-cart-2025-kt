package com.procureflow.repository

import com.procureflow.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByCartId(cartId: UUID): List<Order>
}
