package com.college.bustracking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI collegeBusTrackingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("College Bus Tracking API")
                        .version("1.0.0")
                        .description("REST and WebSocket APIs for the college bus tracking system"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder().group("Authentication").pathsToMatch("/api/auth/**").build();
    }

    @Bean
    public GroupedOpenApi adminGroup() {
        return GroupedOpenApi.builder().group("Admin").pathsToMatch("/api/admin/**").build();
    }

    @Bean
    public GroupedOpenApi driverGroup() {
        return GroupedOpenApi.builder().group("Driver").pathsToMatch("/api/driver/**").build();
    }

    @Bean
    public GroupedOpenApi studentGroup() {
        return GroupedOpenApi.builder().group("Student").pathsToMatch("/api/student/**", "/api/buses/**", "/api/stop/**").build();
    }

    @Bean
    public GroupedOpenApi trackingGroup() {
        return GroupedOpenApi.builder().group("Tracking").pathsToMatch("/api/bus/**").build();
    }
}
