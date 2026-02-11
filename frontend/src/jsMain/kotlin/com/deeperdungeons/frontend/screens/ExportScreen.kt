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
import kotlin.math.floor

@Composable
fun ExportScreen(monsterId: Int, onBack: () -> Unit) {
    var monster by remember { mutableStateOf<MonsterDto?>(null) }
    val scope = rememberCoroutineScope()
    
    data class PostcardItem(
        val id: Double, 
        val type: String, 
        val label: String, 
        val value: String,
        val children: List<PostcardItem> = emptyList()
    )
    var postcardItems by remember { mutableStateOf(listOf<PostcardItem>()) }
    var isLandscape by remember { mutableStateOf(false) }
    var fontSizeScale by remember { mutableStateOf(1.0) }
    var paddingScale by remember { mutableStateOf(1.0) }
    var marginScale by remember { mutableStateOf(1.0) }

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
            
            // Special item: Line Break
            draggableOptions.add(DraggableOption("SEPARATOR", "Line Break", ""))
            
            draggableOptions.add(DraggableOption("PROPERTY", "Name", m.name))
            draggableOptions.add(DraggableOption("PROPERTY", "HP", m.hitPoints))
            draggableOptions.add(DraggableOption("PROPERTY", "AC", "${m.armorClass.value}"))
            draggableOptions.add(DraggableOption("PROPERTY", "Speed", m.speed))
            
            // Stats Block
            val statsObj = js("{}")
            statsObj["STR"] = m.str.value
            statsObj["DEX"] = m.dex.value
            statsObj["CON"] = m.con.value
            statsObj["INT"] = m.int.value
            statsObj["WIS"] = m.wis.value
            statsObj["CHA"] = m.cha.value
            draggableOptions.add(DraggableOption("STATS_BLOCK", "Stats", JSON.stringify(statsObj)))
            
            if (!m.savingThrows.isNullOrBlank()) {
                draggableOptions.add(DraggableOption("PROPERTY", "Saving Throws", m.savingThrows!!))
            }
            if (!m.skills.isNullOrBlank()) {
                draggableOptions.add(DraggableOption("PROPERTY", "Skills", m.skills!!))
            }
            
            draggableOptions.add(DraggableOption("PROPERTY", "Challenge", m.challenge))
            draggableOptions.add(DraggableOption("PROPERTY", "Senses", m.senses))
            draggableOptions.add(DraggableOption("PROPERTY", "Languages", m.languages))
            
            if (m.traits.isNotEmpty()) {
                draggableOptions.add(DraggableOption("GROUP_TRAITS", "All Traits", "Drag to add all traits"))
                m.traits.forEach {
                    draggableOptions.add(DraggableOption("TRAIT", it.name, it.description))
                }
            }
            
            if (m.actions.isNotEmpty()) {
                draggableOptions.add(DraggableOption("GROUP_ACTIONS", "All Actions", "Drag to add all actions"))
                m.actions.forEach { 
                    draggableOptions.add(DraggableOption("ACTION", it.name, it.description)) 
                }
            }
            
            if (m.reactions.isNotEmpty()) {
                draggableOptions.add(DraggableOption("GROUP_REACTIONS", "All Reactions", "Drag to add all reactions"))
                m.reactions.forEach { 
                    draggableOptions.add(DraggableOption("REACTION", it.name, it.description)) 
                }
            }

            draggableOptions.forEach { option ->
                // Check if item already exists (match type, label and value to be sure)
                // Separators can be added multiple times
                // Groups are always draggable (they add missing items or create a group)
                val isGroup = option.type.startsWith("GROUP_")
                val isAlreadyAdded = if (option.type == "SEPARATOR" || isGroup) false else postcardItems.any { 
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
                        if (isGroup) {
                            backgroundColor(Color("#f0e6d2")) // Slightly different color for groups
                            border(1.px, LineStyle.Dashed, Color("#58180d"))
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
                    if (option.type == "SEPARATOR") {
                        Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { Text("--- Line Break ---") }
                    } else if (option.type == "STATS_BLOCK") {
                        Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { Text("Ability Scores") }
                        Span({ style { fontSize(12.px) } }) { Text("STR | DEX | CON | INT | WIS | CHA") }
                    } else {
                        Div({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(5.px) } }) {
                            if (isGroup) {
                                Span({ 
                                    style { 
                                        fontSize(20.px)
                                        color(Color("#58180d"))
                                        lineHeight(1.em)
                                    } 
                                }) { Text("•") }
                            }
                            
                            Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { 
                                val prefix = when(option.type) {
                                    "ACTION" -> "[A] "
                                    "REACTION" -> "[R] "
                                    "TRAIT" -> "[T] "
                                    else -> ""
                                }
                                Text(prefix + option.label) 
                            }
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
            }
            
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(20.px)
                    width(100.percent)
                }
                onClick {
                    val newItems = mutableListOf<PostcardItem>()
                    var currentId = Date.now()
                    
                    draggableOptions.forEach { option ->
                        if (option.type != "SEPARATOR" && !option.type.startsWith("GROUP_")) {
                            newItems.add(PostcardItem(
                                id = currentId++,
                                type = option.type,
                                label = option.label,
                                value = option.value
                            ))
                        }
                    }
                    postcardItems = newItems
                }
            }) { Text("Add All Items") }
            
            Div({ style { display(DisplayStyle.Flex); gap(10.px); marginTop(20.px); alignItems(AlignItems.Center) } }) {
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick { fontSizeScale = (fontSizeScale - 0.1).coerceAtLeast(0.5) }
                }) { Text("A-") }
                
                Span { Text("${(fontSizeScale * 100).toInt()}%") }
                
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick { fontSizeScale = (fontSizeScale + 0.1).coerceAtMost(2.0) }
                }) { Text("A+") }
            }

            Div({ style { display(DisplayStyle.Flex); gap(10.px); marginTop(10.px); alignItems(AlignItems.Center) } }) {
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick { paddingScale = (paddingScale - 0.1).coerceAtLeast(0.0) }
                }) { Text("P-") }
                
                Span { Text("${(paddingScale * 100).toInt()}%") }
                
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick { paddingScale = (paddingScale + 0.1).coerceAtMost(2.0) }
                }) { Text("P+") }
            }

            Div({ style { display(DisplayStyle.Flex); gap(10.px); marginTop(10.px); alignItems(AlignItems.Center) } }) {
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick { marginScale = (marginScale - 0.1).coerceAtLeast(0.0) }
                }) { Text("M-") }
                
                Span { Text("${(marginScale * 100).toInt()}%") }
                
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick { marginScale = (marginScale + 0.1).coerceAtMost(2.0) }
                }) { Text("M+") }
            }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(10.px)
                    width(100.percent)
                }
                onClick {
                    isLandscape = !isLandscape
                }
            }) { Text(if (isLandscape) "Switch to Portrait" else "Switch to Landscape") }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    marginTop(10.px)
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
                    if (isLandscape) {
                        property("width", "14.8cm")
                        property("height", "10cm")
                    } else {
                        property("width", "10cm")
                        property("height", "14.8cm")
                    }
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
                                // Skip headers (if any left, though we moved to groups)
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

                            // Handle Group Drops
                            if (type.startsWith("GROUP_")) {
                                if (source == "list") {
                                    // Create a new group item
                                    val childrenItems = mutableListOf<PostcardItem>()
                                    var currentId = Date.now()
                                    
                                    when (type) {
                                        "GROUP_TRAITS" -> {
                                            m.traits.forEach { 
                                                childrenItems.add(PostcardItem(currentId++, "TRAIT", it.name, it.description))
                                            }
                                        }
                                        "GROUP_ACTIONS" -> {
                                            m.actions.forEach { 
                                                childrenItems.add(PostcardItem(currentId++, "ACTION", it.name, it.description))
                                            }
                                        }
                                        "GROUP_REACTIONS" -> {
                                            m.reactions.forEach { 
                                                childrenItems.add(PostcardItem(currentId++, "REACTION", it.name, it.description))
                                            }
                                        }
                                    }
                                    
                                    val groupItem = PostcardItem(
                                        id = id,
                                        type = type,
                                        label = label,
                                        value = value,
                                        children = childrenItems
                                    )
                                    
                                    val newList = postcardItems.toMutableList()
                                    if (insertIndex > newList.size) insertIndex = newList.size
                                    newList.add(insertIndex, groupItem)
                                    postcardItems = newList
                                } else if (source == "postcard") {
                                    // Reordering a group
                                    val oldIndex = postcardItems.indexOfFirst { it.id == id }
                                    if (oldIndex != -1) {
                                        val item = postcardItems[oldIndex]
                                        val newList = postcardItems.toMutableList()
                                        newList.removeAt(oldIndex)
                                        
                                        if (insertIndex > oldIndex) {
                                            insertIndex--
                                        }
                                        
                                        if (insertIndex > newList.size) insertIndex = newList.size
                                        if (insertIndex < 0) insertIndex = 0
                                        
                                        newList.add(insertIndex, item)
                                        postcardItems = newList
                                    }
                                }
                                return@onDrop
                            }

                            // Enforce grouping for individual items (ACTION, REACTION, TRAIT)
                            // If we drop an individual item, we still enforce grouping relative to other individual items
                            // But we ignore groups in this check for simplicity, or treat them as blocks.
                            // Actually, if we have groups, we probably shouldn't be mixing individual items of the same type easily.
                            // But let's keep the logic for individual items just in case.
                            
                            val groupTypes = listOf("ACTION", "REACTION", "TRAIT")
                            
                            for (groupType in groupTypes) {
                                val existingIndices = postcardItems.mapIndexedNotNull { index, item ->
                                    if (item.type == groupType && (source != "postcard" || item.id != id)) index else null
                                }
                                
                                if (existingIndices.isNotEmpty()) {
                                    val minIndex = existingIndices.minOrNull()!!
                                    val maxIndex = existingIndices.maxOrNull()!!
                                    
                                    if (type != groupType) {
                                        if (insertIndex > minIndex && insertIndex <= maxIndex) {
                                            val distToMin = insertIndex - minIndex
                                            val distToMax = (maxIndex + 1) - insertIndex
                                            if (distToMin < distToMax) insertIndex = minIndex else insertIndex = maxIndex + 1
                                        }
                                    } else {
                                        if (insertIndex < minIndex) insertIndex = minIndex
                                        else if (insertIndex > maxIndex + 1) insertIndex = maxIndex + 1
                                    }
                                }
                            }

                            if (source == "list") {
                                if (type == "SEPARATOR" || postcardItems.none { it.type == type && it.label == label && it.value == value }) {
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
                                val oldIndex = postcardItems.indexOfFirst { it.id == id }
                                if (oldIndex != -1) {
                                    val item = postcardItems[oldIndex]
                                    val newList = postcardItems.toMutableList()
                                    newList.removeAt(oldIndex)
                                    if (insertIndex > oldIndex) insertIndex--
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
                // We no longer need global flags for headers if we use groups, 
                // but for individual items we still do.
                var actionsHeaderRendered = false
                var reactionsHeaderRendered = false

                postcardItems.forEach { item ->
                    // Render Headers for individual items (legacy support / mixed mode)
                    if (item.type == "ACTION" && !actionsHeaderRendered) {
                        H3({ 
                            classes(MonsterSheetStyle.sectionHeader) 
                            style { 
                                width(100.percent)
                                fontSize((18 * fontSizeScale).px)
                                marginBottom((5 * marginScale).px)
                                marginTop((15 * marginScale).px)
                            }
                        }) { Text("Actions") }
                        actionsHeaderRendered = true
                    }
                    if (item.type == "REACTION" && !reactionsHeaderRendered) {
                        H3({ 
                            classes(MonsterSheetStyle.sectionHeader)
                            style { 
                                width(100.percent)
                                fontSize((18 * fontSizeScale).px)
                                marginBottom((5 * marginScale).px)
                                marginTop((15 * marginScale).px)
                            }
                        }) { Text("Reactions") }
                        reactionsHeaderRendered = true
                    }

                    Div({
                        draggable(Draggable.True)
                        attr("data-is-item", "true")
                        style {
                            width(100.percent)
                            marginBottom((2 * paddingScale).px)
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
                            
                            // If it's a group, we might want column layout for content
                            if (item.type.startsWith("GROUP_")) {
                                flexDirection(FlexDirection.Column)
                                alignItems(AlignItems.Start)
                            }
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
                        Div({ style { flex(1); width(100.percent) } }) {
                            if (item.type.startsWith("GROUP_")) {
                                // Render Group Header
                                val headerText = when(item.type) {
                                    "GROUP_ACTIONS" -> "Actions"
                                    "GROUP_REACTIONS" -> "Reactions"
                                    else -> "" // Traits don't have header
                                }
                                
                                if (headerText.isNotEmpty()) {
                                    H3({ 
                                        classes(MonsterSheetStyle.sectionHeader) 
                                        style { 
                                            width(100.percent)
                                            fontSize((18 * fontSizeScale).px)
                                            marginBottom((5 * marginScale).px)
                                            marginTop((15 * marginScale).px)
                                        }
                                    }) { Text(headerText) }
                                }
                                
                                // Render Children
                                item.children.forEach { child ->
                                    Div({ 
                                        style { 
                                            display(DisplayStyle.Flex)
                                            justifyContent(JustifyContent.SpaceBetween)
                                            alignItems(AlignItems.Start)
                                            width(100.percent)
                                        } 
                                    }) {
                                        Div({ style { flex(1) } }) {
                                            Div({ 
                                                classes(MonsterSheetStyle.traitBlock)
                                                style { 
                                                    fontSize((14 * fontSizeScale).px)
                                                    marginBottom((5 * marginScale).px)
                                                    lineHeight((1.0 * marginScale).em)
                                                }
                                            }) {
                                                Span({ classes(MonsterSheetStyle.traitName) }) { Text("${child.label}.") }
                                                Text(child.value)
                                            }
                                        }
                                        
                                        // Delete button for child
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
                                                // Remove child from group
                                                val newChildren = item.children.filter { it.id != child.id }
                                                val newItem = item.copy(children = newChildren)
                                                val index = postcardItems.indexOfFirst { it.id == item.id }
                                                if (index != -1) {
                                                    val newList = postcardItems.toMutableList()
                                                    if (newChildren.isEmpty()) {
                                                        newList.removeAt(index) // Remove empty group
                                                    } else {
                                                        newList[index] = newItem
                                                    }
                                                    postcardItems = newList
                                                }
                                            }
                                            onMouseEnter { (it.target as HTMLElement).style.opacity = "1" }
                                            onMouseLeave { (it.target as HTMLElement).style.opacity = "0.3" }
                                        }) { Text("×") }
                                    }
                                }
                            } else {
                                // Render Individual Item
                                when (item.type) {
                                    "SEPARATOR" -> {
                                        Div({
                                            classes(MonsterSheetStyle.taperedRule)
                                            style {
                                                marginTop((2 * paddingScale).px)
                                                marginBottom((2 * paddingScale).px)
                                            }
                                        }) {}
                                    }
                                    "STATS_BLOCK" -> {
                                        val stats = JSON.parse<dynamic>(item.value)
                                        val statNames = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA")
                                        
                                        Div({
                                            classes(MonsterSheetStyle.abilityScoreContainer)
                                            style {
                                                padding(0.px)
                                                marginBottom((5 * marginScale).px)
                                            }
                                        }) {
                                            statNames.forEach { statName ->
                                                val score = stats[statName] as Int
                                                val modifier = floor((score - 10) / 2.0).toInt()
                                                val sign = if (modifier >= 0) "+" else ""
                                                
                                                Div({
                                                    classes(MonsterSheetStyle.abilityScore)
                                                    style { property("width", "auto") }
                                                }) {
                                                    Span({ 
                                                        classes(MonsterSheetStyle.abilityScoreLabel)
                                                        style { fontSize((12 * fontSizeScale).px) }
                                                    }) { Text(statName) }
                                                    Span({ 
                                                        style { fontSize((14 * fontSizeScale).px) }
                                                    }) { Text("$score ($sign$modifier)") }
                                                }
                                            }
                                        }
                                    }
                                    "PROPERTY" -> {
                                        if (item.label == "Name") {
                                            H1({ 
                                                classes(MonsterSheetStyle.monsterName)
                                                style { 
                                                    fontSize((24 * fontSizeScale).px) 
                                                    margin(0.px)
                                                    marginBottom((5 * marginScale).px)
                                                    lineHeight((1.0 * marginScale).em)
                                                } 
                                            }) { Text(item.value) }
                                        } else {
                                            Div({ 
                                                classes(MonsterSheetStyle.propertyLine)
                                                style { 
                                                    alignItems(AlignItems.Baseline)
                                                    margin(0.px)
                                                    marginBottom((2 * marginScale).px)
                                                    fontSize((14 * fontSizeScale).px)
                                                    lineHeight((1.0 * marginScale).em)
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
                                    "ACTION", "REACTION", "TRAIT" -> {
                                        Div({ 
                                            classes(MonsterSheetStyle.traitBlock)
                                            style { 
                                                fontSize((14 * fontSizeScale).px)
                                                marginBottom((5 * marginScale).px)
                                                lineHeight((1.0 * marginScale).em)
                                            }
                                        }) {
                                            Span({ classes(MonsterSheetStyle.traitName) }) { Text("${item.label}.") }
                                            Text(item.value)
                                        }
                                    }
                                }
                            }
                        }

                        // Delete button for parent item
                        if (!item.type.startsWith("GROUP_")) {
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
                        } else {
                            // Delete button for group (removes whole group)
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
                                    alignSelf(AlignSelf.FlexStart) // Align to top for groups
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
}
