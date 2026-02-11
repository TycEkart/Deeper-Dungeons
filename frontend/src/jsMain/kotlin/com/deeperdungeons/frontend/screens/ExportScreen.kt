package com.deeperdungeons.frontend.screens

import androidx.compose.runtime.*
import com.deeperdungeons.shared.MonsterDto
import com.deeperdungeons.frontend.api.fetchMonster
import com.deeperdungeons.frontend.components.html2canvas
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.Draggable
import kotlin.js.Date
import kotlin.js.JSON

@Composable
fun ExportScreen(monsterId: Int, onBack: () -> Unit) {
    var monster by remember { mutableStateOf<MonsterDto?>(null) }
    val scope = rememberCoroutineScope()
    
    data class PostcardItem(val id: Double, val type: String, val label: String, val value: String)
    var postcardItems by remember { mutableStateOf(listOf<PostcardItem>()) }

    LaunchedEffect(monsterId) {
        scope.launch {
            try {
                monster = fetchMonster(monsterId)
            } catch (e: Exception) {
                console.error("Failed to fetch monster", e)
            }
        }
    }

    if (monster == null) {
        Div({ style { padding(20.px); color(Color.white) } }) { Text("Loading...") }
        return
    }

    val m = monster!!

    Div({
        style {
            display(DisplayStyle.Flex)
            height(100.vh)
            padding(20.px)
            gap(20.px)
        }
    }) {
        // Left Panel: Draggable Items
        Div({
            classes(MonsterSheetStyle.listContainer)
            style {
                width(300.px)
                overflowY("auto")
                padding(10.px)
                property("margin", "0")
            }
        }) {
            H3({
                classes(MonsterSheetStyle.header)
                style { textAlign("center") }
            }) { Text("Drag items to postcard") }
            
            data class DraggableOption(val type: String, val label: String, val value: String)
            val draggableOptions = mutableListOf<DraggableOption>()
            
            draggableOptions.add(DraggableOption("PROPERTY", "Name", m.name))
            draggableOptions.add(DraggableOption("PROPERTY", "HP", m.hitPoints))
            draggableOptions.add(DraggableOption("PROPERTY", "AC", "${m.armorClass.value}"))
            draggableOptions.add(DraggableOption("PROPERTY", "Speed", m.speed))
            draggableOptions.add(DraggableOption("PROPERTY", "Stats", "STR ${m.str.value} | DEX ${m.dex.value} | CON ${m.con.value} | INT ${m.int.value} | WIS ${m.wis.value} | CHA ${m.cha.value}"))
            draggableOptions.add(DraggableOption("PROPERTY", "Challenge", m.challenge))
            draggableOptions.add(DraggableOption("PROPERTY", "Senses", m.senses))
            draggableOptions.add(DraggableOption("PROPERTY", "Languages", m.languages))
            
            m.actions.forEach { 
                draggableOptions.add(DraggableOption("ACTION", it.name, it.description)) 
            }
            
            m.reactions.forEach { 
                draggableOptions.add(DraggableOption("REACTION", it.name, it.description)) 
            }

            draggableOptions.forEach { option ->
                // Check if item already exists (match type, label and value to be sure)
                val isAlreadyAdded = postcardItems.any { 
                    it.type == option.type && it.label == option.label && it.value == option.value 
                }
                
                Div({
                    if (!isAlreadyAdded) {
                        draggable(Draggable.True)
                    }
                    classes(MonsterSheetStyle.listItem)
                    style {
                        cursor(if (isAlreadyAdded) "default" else "grab")
                        flexDirection(FlexDirection.Column)
                        alignItems(AlignItems.Start)
                        gap(5.px)
                        if (isAlreadyAdded) {
                            opacity(0.5)
                            backgroundColor(Color("#e0e0e0"))
                        }
                    }
                    if (!isAlreadyAdded) {
                        onDragStart { event ->
                            val dragData = js("{}")
                            dragData.source = "list"
                            dragData.type = option.type
                            dragData.label = option.label
                            dragData.value = option.value
                            event.dataTransfer?.setData("text/plain", JSON.stringify(dragData))
                        }
                    }
                }) {
                    Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { 
                        val prefix = when(option.type) {
                            "ACTION" -> "[A] "
                            "REACTION" -> "[R] "
                            else -> ""
                        }
                        Text(prefix + option.label) 
                    }
                    Span({ 
                        style { 
                            fontSize(14.px)
                            whiteSpace("nowrap")
                            overflow("hidden")
                            property("text-overflow", "ellipsis")
                            maxWidth(250.px)
                            display(DisplayStyle.Block)
                        } 
                    }) { Text(option.value) }
                }
            }
            
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(20.px)
                    width(100.percent)
                }
                onClick {
                    val element = document.getElementById("postcard-area") as? HTMLElement
                    if (element != null) {
                        val options = js("{}")
                        options.useCORS = true
                        options.allowTaint = true
                        html2canvas(element, options).then { canvas ->
                            val link = document.createElement("a") as HTMLAnchorElement
                            link.download = "${m.name.replace(" ", "_")}_postcard.png"
                            link.href = (canvas as HTMLCanvasElement).toDataURL()
                            link.click()
                            null
                        }
                    }
                }
            }) { Text("Download Postcard PNG") }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(10.px)
                    width(100.percent)
                }
                onClick { onBack() } 
            }) { Text("Back to Details") }
        }

        // Right Panel: Postcard Preview
        Div({
            style {
                flex(1)
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
            }
        }) {
            // The Postcard
            Div({
                id("postcard-area")
                classes(MonsterSheetStyle.sheetContainer)
                style {
                    property("width", "10cm")
                    property("height", "14.8cm")
                    position(Position.Relative)
                    overflow("hidden")
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    alignItems(AlignItems.Start)
                }
                onDragOver { event ->
                    event.preventDefault()
                }
                onDrop { event ->
                    event.preventDefault()
                    val data = event.dataTransfer?.getData("text/plain")
                    if (data != null) {
                        try {
                            val json = JSON.parse<dynamic>(data)
                            val source = json.source as String
                            val type = json.type as String
                            val label = json.label as String
                            val value = json.value as String
                            val id = if (source == "postcard") json.id as Double else Date.now()
                            
                            // Determine insertion index
                            val container = event.currentTarget as HTMLElement
                            val children = container.children
                            var insertIndex = postcardItems.size
                            
                            var itemIndex = 0
                            var found = false
                            for (i in 0 until children.length) {
                                val child = children.item(i) as HTMLElement
                                // Skip headers
                                if (!child.getAttribute("data-is-item").toBoolean()) continue
                                
                                val rect = child.getBoundingClientRect()
                                val childMiddleY = rect.top + rect.height / 2
                                if (event.clientY < childMiddleY) {
                                    insertIndex = itemIndex
                                    found = true
                                    break
                                }
                                itemIndex++
                            }
                            if (!found) insertIndex = postcardItems.size

                            // Enforce grouping for ACTION and REACTION
                            if (type == "ACTION" || type == "REACTION") {
                                val existingIndices = postcardItems.mapIndexedNotNull { index, item ->
                                    if (item.type == type && (source != "postcard" || item.id != id)) index else null
                                }
                                
                                if (existingIndices.isNotEmpty()) {
                                    val minIndex = existingIndices.minOrNull()!!
                                    val maxIndex = existingIndices.maxOrNull()!!
                                    
                                    // If insertIndex is outside the group, clamp it
                                    // Note: if reordering, we need to be careful about indices shifting
                                    // But since we calculate insertIndex based on visual position, it should be mostly fine.
                                    // However, if we remove the item first, indices shift.
                                    // Let's assume we are inserting into the list as it is (with the item potentially still in it if reordering)
                                    // If reordering, we will remove the old item later.
                                    
                                    // If reordering, the item is currently in the list.
                                    // If we move it within the group, it's fine.
                                    // If we move it outside, we clamp.
                                    
                                    if (insertIndex < minIndex) {
                                        insertIndex = minIndex
                                    } else if (insertIndex > maxIndex + 1) {
                                        insertIndex = maxIndex + 1
                                    }
                                }
                            }

                            if (source == "list") {
                                if (postcardItems.none { it.type == type && it.label == label && it.value == value }) {
                                    val newItem = PostcardItem(
                                        id = id,
                                        type = type,
                                        label = label,
                                        value = value
                                    )
                                    val newList = postcardItems.toMutableList()
                                    if (insertIndex > newList.size) insertIndex = newList.size
                                    newList.add(insertIndex, newItem)
                                    postcardItems = newList
                                }
                            } else if (source == "postcard") {
                                // Reordering
                                val oldIndex = postcardItems.indexOfFirst { it.id == id }
                                if (oldIndex != -1) {
                                    val item = postcardItems[oldIndex]
                                    val newList = postcardItems.toMutableList()
                                    newList.removeAt(oldIndex)
                                    
                                    // Adjust insertIndex if needed because of removal
                                    if (insertIndex > oldIndex) {
                                        insertIndex--
                                    }
                                    
                                    if (insertIndex > newList.size) insertIndex = newList.size
                                    if (insertIndex < 0) insertIndex = 0
                                    
                                    newList.add(insertIndex, item)
                                    postcardItems = newList
                                }
                            }
                        } catch (e: Exception) {
                            console.error("Error parsing drag data", e)
                        }
                    }
                }
            }) {
                var actionsHeaderRendered = false
                var reactionsHeaderRendered = false

                postcardItems.forEach { item ->
                    // Render Headers if needed
                    if (item.type == "ACTION" && !actionsHeaderRendered) {
                        H3({ 
                            classes(MonsterSheetStyle.sectionHeader) 
                            style { width(100.percent) }
                        }) { Text("Actions") }
                        actionsHeaderRendered = true
                    }
                    if (item.type == "REACTION" && !reactionsHeaderRendered) {
                        H3({ 
                            classes(MonsterSheetStyle.sectionHeader)
                            style { width(100.percent) }
                        }) { Text("Reactions") }
                        reactionsHeaderRendered = true
                    }

                    Div({
                        draggable(Draggable.True)
                        attr("data-is-item", "true") // Marker for drop logic
                        style {
                            width(100.percent)
                            marginBottom(5.px)
                            padding(2.px)
                            border(1.px, LineStyle.Dashed, Color.transparent)
                            cursor("move")
                            backgroundColor(Color.transparent)
                            property("user-select", "none")
                            fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
                            color(Color("#58180d"))
                            
                            display(DisplayStyle.Flex)
                            justifyContent(JustifyContent.SpaceBetween)
                            alignItems(AlignItems.Center)
                        }
                        onDragStart { event ->
                            val dragData = js("{}")
                            dragData.source = "postcard"
                            dragData.id = item.id
                            dragData.type = item.type
                            dragData.label = item.label
                            dragData.value = item.value
                            event.dataTransfer?.setData("text/plain", JSON.stringify(dragData))
                            event.dataTransfer?.effectAllowed = "move"
                        }
                        onMouseEnter { 
                            (it.target as HTMLElement).style.border = "1px dashed #58180d"
                            (it.target as HTMLElement).style.backgroundColor = "rgba(255, 255, 255, 0.5)"
                        }
                        onMouseLeave { 
                            (it.target as HTMLElement).style.border = "1px dashed transparent"
                            (it.target as HTMLElement).style.backgroundColor = "transparent"
                        }
                    }) {
                        // Content Container
                        Div({ style { flex(1) } }) {
                            when (item.type) {
                                "PROPERTY" -> {
                                    if (item.label == "Name") {
                                        H1({ 
                                            classes(MonsterSheetStyle.monsterName)
                                            style { 
                                                fontSize(24.px) 
                                                margin(0.px)
                                            } 
                                        }) { Text(item.value) }
                                    } else {
                                        Div({ 
                                            classes(MonsterSheetStyle.propertyLine)
                                            style { 
                                                alignItems(AlignItems.Baseline)
                                                margin(0.px)
                                            } 
                                        }) {
                                            Span({ classes(MonsterSheetStyle.propertyLabel) }) { Text("${item.label} ") }
                                            Span({ 
                                                style { 
                                                    color(Color("#58180d")) 
                                                    fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
                                                } 
                                            }) { Text(item.value) }
                                        }
                                    }
                                }
                                "ACTION", "REACTION" -> {
                                    Div({ classes(MonsterSheetStyle.traitBlock) }) {
                                        Span({ classes(MonsterSheetStyle.traitName) }) { Text("${item.label}.") }
                                        Text(item.value)
                                    }
                                }
                            }
                        }

                        // Delete button
                        Span({
                            style {
                                marginLeft(5.px)
                                color(Color.red)
                                cursor("pointer")
                                fontWeight("bold")
                                fontSize(14.px)
                                opacity(0.3)
                                property("transition", "opacity 0.2s")
                                padding(0.px, 5.px)
                            }
                            onClick {
                                postcardItems = postcardItems.filter { it.id != item.id }
                            }
                            onMouseEnter { (it.target as HTMLElement).style.opacity = "1" }
                            onMouseLeave { (it.target as HTMLElement).style.opacity = "0.3" }
                        }) { Text("×") }
                    }
                }
            }
        }
    }
}
