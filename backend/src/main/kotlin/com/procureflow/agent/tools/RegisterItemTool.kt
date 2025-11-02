package com.procureflow.agent.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.procureflow.dto.CreateItemRequest
import com.procureflow.dto.ItemDto
import com.procureflow.service.CatalogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.function.Function


@Description("Register a new item in the catalog when it doesn't exist")
class RegisterItemTool(
    private val catalogService: CatalogService
) : Function<RegisterItemRequest, RegisterItemResponse> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun apply(request: RegisterItemRequest): RegisterItemResponse {
        logger.info("RegisterItemTool invoked with name: '${request.name}', category: '${request.category}', price: ${request.price}")
        return try {
            val createRequest = CreateItemRequest(
                name = request.name,
                category = request.category,
                description = request.description,
                price = request.price,
                status = request.status
            )
            val item = catalogService.registerItem(createRequest)
            logger.info("Item registered successfully: ${item.name} (id: ${item.id}) in category: ${item.category}")
            RegisterItemResponse(item)
        } catch (e: Exception) {
            logger.error("Error registering item with name: '${request.name}'", e)
            throw e
        }
    }
}

@JsonClassDescription("Request to register a new item in the catalog")
data class RegisterItemRequest(
    @JsonPropertyDescription("Name of the item to register")
    val name: String,

    @JsonPropertyDescription("Category (e.g., Electronics, Office Supplies, Furniture)")
    val category: String,

    @JsonPropertyDescription("Detailed description of the item")
    val description: String? = null,

    @JsonPropertyDescription("Price in USD (e.g., 99.99)")
    val price: BigDecimal,

    @JsonPropertyDescription("Status: ACTIVE, INACTIVE, or OUT_OF_STOCK")
    val status: String = "ACTIVE"
)

data class RegisterItemResponse(
    val item: ItemDto,
    val message: String = "Item registered successfully"
)
