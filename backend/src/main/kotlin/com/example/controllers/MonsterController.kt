package com.example.controllers

import com.example.repositories.TempRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
class HealthCheck(
    private val tempRepository: TempRepository
) {

    @GetMapping("/healthCheck")
    fun healthCheck(@RequestParam postcode: String, @RequestParam houseNumber: Int): String {
        log.info { "healthCheck" }
        return "Alive"
    }
}