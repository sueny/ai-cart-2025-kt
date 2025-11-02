package com.procureflow.repository

import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ItemRepository : JpaRepository<Item, UUID> {
    fun findByNameContainingIgnoreCase(name: String): List<Item>
    fun findByCategory(category: String): List<Item>
    fun findByNameContainingIgnoreCaseAndCategory(name: String, category: String): List<Item>
    fun findByStatus(status: ItemStatus): List<Item>
}
