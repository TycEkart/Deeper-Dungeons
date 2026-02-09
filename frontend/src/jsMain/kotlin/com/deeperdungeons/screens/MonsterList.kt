package com.deeperdungeons.screens

import androidx.compose.runtime.*
import com.example.shared.MonsterDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlinx.browser.window
import com.deeperdungeons.api.fetchAllMonsters
import com.deeperdungeons.api.saveMonster
import com.deeperdungeons.styles.MonsterSheetStyle

@Composable
fun MonsterList() {
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

    Div({ classes(MonsterSheetStyle.mainContainer) }) {
        H1 { Text("Deeper Dungeons - Monsters") }
        
        Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(10.px) } }) {
            monsters.forEach { monster ->
                Div({
                    style {
                        padding(10.px)
                        border(1.px, LineStyle.Solid, Color("#ccc"))
                        borderRadius(5.px)
                        cursor("pointer")
                        backgroundColor(Color.white)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(10.px)
                    }
                    onClick {
                        window.location.href = "?id=${monster.id}"
                    }
                }) {
                    if (monster.imageUrl != null) {
                        Img(src = "http://localhost:8090${monster.imageUrl}", alt = monster.name) {
                            style {
                                width(50.px)
                                height(50.px)
                                property("object-fit", "cover")
                                borderRadius(50.percent)
                            }
                        }
                    } else {
                        Div({
                            style {
                                width(50.px)
                                height(50.px)
                                backgroundColor(Color("#eee"))
                                borderRadius(50.percent)
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.Center)
                                alignItems(AlignItems.Center)
                                fontSize(20.px)
                                color(Color("#aaa"))
                            }
                        }) { Text("?") }
                    }
                    
                    Div {
                        Div { B { Text(monster.name) } }
                        Span({ style { color(Color.gray); fontSize(12.px) } }) {
                            Text(monster.meta)
                        }
                    }
                }
            }
            
             Button(attrs = {
                style {
                    marginTop(20.px)
                    padding(10.px)
                    cursor("pointer")
                }
                onClick {
                     // Create a new empty monster
                     val newMonster = MonsterDto(
                         name = "New Monster",
                         meta = "Size, Type, Alignment",
                         armorClass = "10",
                         hitPoints = "10 (2d8 + 2)",
                         speed = "30 ft.",
                         str = "10 (+0)",
                         dex = "10 (+0)",
                         con = "10 (+0)",
                         int = "10 (+0)",
                         wis = "10 (+0)",
                         cha = "10 (+0)",
                         senses = "passive Perception 10",
                         languages = "-",
                         challenge = "0 (10 XP)"
                     )
                     scope.launch {
                         try {
                             val savedMonster = saveMonster(newMonster)
                             window.location.href = "?id=${savedMonster.id}"
                         } catch (e: Exception) {
                             console.error("Failed to create monster", e)
                             window.alert("Failed to create new monster")
                         }
                     }
                }
            }) {
                Text("Create New Monster")
            }
        }
    }
}