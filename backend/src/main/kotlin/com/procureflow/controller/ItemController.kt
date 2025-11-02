package com.procureflow.controller

import com.procureflow.dto.CreateItemRequest
import com.procureflow.dto.ItemDto
import com.procureflow.dto.UpdateItemRequest
import com.procureflow.service.CatalogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/items")
@CrossOrigin(origins = ["*"])
class ItemController(
    private val catalogService: CatalogService
) {

    @GetMapping
    fun getAllItems(): ResponseEntity<List<ItemDto>> {
        val items = catalogService.getAllItems()
        return ResponseEntity.ok(items)
    }

    @GetMapping("/search")
    fun searchItems(
        @RequestParam("q") query: String,
        @RequestParam(required = false) category: String?
    ): ResponseEntity<List<ItemDto>> {
        val items = catalogService.searchItems(query, category)
        return ResponseEntity.ok(items)
    }

    @GetMapping("/{id}")
    fun getItem(@PathVariable id: UUID): ResponseEntity<ItemDto> {
        val item = catalogService.getItemById(id)
        return if (item != null) {
            ResponseEntity.ok(item)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createItem(@RequestBody request: CreateItemRequest): ResponseEntity<ItemDto> {
        val item = catalogService.registerItem(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(item)
    }

    @PutMapping("/{id}")
    fun updateItem(
        @PathVariable id: UUID,
        @RequestBody request: UpdateItemRequest
    ): ResponseEntity<ItemDto> {
        val updated = catalogService.updateItem(
            id,
            request.name,
            request.category,
            request.description,
            request.price,
            request.status
        )
        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteItem(@PathVariable id: UUID): ResponseEntity<Unit> {
        catalogService.deleteItem(id)
        return ResponseEntity.noContent().build()
    }
}
