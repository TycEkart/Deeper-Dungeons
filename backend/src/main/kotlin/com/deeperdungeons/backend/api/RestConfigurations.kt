package com.deeperdungeons.backend.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class RestConfigurations {
    @Bean
    fun client(): RestClient {
        val restClient = RestClient.create()
        return restClient
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:8080") // Adjust if your frontend runs on a different port
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }

            override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
                val uploadDir = Paths.get("data/images").toAbsolutePath().toUri().toString()
                registry.addResourceHandler("/images/**")
                    .addResourceLocations(uploadDir)
            }
        }
    }
}