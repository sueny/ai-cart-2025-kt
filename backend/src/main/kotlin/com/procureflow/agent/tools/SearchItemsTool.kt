package com.procureflow.agent.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.procureflow.dto.ItemDto
import com.procureflow.service.CatalogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.util.function.Function


@Description("Search for items in the catalog by name or keyword")
class SearchItemsTool(
    private val catalogService: CatalogService
) : Function<SearchItemsRequest, SearchItemsResponse> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun apply(request: SearchItemsRequest): SearchItemsResponse {
        logger.info("SearchItemsTool invoked with query: '${request.query}', category: ${request.category}")
        return try {
            val items = catalogService.searchItems(request.query, request.category)
            logger.info("Search completed: found ${items.size} items for query: '${request.query}'")
            SearchItemsResponse(items)
        } catch (e: Exception) {
            logger.error("Error searching for items with query: '${request.query}'", e)
            throw e
        }
    }
}

@JsonClassDescription("Request to search for items in the catalog")
data class SearchItemsRequest(
    @JsonPropertyDescription("Search query or keyword to find items")
    val query: String,

    @JsonPropertyDescription("Optional category filter (e.g., Electronics, Office Supplies, Furniture)")
    val category: String? = null
)

data class SearchItemsResponse(
    val items: List<ItemDto>
)
