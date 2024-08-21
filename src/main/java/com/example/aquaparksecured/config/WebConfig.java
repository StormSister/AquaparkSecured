package com.example.aquaparksecured.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Define the path for serving files
        registry.addResourceHandler("/tickets/api/file/**")
                .addResourceLocations("file:/C:/Users/momika/AquaparkSecured/src/main/resources/tickets/");
    }
}