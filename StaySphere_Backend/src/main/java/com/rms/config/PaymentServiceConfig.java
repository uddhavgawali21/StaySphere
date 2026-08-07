package com.rms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentServiceConfig {

    @Value("${payment.service.base-url}")
    private String paymentServiceBaseUrl;

    @Value("${payment.service.internal-api-key}")
    private String internalApiKey;

    @Bean
    public RestClient paymentServiceRestClient() {
        return RestClient.builder()
                .baseUrl(paymentServiceBaseUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }
}