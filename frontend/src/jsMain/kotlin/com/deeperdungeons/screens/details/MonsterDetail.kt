package com.deeperdungeons.screens.details

import androidx.compose.runtime.*
import com.example.shared.MonsterDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import com.deeperdungeons.api.fetchMonster
import com.deeperdungeons.api.saveMonster

@Composable
fun MonsterDetail(id: Int) {
    var monster by remember { mutableStateOf<MonsterDto?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        scope.launch {
            try {
                monster = fetchMonster(id)
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