package com.example.repositories

import com.example.shared.MonsterDto
import com.example.shared.TraitDto
import jakarta.persistence.*

@Entity
data class MonsterEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
    val name: String = "",
    val meta: String = "",
    val armorClass: String = "",
    val hitPoints: String = "",
    val speed: String = "",
    val str: String = "",
    val dex: String = "",
    val con: String = "",
    val intVal: String = "", // 'int' is a reserved keyword in SQL/Kotlin
    val wis: String = "",
    val cha: String = "",
    val savingThrows: String? = null,
    val skills: String? = null,
    val senses: String = "",
    val languages: String = "",
    val challenge: String = "",
    
    @ElementCollection
    val traits: List<TraitEmbeddable> = emptyList(),
    
    @ElementCollection
    val actions: List<TraitEmbeddable> = emptyList()
) {
    fun toDto() = MonsterDto(
        id = id,
        name = name,
        meta = meta,
        armorClass = armorClass,
        hitPoints = hitPoints,
        speed = speed,
        str = str,
        dex = dex,
        con = con,
        int = intVal,
        wis = wis,
        cha = cha,
        savingThrows = savingThrows,
        skills = skills,
        senses = senses,
        languages = languages,
        challenge = challenge,
        traits = traits.map { it.toDto() },
        actions = actions.map { it.toDto() }
    )
}

@Embeddable
data class TraitEmbeddable(
    val name: String = "",
    @Column(length = 1000)
    val description: String = ""
) {
    fun toDto() = TraitDto(name, description)
}

fun MonsterDto.toEntity() = MonsterEntity(
    id = id,
    name = name,
    meta = meta,
    armorClass = armorClass,
    hitPoints = hitPoints,
    speed = speed,
    str = str,
    dex = dex,
    con = con,
    intVal = int,
    wis = wis,
    cha = cha,
    savingThrows = savingThrows,
    skills = skills,
    senses = senses,
    languages = languages,
    challenge = challenge,
    traits = traits.map { TraitEmbeddable(it.name, it.description) },
    actions = actions.map { TraitEmbeddable(it.name, it.description) }
)