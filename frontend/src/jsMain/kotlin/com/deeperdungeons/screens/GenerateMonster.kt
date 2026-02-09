package com.deeperdungeons.screens

import androidx.compose.runtime.*
import com.deeperdungeons.frontend.api.saveMonster
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import com.deeperdungeons.shared.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.readOnly
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun GenerateMonster(onBack: () -> Unit, onCreated: (Int) -> Unit) {
    var jsonInput by remember { mutableStateOf("") }
    var monsterName by remember { mutableStateOf("") }
    var challengeRating by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    val exampleJson = remember {
        val example = MonsterDto(
            name = "Example Monster",
            size = MonsterSize.Medium,
            type = MonsterType.Humanoid,
            alignment = Alignment.NeutralEvil,
            armorClass = ArmorClassDto(14, "leather armor"),
            hitPoints = "22 (4d8 + 4)",
            speed = "30 ft.",
            str = StatDto(10, 0),
            dex = StatDto(10, 0),
            con = StatDto(10, 0),
            int = StatDto(10, 0),
            wis = StatDto(10, 0),
            cha = StatDto(10, 0),
            savingThrows = "Dex +2",
            skills = "Stealth +4",
            senses = "passive Perception 10",
            languages = "Common",
            challenge = "1/2 (100 XP)",
            traits = listOf(TraitDto("Trait Name", "Trait Description")),
            actions = listOf(TraitDto("Action Name", "Action Description")),
            reactions = listOf(TraitDto("Reaction Name", "Reaction Description"))
        )
        val json = Json { prettyPrint = true; encodeDefaults = true }
        json.encodeToString(example)
    }

    val prompt = """
        Generate a Dungeons & Dragons 5e monster in JSON format.
        ${if (monsterName.isNotBlank()) "Name: $monsterName" else ""}
        ${if (challengeRating.isNotBlank()) "Challenge Rating: $challengeRating" else ""}
        ${if (description.isNotBlank()) "Description/Concept: $description" else ""}
        
        Use the following JSON structure as a template. Ensure all fields are present.
        
        Allowed Enum Values:
        - Size: ${MonsterSize.values().joinToString(", ") { "\"${it.name}\"" }}
        - Type: ${MonsterType.values().joinToString(", ") { "\"${it.name}\"" }}
        - Alignment: ${Alignment.values().joinToString(", ") { "\"${it.name}\"" }}
        
        Example JSON Structure:
        $exampleJson
        
        Do not include the 'id' field.
        The 'meta' field is computed, so do not include it.
        Return ONLY the JSON object, no markdown formatting.
    """.trimIndent().split("\n").filter { it.isNotBlank() }.joinToString("\n")

    Div({ classes(MonsterSheetStyle.listContainer) }) {
        Div({ classes(MonsterSheetStyle.controlsContainer) }) {
             Div({ 
                style { cursor("pointer"); textDecoration("underline") }
                onClick { onBack() }
            }) {
                Text("← Back to List")
            }
        }

        H1({ classes(MonsterSheetStyle.monsterName) }) { Text("Generate Monster with AI") }

        Div({ style { marginBottom(20.px) } }) {
            P { Text("1. Enter Details:") }
            Div({ style { display(DisplayStyle.Flex); gap(20.px); marginBottom(10.px) } }) {
                Div({ style { flex(1) } }) {
                    Label { Text("Name (Mandatory):") }
                    Input(InputType.Text) {
                        classes(MonsterSheetStyle.inputField)
                        value(monsterName)
                        onInput { monsterName = it.value }
                        placeholder("Monster Name")
                    }
                }
                Div({ style { flex(1) } }) {
                    Label { Text("Challenge Rating (Mandatory):") }
                    Input(InputType.Text) {
                        classes(MonsterSheetStyle.inputField)
                        value(challengeRating)
                        onInput { challengeRating = it.value }
                        placeholder("e.g. 1/2 (100 XP)")
                    }
                }
            }
            Div {
                Label { Text("Description / Concept (Optional):") }
                TextArea(
                    value = description,
                    attrs = {
                        classes(MonsterSheetStyle.inputField)
                        style { height(60.px) }
                        placeholder("Describe the monster (e.g., 'A giant spider made of lava', 'A goblin wizard with a pet rat')")
                        onInput { description = it.value }
                    }
                )
            }
        }

        Div({ style { marginBottom(20.px) } }) {
            P { Text("2. Copy this prompt and paste it into Gemini (or another LLM):") }
            TextArea(
                value = prompt,
                attrs = {
                    style { width(100.percent); height(100.px) }
                    readOnly()
                }
            )
            Div({ style { display(DisplayStyle.Flex); gap(10.px); marginTop(10.px) } }) {
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick {
                        window.navigator.clipboard.writeText(prompt)
                        window.alert("Prompt copied to clipboard!")
                    }
                }) { Text("Copy Prompt") }
                
                A(href = "https://gemini.google.com/app", attrs = { 
                    target(ATarget.Blank)
                    classes(MonsterSheetStyle.dndButton)
                    style { textDecoration("none") }
                }) {
                    Text("Open Gemini")
                }
            }
        }

        Div {
            P { Text("3. Paste the generated JSON here:") }
            TextArea(
                value = jsonInput,
                attrs = {
                    style { width(100.percent); height(100.px) }
                    placeholder("Paste JSON here...")
                    onInput { jsonInput = it.value }
                }
            )
            
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style { marginTop(10.px) }
                onClick {
                    if (monsterName.isBlank() || challengeRating.isBlank()) {
                        window.alert("Name and Challenge Rating are mandatory.")
                        return@onClick
                    }

                    scope.launch {
                        try {
                            val json = Json { ignoreUnknownKeys = true }
                            // Clean up potential markdown code blocks if the user pastes them
                            val cleanJson = jsonInput.replace("```json", "").replace("```", "").trim()
                            var monsterDto = json.decodeFromString<MonsterDto>(cleanJson)
                            
                            // Override with mandatory fields
                            monsterDto = monsterDto.copy(
                                name = monsterName,
                                challenge = challengeRating
                            )
                            
                            val savedMonster = saveMonster(monsterDto)
                            if (savedMonster.id != null) {
                                onCreated(savedMonster.id!!)
                            }
                        } catch (e: Exception) {
                            console.error("Failed to parse or save monster", e)
                            window.alert("Failed to create monster: ${e.message}")
                        }
                    }
                }
            }) { Text("Create Monster") }
        }
    }
}