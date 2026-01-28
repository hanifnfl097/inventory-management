package com.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI inventoryManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Management System API")
                        .description("REST API for managing inventory with ITEM, INVENTORY, and ORDER modules. " +
                                "Features real-time stock calculation, soft delete, and @Transactional support.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Inventory Management Team")
                                .email("support@inventory.com")));
    }
}
