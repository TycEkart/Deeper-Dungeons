package com.example.shared

import kotlinx.serialization.Serializable

@Serializable
data class MonsterDto(
    val id: Int? = null,
    val name: String,
    val meta: String, // e.g., "Large dragon, chaotic evil"
    val armorClass: String,
    val hitPoints: String,
    val speed: String,
    val str: String,
    val dex: String,
    val con: String,
    val int: String,
    val wis: String,
    val cha: String,
    val savingThrows: String? = null,
    val skills: String? = null,
    val senses: String,
    val languages: String,
    val challenge: String,
    val imageUrl: String? = null,
    val imagePosition: String = "top", // "top", "right", "bottom", "left"
    val traits: List<TraitDto> = emptyList(),
    val actions: List<TraitDto> = emptyList()
)

@Serializable
data class TraitDto(
    val name: String,
    val description: String
)