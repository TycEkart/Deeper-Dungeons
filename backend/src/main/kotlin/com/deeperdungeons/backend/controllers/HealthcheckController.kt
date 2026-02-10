package com.deeperdungeons.backend.controllers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
class HealthCheck {

    @GetMapping("/healthCheck")
    fun healthCheck(): String {
        log.info { "healthCheck" }
        return "Alive"
    }
}