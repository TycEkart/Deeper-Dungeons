import androidx.compose.runtime.*
import com.example.shared.MonsterDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        var monster by remember { mutableStateOf<MonsterDto?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    monster = fetchMonster()
                } catch (e: Exception) {
                    console.error("Failed to fetch monster", e)
                }
            }
        }

        if (monster != null) {
            MonsterSheet(monster!!) { newMonster ->
                // Optimistic update
                monster = newMonster
                
                // Save to backend
                scope.launch {
                    try {
                        val savedMonster = saveMonster(newMonster)
                        monster = savedMonster // Update with server response (e.g. generated ID)
                    } catch (e: Exception) {
                        console.error("Failed to save monster", e)
                        // Optionally revert optimistic update or show error
                    }
                }
            }
        } else {
            Div({ style { padding(20.px) } }) {
                Text("Loading monster...")
            }
        }
    }
}

@Composable
fun MonsterSheet(monster: MonsterDto, onUpdate: (MonsterDto) -> Unit) {
    Div({ classes(MonsterSheetStyle.container) }) {
        // ID Label
        if (monster.id != null) {
            Div({ classes(MonsterSheetStyle.idLabel) }) {
                Text("ID: ${monster.id}")
            }
        }

        // Header Section
        Div({ classes(MonsterSheetStyle.header) }) {
            EditableText(monster.name, onValueChange = { onUpdate(monster.copy(name = it)) }) {
                H1 { Text(monster.name) }
            }
            
            Div({ classes(MonsterSheetStyle.subHeader) }) {
                EditableText(monster.meta, onValueChange = { onUpdate(monster.copy(meta = it)) }) {
                    Text(monster.meta)
                }
            }
        }

        Hr { }

        // Stats Block
        Div({ classes(MonsterSheetStyle.statsGrid) }) {
            StatBox("Armor Class", monster.armorClass) { onUpdate(monster.copy(armorClass = it)) }
            StatBox("Hit Points", monster.hitPoints) { onUpdate(monster.copy(hitPoints = it)) }
            StatBox("Speed", monster.speed) { onUpdate(monster.copy(speed = it)) }
        }

        Hr { }

        // Ability Scores
        Div({ classes(MonsterSheetStyle.abilityScoreContainer) }) {
            AbilityScore("STR", monster.str) { onUpdate(monster.copy(str = it)) }
            AbilityScore("DEX", monster.dex) { onUpdate(monster.copy(dex = it)) }
            AbilityScore("CON", monster.con) { onUpdate(monster.copy(con = it)) }
            AbilityScore("INT", monster.int) { onUpdate(monster.copy(int = it)) }
            AbilityScore("WIS", monster.wis) { onUpdate(monster.copy(wis = it)) }
            AbilityScore("CHA", monster.cha) { onUpdate(monster.copy(cha = it)) }
        }

        Hr { }

        // Skills & Senses
        Div({ classes(MonsterSheetStyle.section) }) {
            PropertyLine("Saving Throws", monster.savingThrows ?: "") { onUpdate(monster.copy(savingThrows = it)) }
            PropertyLine("Skills", monster.skills ?: "") { onUpdate(monster.copy(skills = it)) }
            PropertyLine("Senses", monster.senses) { onUpdate(monster.copy(senses = it)) }
            PropertyLine("Languages", monster.languages) { onUpdate(monster.copy(languages = it)) }
            PropertyLine("Challenge", monster.challenge) { onUpdate(monster.copy(challenge = it)) }
        }

        Hr { }

        // Traits
        Div({ classes(MonsterSheetStyle.section) }) {
            monster.traits.forEachIndexed { index, trait ->
                TraitBlock(
                    trait.name, 
                    trait.description, 
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