package com.deeperdungeons.shared

import kotlinx.serialization.Serializable

@Serializable
data class MonsterDto(
    val id: Int? = null,
    val name: String,
    val size: MonsterSize,
    val type: MonsterType,
    val alignment: Alignment,
    val armorClass: ArmorClassDto,
    val hitPoints: String,
    val speed: String,
    val str: StatDto,
    val dex: StatDto,
    val con: StatDto,
    val int: StatDto,
    val wis: StatDto,
    val cha: StatDto,
    val savingThrows: String? = null,
    val skills: String? = null,
    val senses: String,
    val languages: String,
    val challenge: String,
    val imageUrl: String? = null,
    val imagePrompt: String? = null,
    val imagePosition: String = "top", // "top", "right", "bottom", "left"
    val imageScale: Float = 1.0f,
    val traits: List<TraitDto> = emptyList(),
    val actions: List<TraitDto> = emptyList(),
    val reactions: List<TraitDto> = emptyList()
)


@Serializable
data class ArmorClassDto(
    val value: Int,
    val description: String? = null
) {
    override fun toString(): String {
        return if (description.isNullOrBlank()) "$value" else "$value ($description)"
    }
}

@Serializable
data class StatDto(
    val value: Int,
    val modifier: Int
)

@Serializable
data class TraitDto(
    val name: String,
    val description: String
)

enum class Stat(val label: String) {
    STR("Str"),
    DEX("Dex"),
    CON("Con"),
    INT("Int"),
    WIS("Wis"),
    CHA("Cha")
}

enum class Skill(val label: String) {
    Acrobatics("Acrobatics"),
    AnimalHandling("Animal Handling"),
    Arcana("Arcana"),
    Athletics("Athletics"),
    Deception("Deception"),
    History("History"),
    Insight("Insight"),
    Intimidation("Intimidation"),
    Investigation("Investigation"),
    Medicine("Medicine"),
    Nature("Nature"),
    Perception("Perception"),
    Performance("Performance"),
    Persuasion("Persuasion"),
    Religion("Religion"),
    SleightOfHand("Sleight of Hand"),
    Stealth("Stealth"),
    Survival("Survival")
}

enum class MonsterSize(val label: String) {
    Tiny("Tiny"),
    Small("Small"),
    Medium("Medium"),
    Large("Large"),
    Huge("Huge"),
    Gargantuan("Gargantuan")
}

enum class MonsterType(val label: String) {
    Aberration("aberration"),
    Beast("beast"),
    Celestial("celestial"),
    Construct("construct"),
    Dragon("dragon"),
    Elemental("elemental"),
    Fey("fey"),
    Fiend("fiend"),
    Giant("giant"),
    Humanoid("humanoid"),
    Monstrosity("monstrosity"),
    Ooze("ooze"),
    Plant("plant"),
    Undead("undead")
}

enum class Alignment(val label: String) {
    LawfulGood("lawful good"),
    NeutralGood("neutral good"),
    ChaoticGood("chaotic good"),
    LawfulNeutral("lawful neutral"),
    TrueNeutral("neutral"),
    ChaoticNeutral("chaotic neutral"),
    LawfulEvil("lawful evil"),
    NeutralEvil("neutral evil"),
    ChaoticEvil("chaotic evil"),
    Unaligned("unaligned"),
    AnyAlignment("any alignment")
}