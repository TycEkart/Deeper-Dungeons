package com.deeperdungeons.frontend

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposable
import kotlinx.browser.window
import com.deeperdungeons.frontend.screens.details.MonsterDetail
import com.deeperdungeons.frontend.screens.MonsterList
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import org.w3c.dom.url.URLSearchParams

fun main() {
    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        
        var currentMonsterId by remember { mutableStateOf<Int?>(null) }

        // Initialize state from URL
        LaunchedEffect(Unit) {
            val urlParams = URLSearchParams(window.location.search)
            currentMonsterId = urlParams.get("id")?.toIntOrNull()
            
            // Listen for popstate events (back/forward button)
            window.addEventListener("popstate", {
                val params = URLSearchParams(window.location.search)
                currentMonsterId = params.get("id")?.toIntOrNull()
            })
        }

        // Function to handle navigation without full reload
        fun navigateTo(id: Int?) {
            currentMonsterId = id
            val url = if (id != null) "?id=$id" else "/"
            window.history.pushState(null, "", url)
        }

        if (currentMonsterId != null) {
            MonsterDetail(currentMonsterId!!, onBack = { navigateTo(null) })
        } else {
            MonsterList(onMonsterClick = { navigateTo(it) })
        }
    }
}