package com.procureflow.controller

import com.procureflow.dto.ChatRequest
import com.procureflow.dto.ChatResponse
import com.procureflow.service.AgentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/agent")
@CrossOrigin(origins = ["*"])
class AgentController(
    private val agentService: AgentService
) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val response = agentService.chat(request.messages, request.cartId)
        return ResponseEntity.ok(ChatResponse(response = response, cartId = request.cartId))
    }
}
