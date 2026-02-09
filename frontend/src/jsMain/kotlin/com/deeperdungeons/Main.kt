package com.deeperdungeons

import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposable
import kotlinx.browser.window
import com.deeperdungeons.screens.details.MonsterDetail
import com.deeperdungeons.screens.MonsterList
import com.deeperdungeons.styles.MonsterSheetStyle
import org.w3c.dom.url.URLSearchParams

fun main() {
    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        
        // Simple routing based on URL query param
        val urlParams = URLSearchParams(window.location.search)
        val monsterId = urlParams.get("id")?.toIntOrNull()

        if (monsterId != null) {
            MonsterDetail(monsterId)
        } else {
            MonsterList()
        }
    }
}