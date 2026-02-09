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
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.readOnly
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun GenerateMonster(onBack: () -> Unit, onCreated: (Int) -> Unit) {
    var jsonInput by remember { mutableStateOf("") }
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
        
        Use the following JSON structure as a template. Ensure all fields are present.
        For enums (size, type, alignment), use the exact string values shown in the example.
        
        Example JSON Structure:
        $exampleJson
        
        Do not include the 'id' field.
        The 'meta' field is computed, so do not include it.
        Return ONLY the JSON object, no markdown formatting.
    """.trimIndent()

    Div({ classes(MonsterSheetStyle.mainContainer) }) {
        Div({ classes(MonsterSheetStyle.controlsContainer) }) {
             Div({ 
                style { cursor("pointer"); textDecoration("underline") }
                onClick { onBack() }
            }) {
                Text("← Back to List")
            }
        }

        H1 { Text("Generate Monster with AI") }

        Div({ style { marginBottom(20.px) } }) {
            P { Text("1. Copy this prompt and paste it into Gemini (or another LLM):") }
            TextArea(
                value = prompt,
                attrs = {
                    style { width(100.percent); height(200.px) }
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
            P { Text("2. Paste the generated JSON here:") }
            TextArea(
                value = jsonInput,
                attrs = {
                    style { width(100.percent); height(200.px) }
                    placeholder("Paste JSON here...")
                    onInput { jsonInput = it.value }
                }
            )
            
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style { marginTop(10.px) }
                onClick {
                    scope.launch {
                        try {
                            val json = Json { ignoreUnknownKeys = true }
                            // Clean up potential markdown code blocks if the user pastes them
                            val cleanJson = jsonInput.replace("```json", "").replace("```", "").trim()
                            val monsterDto = json.decodeFromString<MonsterDto>(cleanJson)
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