package com.deeperdungeons.backend.controllers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.info.BuildProperties
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.concurrent.thread

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/system")
class SystemController(
    private val context: ApplicationContext,
    private val buildProperties: BuildProperties?
) {

    @PostMapping("/shutdown")
    fun shutdown() {
        log.info { "Shutdown request received via frontend." }
        // Run in a separate thread to allow the response to be sent back to the frontend first
        thread {
            Thread.sleep(500) // Give a small buffer for the response to flush
            val exitCode = SpringApplication.exit(context, { 0 })
            System.exit(exitCode)
        }
    }

    @GetMapping("/version")
    fun getVersion(): String {
        return buildProperties?.version ?: "Unknown"
    }
}