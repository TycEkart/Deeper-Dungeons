import androidx.compose.runtime.*
import com.example.shared.MonsterDto
import com.example.shared.TraitDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

fun main() {
    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        var monster by remember { mutableStateOf<MonsterDto?>(null) }
        val scope = rememberCoroutineScope()

        // Simple way to get ID from URL query param ?id=1
        val urlParams = org.w3c.dom.url.URLSearchParams(window.location.search)
        val monsterId = urlParams.get("id")?.toIntOrNull()

        LaunchedEffect(monsterId) {
            scope.launch {
                try {
                    monster = fetchMonster(monsterId)
                } catch (e: Exception) {
                    console.error("Failed to fetch monster", e)
                }
            }
        }

        if (monster != null) {
            MonsterSheet(monster!!) { newMonster ->
                monster = newMonster
                
                scope.launch {
                    try {
                        val savedMonster = saveMonster(newMonster)
                        monster = savedMonster
                        // Update URL if it was a new monster
                        if (monsterId == null && savedMonster.id != null) {
                            val newUrl = "${window.location.pathname}?id=${savedMonster.id}"
                            window.history.pushState(null, "", newUrl)
                        }
                    } catch (e: Exception) {
                        console.error("Failed to save monster", e)
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
fun MonsterSheet(initialMonster: MonsterDto, onSave: (MonsterDto) -> Unit) {
    var monster by remember { mutableStateOf(initialMonster) }
    var isEditingEnabled by remember { mutableStateOf(false) }

    // Update local state when initialMonster changes (e.g. after save)
    LaunchedEffect(initialMonster) {
        monster = initialMonster
    }

    Div({ classes(MonsterSheetStyle.mainContainer) }) {
        // Controls (ID & Edit Button) outside the sheet
        Div({ classes(MonsterSheetStyle.controlsContainer) }) {
            Div {
                if (monster.id != null) {
                    Text("ID: ${monster.id}")
                }
            }
            Div({ style { display(DisplayStyle.Flex); gap(5.px) } }) {
                Button(attrs = {
                    style {
                        fontSize(12.px)
                        padding(5.px, 10.px)
                        cursor("pointer")
                    }
                    onClick {
                        val element = document.getElementById("monster-sheet-content") as? HTMLElement
                        if (element != null) {
                            html2canvas(element).then { canvas ->
                                val link = document.createElement("a") as org.w3c.dom.HTMLAnchorElement
                                link.download = "${monster.name.replace(" ", "_")}.png"
                                link.href = (canvas as org.w3c.dom.HTMLCanvasElement).toDataURL()
                                link.click()
                                null
                            }
                        }
                    }
                }) {
                    Text("Print PNG")
                }

                Button(attrs = {
                    style {
                        fontSize(12.px)
                        padding(5.px, 10.px)
                        cursor("pointer")
                    }
                    onClick {
                        if (isEditingEnabled) {
                            // Save when clicking "Done"
                            onSave(monster)
                        }
                        isEditingEnabled = !isEditingEnabled
                    }
                }) {
                    Text(if (isEditingEnabled) "Done" else "Edit")
                }
            }
        }

        // The Monster Sheet itself
        Div({ 
            id("monster-sheet-content")
            classes(MonsterSheetStyle.sheetContainer) 
        }) {
            // Header Section
            Div({ classes(MonsterSheetStyle.header) }) {
                EditableText(monster.name, isEditingEnabled = isEditingEnabled, onValueChange = { monster = monster.copy(name = it) }) {
                    H1 { Text(monster.name) }
                }
                
                Div({ classes(MonsterSheetStyle.subHeader) }) {
                    EditableText(monster.meta, isEditingEnabled = isEditingEnabled, onValueChange = { monster = monster.copy(meta = it) }) {
                        Text(monster.meta)
                    }
                }
            }

            Hr { }

            // Stats Block
            Div({ classes(MonsterSheetStyle.statsGrid) }) {
                StatBox("Armor Class", monster.armorClass, isEditingEnabled) { monster = monster.copy(armorClass = it) }
                StatBox("Hit Points", monster.hitPoints, isEditingEnabled) { monster = monster.copy(hitPoints = it) }
                StatBox("Speed", monster.speed, isEditingEnabled) { monster = monster.copy(speed = it) }
            }

            Hr { }

            // Ability Scores
            Div({ classes(MonsterSheetStyle.abilityScoreContainer) }) {
                AbilityScore("STR", monster.str, isEditingEnabled) { monster = monster.copy(str = it) }
                AbilityScore("DEX", monster.dex, isEditingEnabled) { monster = monster.copy(dex = it) }
                AbilityScore("CON", monster.con, isEditingEnabled) { monster = monster.copy(con = it) }
                AbilityScore("INT", monster.int, isEditingEnabled) { monster = monster.copy(int = it) }
                AbilityScore("WIS", monster.wis, isEditingEnabled) { monster = monster.copy(wis = it) }
                AbilityScore("CHA", monster.cha, isEditingEnabled) { monster = monster.copy(cha = it) }
            }

            Hr { }

            // Skills & Senses
            Div({ classes(MonsterSheetStyle.section) }) {
                PropertyLine("Saving Throws", monster.savingThrows ?: "", isEditingEnabled) { monster = monster.copy(savingThrows = it) }
                PropertyLine("Skills", monster.skills ?: "", isEditingEnabled) { monster = monster.copy(skills = it) }
                PropertyLine("Senses", monster.senses, isEditingEnabled) { monster = monster.copy(senses = it) }
                PropertyLine("Languages", monster.languages, isEditingEnabled) { monster = monster.copy(languages = it) }
                PropertyLine("Challenge", monster.challenge, isEditingEnabled) { monster = monster.copy(challenge = it) }
            }

            Hr { }

            // Traits
            Div({ classes(MonsterSheetStyle.section) }) {
                monster.traits.forEachIndexed { index, trait ->
                    TraitBlock(
                        trait.name, 
                        trait.description, 
                        isEditingEnabled,
                        onNameChange = { newName -> 
                            val newTraits = monster.traits.toMutableList()
                            newTraits[index] = trait.copy(name = newName)
                            monster = monster.copy(traits = newTraits)
                        },
                        onDescChange = { newDesc ->
                            val newTraits = monster.traits.toMutableList()
                            newTraits[index] = trait.copy(description = newDesc)
                            monster = monster.copy(traits = newTraits)
                        },
                        onDelete = {
                            val newTraits = monster.traits.toMutableList()
                            newTraits.removeAt(index)
                            monster = monster.copy(traits = newTraits)
                        }
                    )
                }
                if (isEditingEnabled) {
                    AddButton("Add Trait") {
                        val newTraits = monster.traits.toMutableList()
                        newTraits.add(TraitDto("New Trait", "Description"))
                        monster = monster.copy(traits = newTraits)
                    }
                }
            }

            // Actions
            H3({ classes(MonsterSheetStyle.sectionHeader) }) { Text("Actions") }
            Div({ classes(MonsterSheetStyle.section) }) {
                 monster.actions.forEachIndexed { index, action ->
                    TraitBlock(
                        action.name, 
                        action.description, 
                        isEditingEnabled,
                        onNameChange = { newName -> 
                            val newActions = monster.actions.toMutableList()
                            newActions[index] = action.copy(name = newName)
                            monster = monster.copy(actions = newActions)
                        },
                        onDescChange = { newDesc ->
                            val newActions = monster.actions.toMutableList()
                            newActions[index] = action.copy(description = newDesc)
                            monster = monster.copy(actions = newActions)
                        },
                        onDelete = {
                            val newActions = monster.actions.toMutableList()
                            newActions.removeAt(index)
                            monster = monster.copy(actions = newActions)
                        }
                    )
                }
                if (isEditingEnabled) {
                    AddButton("Add Action") {
                        val newActions = monster.actions.toMutableList()
                        newActions.add(TraitDto("New Action", "Description"))
                        monster = monster.copy(actions = newActions)
                    }
                }
            }
        }
    }
}