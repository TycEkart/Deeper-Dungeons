package com.deeperdungeons.backend.controllers

import com.deeperdungeons.backend.repositories.MonsterRepository
import com.deeperdungeons.shared.Alignment
import com.deeperdungeons.shared.ArmorClassDto
import com.deeperdungeons.shared.MonsterDto
import com.deeperdungeons.shared.MonsterSize
import com.deeperdungeons.shared.MonsterType
import com.deeperdungeons.shared.StatDto
import com.deeperdungeons.shared.TraitDto
import com.deeperdungeons.backend.repositories.toEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.info.BuildProperties
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.jvm.optionals.getOrNull

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/monsters")
class MonsterController(
    val repository: MonsterRepository,
    val restClient: RestClient,
    val buildProperties: BuildProperties?
) {

    @GetMapping
    fun getAllMonsters(): List<MonsterDto> {
        log.info { "Getting all monsters" }
        val monsters = repository.findAll()
        return monsters.map { it.toDto() }
    }

    @GetMapping("/{id}")
    fun getMonsterById(@PathVariable id: Int): MonsterDto {
        log.info { "Getting monster with id: $id" }
        val monster = repository.findById(id).getOrNull()
        return monster?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found")
    }

    @PutMapping
    fun saveMonster(@RequestBody monsterDto: MonsterDto): MonsterDto {
        log.info { "Saving monster: $monsterDto" }
        
        // If creating a new monster or updating, ensure version is set to current app version
        // Note: If you want to preserve the original version on update, you might need logic here.
        // But typically, saving implies modifying with the current version of the tool.
        val currentVersion = buildProperties?.version ?: "Unknown"
        val monsterToSave = monsterDto.copy(deeperDungeonsVersion = currentVersion)
        
        val entity = monsterToSave.toEntity()
        val savedEntity = repository.save(entity)
        return savedEntity.toDto()
    }

    @DeleteMapping("/{id}")
    fun deleteMonster(@PathVariable id: Int) {
        log.info { "Deleting monster with id: $id" }
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found")
        }
    }

    @PostMapping(value = ["/{id}/image"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(@PathVariable id: Int, @RequestParam("file") file: MultipartFile): MonsterDto {
        log.info { "Uploading image for monster id: $id" }
        val monster = repository.findById(id).getOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found")

        val uploadDir = Paths.get("data/images").toAbsolutePath()
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        val fileName = "monster_$id.${file.originalFilename?.substringAfterLast('.', "png") ?: "png"}"
        val filePath = uploadDir.resolve(fileName)
        Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

        val updatedMonster = monster.copy(imageUrl = "/images/$fileName")
        val savedEntity = repository.save(updatedMonster)
        return savedEntity.toDto()
    }

    @PostMapping("/{id}/image-url")
    fun uploadImageUrl(@PathVariable id: Int, @RequestBody imageUrl: String): MonsterDto {
        log.info { "Uploading image from URL for monster id: $id, url: $imageUrl" }
        val monster = repository.findById(id).getOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found")

        val uploadDir = Paths.get("data/images").toAbsolutePath()
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        // Download the image
        val imageBytes = restClient.get().uri(imageUrl).retrieve().body(ByteArray::class.java)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not download image")

        val fileName = "monster_$id.png" // Defaulting to png, could try to detect from content-type
        val filePath = uploadDir.resolve(fileName)
        Files.write(filePath, imageBytes)

        val updatedMonster = monster.copy(imageUrl = "/images/$fileName")
        val savedEntity = repository.save(updatedMonster)
        return savedEntity.toDto()
    }
}