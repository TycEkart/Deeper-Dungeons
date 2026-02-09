package com.deeperdungeons.backend.repositories

import com.deeperdungeons.shared.Alignment
import com.deeperdungeons.shared.ArmorClassDto
import com.deeperdungeons.shared.MonsterDto
import com.deeperdungeons.shared.MonsterSize
import com.deeperdungeons.shared.MonsterType
import com.deeperdungeons.shared.StatDto
import com.deeperdungeons.shared.TraitDto
import jakarta.persistence.*
import kotlin.math.floor

@Entity
data class MonsterEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
    val name: String = "",
    @Enumerated(EnumType.STRING)
    val size: MonsterSize = MonsterSize.Medium,
    @Enumerated(EnumType.STRING)
    val type: MonsterType = MonsterType.Humanoid,
    @Enumerated(EnumType.STRING)
    val alignment: Alignment = Alignment.Unaligned,
    val armorClassValue: Int = 10,
    val armorClassDescription: String? = null,
    val hitPoints: String = "",
    val speed: String = "",
    val str: Int = 10,
    val dex: Int = 10,
    val con: Int = 10,
    val intVal: Int = 10, // 'int' is a reserved keyword in SQL/Kotlin
    val wis: Int = 10,
    val cha: Int = 10,
    val savingThrows: String? = null,
    val skills: String? = null,
    val senses: String = "",
    val languages: String = "",
    val challenge: String = "",
    val imageUrl: String? = null,
    val imagePosition: String = "top",
    val imageScale: Float = 1.0f,

    @ElementCollection
    val traits: List<TraitEmbeddable> = emptyList(),

    @ElementCollection
    val actions: List<TraitEmbeddable> = emptyList(),

    @ElementCollection
    val reactions: List<TraitEmbeddable> = emptyList()
) {
    fun toDto(): MonsterDto {
        fun calculateModifier(score: Int): Int {
            return floor((score - 10) / 2.0).toInt()
        }

        return MonsterDto(
            id = id,
            name = name,
            size = size,
            type = type,
            alignment = alignment,
            armorClass = ArmorClassDto(armorClassValue, armorClassDescription),
            hitPoints = hitPoints,
            speed = speed,
            str = StatDto(str, calculateModifier(str)),
            dex = StatDto(dex, calculateModifier(dex)),
            con = StatDto(con, calculateModifier(con)),
            int = StatDto(intVal, calculateModifier(intVal)),
            wis = StatDto(wis, calculateModifier(wis)),
            cha = StatDto(cha, calculateModifier(cha)),
            savingThrows = savingThrows,
            skills = skills,
            senses = senses,
            languages = languages,
            challenge = challenge,
            imageUrl = imageUrl,
            imagePosition = imagePosition,
            imageScale = imageScale,
            traits = traits.map { it.toDto() },
            actions = actions.map { it.toDto() },
            reactions = reactions.map { it.toDto() }
        )
    }
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
    size = size,
    type = type,
    alignment = alignment,
    armorClassValue = armorClass.value,
    armorClassDescription = armorClass.description,
    hitPoints = hitPoints,
    speed = speed,
    str = str.value,
    dex = dex.value,
    con = con.value,
    intVal = int.value,
    wis = wis.value,
    cha = cha.value,
    savingThrows = savingThrows,
    skills = skills,
    senses = senses,
    languages = languages,
    challenge = challenge,
    imageUrl = imageUrl,
    imagePosition = imagePosition,
    imageScale = imageScale,
    traits = traits.map { TraitEmbeddable(it.name, it.description) },
    actions = actions.map { TraitEmbeddable(it.name, it.description) },
    reactions = reactions.map { TraitEmbeddable(it.name, it.description) }
)