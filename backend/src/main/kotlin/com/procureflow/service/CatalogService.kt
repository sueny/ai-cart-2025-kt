package com.procureflow.service

import com.procureflow.domain.Item
import com.procureflow.domain.ItemStatus
import com.procureflow.dto.CreateItemRequest
import com.procureflow.dto.ItemDto
import com.procureflow.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CatalogService(
    private val itemRepository: ItemRepository
) {

    fun searchItems(query: String, category: String? = null): List<ItemDto> {
        val items = if (category != null) {
            itemRepository.findByNameContainingIgnoreCaseAndCategory(query, category)
        } else {
            itemRepository.findByNameContainingIgnoreCase(query)
        }
        return items.map { it.toDto() }
    }

    fun getAllItems(): List<ItemDto> {
        return itemRepository.findAll().map { it.toDto() }
    }

    fun getItemById(id: UUID): ItemDto? {
        return itemRepository.findById(id).map { it.toDto() }.orElse(null)
    }

    fun registerItem(request: CreateItemRequest): ItemDto {
        val item = Item(
            name = request.name,
            category = request.category,
            description = request.description,
            price = request.price,
            status = ItemStatus.valueOf(request.status)
        )
        val saved = itemRepository.save(item)
        return saved.toDto()
    }

    fun updateItem(id: UUID, name: String?, category: String?, description: String?, price: java.math.BigDecimal?, status: String?): ItemDto? {
        val item = itemRepository.findById(id).orElse(null) ?: return null

        val updated = item.copy(
            name = name ?: item.name,
            category = category ?: item.category,
            description = description ?: item.description,
            price = price ?: item.price,
            status = status?.let { ItemStatus.valueOf(it) } ?: item.status,
            updatedAt = java.time.Instant.now()
        )

        return itemRepository.save(updated).toDto()
    }

    fun deleteItem(id: UUID) {
        itemRepository.deleteById(id)
    }

    private fun Item.toDto() = ItemDto(
        id = id,
        name = name,
        category = category,
        description = description,
        price = price,
        status = status.name,
        createdAt = createdAt
    )
}
