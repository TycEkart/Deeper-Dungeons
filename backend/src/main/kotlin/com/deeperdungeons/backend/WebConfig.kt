package com.deeperdungeons.backend

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve uploaded images
        // Use absolute path to ensure it points to the same location as the controller
        val uploadDir = Paths.get("data/images").toAbsolutePath().toUri().toString()
        registry.addResourceHandler("/images/**")
            .addResourceLocations(uploadDir)

        // Serve static frontend files
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Forward root to index.html
        registry.addViewController("/").setViewName("forward:/index.html")
    }
}