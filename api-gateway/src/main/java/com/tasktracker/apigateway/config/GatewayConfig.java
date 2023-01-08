package com.tasktracker.apigateway.config;

import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("task-tracker", r -> r.path("/api/boards/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://task-tracker"))
                .route("task-tracker", r -> r.path("/api/task-states/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://task-tracker"))
                .route("task-tracker", r -> r.path("/api/tasks/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://task-tracker"))
                .route("authorization-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://authorization-service"))
                .build();
    }
}
