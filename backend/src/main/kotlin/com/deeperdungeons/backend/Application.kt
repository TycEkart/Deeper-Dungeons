package com.deeperdungeons.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class Application {
    @Bean
    fun printer(@Value("\${server.port:8090}") port: String): CommandLineRunner {
        return CommandLineRunner {
            logger.info { "Swagger UI: http://localhost:$port/swagger-ui/index.html" }
            logger.info { "H2 Console: http://localhost:$port/h2-console" }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }
}