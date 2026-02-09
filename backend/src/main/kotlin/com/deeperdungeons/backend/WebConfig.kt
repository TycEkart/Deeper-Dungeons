package com.deeperdungeons.backend

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve uploaded images
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:data/images/")

        // Serve static frontend files
        // This must be after specific handlers if we want specific ones to take precedence,
        // but /** matches everything.
        // Spring matches patterns. /images/** is more specific than /**.
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Forward root to index.html
        registry.addViewController("/").setViewName("forward:/index.html")
    }
}