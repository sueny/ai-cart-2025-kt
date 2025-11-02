package com.procureflow.config

import com.procureflow.agent.tools.AddToCartTool
import com.procureflow.agent.tools.CheckoutTool
import com.procureflow.agent.tools.RegisterItemTool
import com.procureflow.agent.tools.SearchItemsTool
import com.procureflow.service.CartService
import com.procureflow.service.CatalogService
import com.procureflow.service.OrderService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AgentConfig(
    private val catalogService: CatalogService,
    private val cartService: CartService,
    private val orderService: OrderService
) {

    @Bean
    fun searchItemsTool(): SearchItemsTool {
        return SearchItemsTool(catalogService)
    }

    @Bean
    fun registerItemTool(): RegisterItemTool {
        return RegisterItemTool(catalogService)
    }

    @Bean
    fun addToCartTool(): AddToCartTool {
        return AddToCartTool(cartService)
    }

    @Bean
    fun checkoutTool(): CheckoutTool {
        return CheckoutTool(orderService)
    }
}
