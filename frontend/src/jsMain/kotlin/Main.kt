import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import com.example.shared.MonsterDto
import com.example.shared.TraitDto

fun main() {
    val initialMonster = MonsterDto(
        name = "Young Green Dragon2",
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
            TraitDto("Bite", "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage plus 7 (2d6) poison damage."),
            TraitDto("Claw", "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."),
            TraitDto("Poison Breath (Recharge 5–6)", "The dragon exhales poisonous gas in a 30-foot cone. Each creature in that area must make a DC 14 Constitution saving throw, taking 42 (12d6) poison damage on a failed save, or half as much damage on a successful one.")
        )
    )

    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        var monster by remember { mutableStateOf(initialMonster) }
        var isEditing by remember { mutableStateOf(false) }

        Div({ style { marginBottom(20.px); textAlign("center") } }) {
            Button(attrs = {
                onClick { isEditing = !isEditing }
            }) {
                Text(if (isEditing) "Save Changes" else "Edit Monster")
            }
        }

        MonsterSheet(monster, isEditing) { newMonster ->
            monster = newMonster
        }
    }
}

@Composable
fun MonsterSheet(monster: MonsterDto, isEditing: Boolean, onUpdate: (MonsterDto) -> Unit) {
    Div({ classes(MonsterSheetStyle.container) }) {
        // Header Section
        Div({ classes(MonsterSheetStyle.header) }) {
            EditableText(monster.name, isEditing, { onUpdate(monster.copy(name = it)) }) {
                H1 { Text(monster.name) }
            }
            
            Div({ classes(MonsterSheetStyle.subHeader) }) {
                EditableText(monster.meta, isEditing, { onUpdate(monster.copy(meta = it)) }) {
                    Text(monster.meta)
                }
            }
        }

        Hr { }

        // Stats Block
        Div({ classes(MonsterSheetStyle.statsGrid) }) {
            StatBox("Armor Class", monster.armorClass, isEditing) { onUpdate(monster.copy(armorClass = it)) }
            StatBox("Hit Points", monster.hitPoints, isEditing) { onUpdate(monster.copy(hitPoints = it)) }
            StatBox("Speed", monster.speed, isEditing) { onUpdate(monster.copy(speed = it)) }
        }

        Hr { }

        // Ability Scores
        Div({ classes(MonsterSheetStyle.abilityScoreContainer) }) {
            AbilityScore("STR", monster.str, isEditing) { onUpdate(monster.copy(str = it)) }
            AbilityScore("DEX", monster.dex, isEditing) { onUpdate(monster.copy(dex = it)) }
            AbilityScore("CON", monster.con, isEditing) { onUpdate(monster.copy(con = it)) }
            AbilityScore("INT", monster.int, isEditing) { onUpdate(monster.copy(int = it)) }
            AbilityScore("WIS", monster.wis, isEditing) { onUpdate(monster.copy(wis = it)) }
            AbilityScore("CHA", monster.cha, isEditing) { onUpdate(monster.copy(cha = it)) }
        }

        Hr { }

        // Skills & Senses
        Div({ classes(MonsterSheetStyle.section) }) {
            PropertyLine("Saving Throws", monster.savingThrows ?: "", isEditing) { onUpdate(monster.copy(savingThrows = it)) }
            PropertyLine("Skills", monster.skills ?: "", isEditing) { onUpdate(monster.copy(skills = it)) }
            PropertyLine("Senses", monster.senses, isEditing) { onUpdate(monster.copy(senses = it)) }
            PropertyLine("Languages", monster.languages, isEditing) { onUpdate(monster.copy(languages = it)) }
            PropertyLine("Challenge", monster.challenge, isEditing) { onUpdate(monster.copy(challenge = it)) }
        }

        Hr { }

        // Traits
        Div({ classes(MonsterSheetStyle.section) }) {
            monster.traits.forEachIndexed { index, trait ->
                TraitBlock(
                    trait.name, 
                    trait.description, 
                    isEditing,
                    onNameChange = { newName -> 
                        val newTraits = monster.traits.toMutableList()
                        newTraits[index] = trait.copy(name = newName)
                        onUpdate(monster.copy(traits = newTraits))
                    },
                    onDescChange = { newDesc ->
                        val newTraits = monster.traits.toMutableList()
                        newTraits[index] = trait.copy(description = newDesc)
                        onUpdate(monster.copy(traits = newTraits))
                    }
                )
            }
        }

        // Actions
        H3({ classes(MonsterSheetStyle.sectionHeader) }) { Text("Actions") }
        Div({ classes(MonsterSheetStyle.section) }) {
             monster.actions.forEachIndexed { index, action ->
                TraitBlock(
                    action.name, 
                    action.description, 
                    isEditing,
                    onNameChange = { newName -> 
                        val newActions = monster.actions.toMutableList()
                        newActions[index] = action.copy(name = newName)
                        onUpdate(monster.copy(actions = newActions))
                    },
                    onDescChange = { newDesc ->
                        val newActions = monster.actions.toMutableList()
                        newActions[index] = action.copy(description = newDesc)
                        onUpdate(monster.copy(actions = newActions))
                    }
                )
            }
        }
    }
}