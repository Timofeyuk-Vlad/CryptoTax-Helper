package com.cryptotax.helper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalApiConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.binance.com")
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }
}