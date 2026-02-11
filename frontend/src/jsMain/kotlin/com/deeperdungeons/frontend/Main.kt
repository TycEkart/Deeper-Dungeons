package com.deeperdungeons.frontend

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposable
import kotlinx.browser.window
import com.deeperdungeons.frontend.screens.details.MonsterDetail
import com.deeperdungeons.frontend.screens.MonsterList
import com.deeperdungeons.frontend.screens.ExportScreen
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import com.deeperdungeons.frontend.screens.GenerateMonster
import org.w3c.dom.url.URLSearchParams

fun main() {
    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        
        var currentMonsterId by remember { mutableStateOf<Int?>(null) }
        var isGenerating by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }

        // Initialize state from URL
        LaunchedEffect(Unit) {
            val urlParams = URLSearchParams(window.location.search)
            currentMonsterId = urlParams.get("id")?.toIntOrNull()
            isGenerating = urlParams.get("generate") == "true"
            isExporting = urlParams.get("export") == "true"
            
            // Listen for popstate events (back/forward button)
            window.addEventListener("popstate", {
                val params = URLSearchParams(window.location.search)
                currentMonsterId = params.get("id")?.toIntOrNull()
                isGenerating = params.get("generate") == "true"
                isExporting = params.get("export") == "true"
            })
        }

        // Function to handle navigation without full reload
        fun navigateTo(id: Int?) {
            currentMonsterId = id
            isGenerating = false
            isExporting = false
            val url = if (id != null) "?id=$id" else "/"
            window.history.pushState(null, "", url)
        }
        
        fun navigateToGenerate() {
            currentMonsterId = null
            isGenerating = true
            isExporting = false
            window.history.pushState(null, "", "?generate=true")
        }

        fun navigateToExport(id: Int) {
            currentMonsterId = id
            isExporting = true
            isGenerating = false
            window.history.pushState(null, "", "?id=$id&export=true")
        }

        if (isExporting && currentMonsterId != null) {
            ExportScreen(
                monsterId = currentMonsterId!!,
                onBack = { navigateTo(currentMonsterId) }
            )
        } else if (isGenerating) {
            GenerateMonster(
                onBack = { navigateTo(null) },
                onCreated = { navigateTo(it) }
            )
        } else if (currentMonsterId != null) {
            MonsterDetail(
                id = currentMonsterId!!, 
                onBack = { navigateTo(null) },
                onExport = { navigateToExport(currentMonsterId!!) }
            )
        } else {
            MonsterList(
                onMonsterClick = { navigateTo(it) },
                onGenerateClick = { navigateToGenerate() }
            )
        }
    }
}
