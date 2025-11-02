package com.procureflow.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "carts")
data class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: MutableList<CartItem> = mutableListOf()
)
