package com.gm2dev.demo_spring.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> Health.up()
                .withDetail("app", "Demo Spring Microservice")
                .withDetail("version", "1.0.0")
                .withDetail("description", "Spring Boot microservice for demo purposes")
                .build();
    }
}