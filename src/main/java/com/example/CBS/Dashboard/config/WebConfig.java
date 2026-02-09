package com.example.CBS.Dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:4200",
                                "http://127.0.0.1:4200",
                                "http://localhost:5000",
                                "http://127.0.0.1:5000",
                                "http://72.61.116.191:4200",
                                "http://72.61.116.191:5000",
                                "http://72.61.116.191",
                                "https://localhost:4200",
                                "https://72.61.116.191:4200",
                                "https://72.61.116.191:5000",
                                "https://72.61.116.191"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
            
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Ensure API paths are not treated as static resources
                // Only serve static resources for non-API paths
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/static/", "classpath:/public/")
                        .setCachePeriod(0);
            }
        };
    }
}
