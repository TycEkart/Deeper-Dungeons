package com.deeperdungeons.frontend.screens

import androidx.compose.runtime.*
import com.deeperdungeons.shared.Alignment
import com.deeperdungeons.shared.ArmorClassDto
import com.deeperdungeons.shared.MonsterDto
import com.deeperdungeons.shared.MonsterSize
import com.deeperdungeons.shared.MonsterType
import com.deeperdungeons.shared.StatDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import com.deeperdungeons.frontend.api.fetchAllMonsters
import com.deeperdungeons.frontend.api.saveMonster
import com.deeperdungeons.frontend.api.deleteMonster
import com.deeperdungeons.frontend.api.importMonster
import com.deeperdungeons.frontend.api.shutdownBackend
import com.deeperdungeons.frontend.api.getBaseUrl
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.target
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
fun MonsterList(onMonsterClick: (Int) -> Unit, onGenerateClick: () -> Unit) {
    var monsters by remember { mutableStateOf<List<MonsterDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                monsters = fetchAllMonsters()
            } catch (e: Exception) {
                console.error("Failed to fetch monsters", e)
            }
        }
    }

    Div({ classes(MonsterSheetStyle.listContainer) }) {
        H1({ classes(MonsterSheetStyle.monsterName) }) { Text("Deeper Dungeons - Monsters") }
        
        Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(10.px) } }) {
            monsters.forEach { monster ->
                Div({
                    classes(MonsterSheetStyle.listItem)
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.SpaceBetween)
                    }
                }) {
                    // Clickable area for opening the monster details
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(10.px)
                            flex(1) // Take up remaining space
                            cursor("pointer")
                        }
                        onClick {
                            if (monster.id != null) {
                                onMonsterClick(monster.id!!)
                            }
                        }
                    }) {
                        if (monster.imageUrl != null) {
                            Img(src = "${getBaseUrl()}${monster.imageUrl}", alt = monster.name) {
                                style {
                                    width(50.px)
                                    height(50.px)
                                    property("object-fit", "cover")
                                    borderRadius(50.percent)
                                    border(1.px, LineStyle.Solid, Color("#58180d"))
                                }
                            }
                        } else {
                            Div({
                                style {
                                    width(50.px)
                                    height(50.px)
                                    backgroundColor(Color("#fdf1dc"))
                                    borderRadius(50.percent)
                                    border(1.px, LineStyle.Solid, Color("#58180d"))
                                    display(DisplayStyle.Flex)
                                    justifyContent(JustifyContent.Center)
                                    alignItems(AlignItems.Center)
                                    fontSize(20.px)
                                    color(Color("#58180d"))
                                }
                            }) { Text("?") }
                        }
                        
                        Div {
                            Div { 
                                Span({ 
                                    style { 
                                        fontWeight("bold")
                                        color(Color("#58180d"))
                                        fontSize(18.px)
                                        fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
                                        property("font-variant", "small-caps")
                                    } 
                                }) { Text(monster.name) } 
                            }
                            Span({ style { color(Color.black); fontSize(12.px); fontStyle("italic") } }) {
                                Text("${monster.size.label} ${monster.type.label}, ${monster.alignment.label}")
                            }
                        }
                    }

                    // Delete Button
                    Button(attrs = {
                        style {
                            backgroundColor(Color.transparent)
                            border(0.px)
                            color(Color("#58180d"))
                            fontSize(18.px)
                            cursor("pointer")
                            padding(5.px)
                            marginLeft(10.px)
                        }
                        onClick {
                            if (window.confirm("Are you sure you want to delete '${monster.name}'?")) {
                                scope.launch {
                                    try {
                                        if (monster.id != null) {
                                            deleteMonster(monster.id!!)
                                            monsters = fetchAllMonsters() // Refresh list
                                        }
                                    } catch (e: Exception) {
                                        console.error("Failed to delete monster", e)
                                        window.alert("Failed to delete monster")
                                    }
                                }
                            }
                        }
                        title("Delete Monster")
                    }) {
                        Text("✕")
                    }
                }
            }
            
             Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(20.px)
                    width(100.percent)
                }
                onClick {
                     // Create a new empty monster
                     val newMonster = MonsterDto(
                         name = "New Monster",
                         size = MonsterSize.Medium,
                         type = MonsterType.Humanoid,
                         alignment = Alignment.Unaligned,
                         armorClass = ArmorClassDto(10, null),
                         hitPoints = "10 (2d8 + 2)",
                         speed = "30 ft.",
                         str = StatDto(10, 0),
                         dex = StatDto(10, 0),
                         con = StatDto(10, 0),
                         int = StatDto(10, 0),
                         wis = StatDto(10, 0),
                         cha = StatDto(10, 0),
                         senses = "passive Perception 10",
                         languages = "-",
                         challenge = "0 (10 XP)"
                     )
                     scope.launch {
                         try {
                             val savedMonster = saveMonster(newMonster)
                             if (savedMonster.id != null) {
                                 onMonsterClick(savedMonster.id!!)
                             }
                         } catch (e: Exception) {
                             console.error("Failed to create monster", e)
                             window.alert("Failed to create new monster")
                         }
                     }
                }
            }) {
                Text("Create New Monster")
            }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(10.px)
                    width(100.percent)
                }
                onClick {
                    onGenerateClick()
                }
            }) {
                Text("Generate New Monster")
            }

            // Import Monster Button
            Div {
                val inputId = "import-monster-input"
                Input(InputType.File) {
                    id(inputId)
                    style { display(DisplayStyle.None) }
                    attr("accept", ".json")
                    onChange { event ->
                        val file = (event.target as HTMLInputElement).files?.item(0)
                        if (file != null) {
                            val reader = FileReader()
                            reader.onload = { e ->
                                val content = e.target.asDynamic().result as String
                                try {
                                    val importedMonster = Json.decodeFromString<MonsterDto>(content)
                                    scope.launch {
                                        try {
                                            val savedMonster = importMonster(importedMonster)
                                            monsters = fetchAllMonsters() // Refresh list
                                            if (savedMonster.id != null) {
                                                onMonsterClick(savedMonster.id!!)
                                            }
                                        } catch (e: Exception) {
                                            console.error("Failed to import monster", e)
                                            window.alert("Failed to import monster: ${e.message}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    console.error("Invalid JSON file", e)
                                    window.alert("Invalid JSON file")
                                }
                            }
                            reader.readAsText(file)
                        }
                        // Reset input value so the same file can be selected again
                        (event.target as HTMLInputElement).value = ""
                    }
                }

                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    style {
                        marginTop(10.px)
                        width(100.percent)
                    }
                    onClick {
                        window.document.getElementById(inputId)?.unsafeCast<HTMLInputElement>()?.click()
                    }
                }) {
                    Text("Import Monster")
                }
            }

            // Developer Tools Section
            Div({ style { marginTop(20.px); property("border-top", "1px solid #ccc"); paddingTop(10.px) } }) {
                Div({ style { fontSize(12.px); color(Color("#666")); marginBottom(5.px) } }) { Text("Developer Tools") }
                Div({ style { display(DisplayStyle.Flex); gap(10.px) } }) {
                    A(href = "${getBaseUrl()}/h2-console", attrs = {
                        target(ATarget.Blank)
                        classes(MonsterSheetStyle.dndButton)
                        style { textDecoration("none"); flex(1) }
                    }) {
                        Text("H2 Database")
                    }
                    
                    A(href = "${getBaseUrl()}/swagger-ui/index.html", attrs = {
                        target(ATarget.Blank)
                        classes(MonsterSheetStyle.dndButton)
                        style { textDecoration("none"); flex(1) }
                    }) {
                        Text("Swagger UI")
                    }
                }
            }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(20.px)
                    width(100.percent)
                    backgroundColor(Color.red)
                    color(Color.white)
                }
                onClick {
                    if (window.confirm("Are you sure you want to close the application?")) {
                        scope.launch {
                            shutdownBackend()
                            window.close() // This might not work in all browsers if not opened by script
                            // Fallback: show message
                            window.document.body?.innerHTML = "<h1>Application Closed</h1><p>You can close this tab now.</p>"
                        }
                    }
                }
            }) {
                Text("Exit Application")
            }
        }
    }
}
