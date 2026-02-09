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
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import kotlinx.browser.window

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
                    onClick {
                        if (monster.id != null) {
                            onMonsterClick(monster.id!!)
                        }
                    }
                }) {
                    if (monster.imageUrl != null) {
                        Img(src = "http://localhost:8090${monster.imageUrl}", alt = monster.name) {
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
        }
    }
}