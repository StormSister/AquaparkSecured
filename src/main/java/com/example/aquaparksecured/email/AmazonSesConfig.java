package com.example.aquaparksecured.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AmazonSesConfig {

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.EU_NORTH_1)
                .build();
    }
}