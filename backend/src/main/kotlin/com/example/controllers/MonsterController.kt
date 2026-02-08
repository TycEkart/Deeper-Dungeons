package com.example.controllers

import com.example.repositories.MonsterRepository
import com.example.repositories.toEntity
import com.example.shared.MonsterDto
import com.example.shared.TraitDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.optionals.getOrNull

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/monsters")
class MonsterController(val repository: MonsterRepository) {

    @GetMapping
    fun getMonster(): MonsterDto {
        log.info { "Getting default monster" }
        val monster = repository.findAll().firstOrNull()
        return monster?.toDto() ?: createInitialMonster()
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
        val entity = monsterDto.toEntity()
        val savedEntity = repository.save(entity)
        return savedEntity.toDto()
    }

    private fun createInitialMonster(): MonsterDto {
        return MonsterDto(
            name = "Young Green Dragon",
            meta = "Large dragon, lawful evil",
            armorClass = "18 (natural armor)",
            hitPoints = "136 (16d10 + 48)",
            speed = "40 ft., fly 80 ft., swim 40 ft.",
            str = "19 (+4)",
            dex = "12 (+1)",
            con = "17 (+3)",
            int = "16 (+3)",
            wis = "13 (+1)",
            cha = "15 (+2)",
            savingThrows = "Dex +4, Con +6, Wis +4, Cha +5",
            skills = "Deception +5, Perception +7, Stealth +4",
            senses = "blindsight 30 ft., darkvision 120 ft., passive Perception 17",
            languages = "Common, Draconic",
            challenge = "8 (3,900 XP)",
            traits = listOf(
                TraitDto("Amphibious", "The dragon can breathe air and water.")
            ),
            actions = listOf(
                TraitDto("Multiattack", "The dragon makes three attacks: one with its bite and two with its claws."),
                TraitDto(
                    "Bite",
                    "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage plus 7 (2d6) poison damage."
                ),
                TraitDto(
                    "Claw",
                    "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."
                ),
                TraitDto(
                    "Poison Breath (Recharge 5–6)",
                    "The dragon exhales poisonous gas in a 30-foot cone. Each creature in that area must make a DC 14 Constitution saving throw, taking 42 (12d6) poison damage on a failed save, or half as much damage on a successful one."
                )
            )
        )
    }
}