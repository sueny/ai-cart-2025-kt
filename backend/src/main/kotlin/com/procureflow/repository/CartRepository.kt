package com.procureflow.repository

import com.procureflow.domain.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CartRepository : JpaRepository<Cart, UUID> {
    fun findByUserId(userId: String): List<Cart>
}
