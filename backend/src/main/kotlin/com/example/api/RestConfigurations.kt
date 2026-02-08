package com.example.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestConfigurations {
    @Bean
    fun client(): RestClient {
        val restClient = RestClient.create()
        return restClient
    }
}