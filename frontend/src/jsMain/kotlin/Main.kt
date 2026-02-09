import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposable
import kotlinx.browser.window
import screens.details.MonsterDetail
import screens.MonsterList
import styles.MonsterSheetStyle

fun main() {
    renderComposable(rootElementId = "root") {
        Style(MonsterSheetStyle)
        
        // Simple routing based on URL query param
        val urlParams = org.w3c.dom.url.URLSearchParams(window.location.search)
        val monsterId = urlParams.get("id")?.toIntOrNull()

        if (monsterId != null) {
            MonsterDetail(monsterId)
        } else {
            MonsterList()
        }
    }
}