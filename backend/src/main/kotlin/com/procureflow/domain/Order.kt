package com.procureflow.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart,

    @Column(nullable = false, precision = 10, scale = 2)
    val total: BigDecimal,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: OrderStatus = OrderStatus.CONFIRMED,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)

enum class OrderStatus {
    PENDING, CONFIRMED, CANCELLED
}
