package com.deeperdungeons.backend.controllers

import com.deeperdungeons.backend.repositories.MonsterRepository
import com.deeperdungeons.backend.repositories.toEntity
import com.deeperdungeons.shared.MonsterDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.info.BuildProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.optionals.getOrNull

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/monsters")
class MonsterImportExportController(
    val repository: MonsterRepository,
    val buildProperties: BuildProperties?
) {

    @GetMapping("/{id}/export")
    fun exportMonster(@PathVariable id: Int): ResponseEntity<MonsterDto> {
        log.info { "Exporting monster with id: $id" }
        val monster = repository.findById(id).getOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found")

        val dto = monster.toDto()
        
        val filename = "${dto.name.replace(" ", "_")}.json"
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(dto)
    }

    @PostMapping("/import")
    fun importMonster(@RequestBody monsterDto: MonsterDto): MonsterDto {
        log.info { "Importing monster: ${monsterDto.name}" }
        
        // Always prioritize the current app version when importing
        val currentVersion = buildProperties?.version ?: "Unknown"
        
        // Ensure ID is null so it creates a new entry, and override version
        val monsterToSave = monsterDto.copy(
            id = null,
            deeperDungeonsVersion = currentVersion
        )
        
        val entity = monsterToSave.toEntity()
        val savedEntity = repository.save(entity)
        return savedEntity.toDto()
    }
}
