package com.bites.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Sets up the Swagger/OpenAPI documentation for the Bites API.
 * This lets developers view and try out all API endpoints from a browser
 * (usually at /swagger-ui), and tells the docs UI that endpoints need a
 * Bearer JWT token for authentication.
 */
@Configuration
public class OpenApiConfig {

    // Name used to identify the JWT bearer authentication scheme in the generated docs.
    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    /*
     * Builds the OpenAPI definition used to generate the Swagger docs page,
     * including the API title/description and the JWT bearer auth requirement.
     */
    @Bean
    public OpenAPI bitesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bites API")
                        .description("REST API for the Bites restaurant ordering app")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
