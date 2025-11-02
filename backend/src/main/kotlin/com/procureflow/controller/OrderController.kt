package com.procureflow.controller

import com.procureflow.dto.CreateOrderRequest
import com.procureflow.dto.OrderDto
import com.procureflow.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = ["*"])
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderDto> {
        val order = orderService.checkout(request.cartId)
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: UUID): ResponseEntity<OrderDto> {
        val order = orderService.getOrder(id)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<OrderDto>> {
        val orders = orderService.getAllOrders()
        return ResponseEntity.ok(orders)
    }
}
