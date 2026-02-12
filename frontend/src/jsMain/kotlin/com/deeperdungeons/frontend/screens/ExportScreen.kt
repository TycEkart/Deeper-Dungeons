package com.deeperdungeons.frontend.screens

import androidx.compose.runtime.*
import com.deeperdungeons.shared.MonsterDto
import com.deeperdungeons.frontend.api.fetchMonster
import com.deeperdungeons.frontend.api.getBaseUrl
import com.deeperdungeons.frontend.components.html2canvas
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Node
import kotlinx.browser.document
import kotlinx.browser.window
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
        val children: List<PostcardItem> = emptyList(),
        val width: Double? = null
    )

    data class DraggableOption(val type: String, val label: String, val value: String)

    var postcardItems by remember { mutableStateOf(listOf<PostcardItem>()) }
    var isLandscape by remember { mutableStateOf(false) }
    var fontSizeScale by remember { mutableStateOf(1.0) }
    var paddingScale by remember { mutableStateOf(1.0) }
    var marginScale by remember { mutableStateOf(1.0) }
    var zoomScale by remember { mutableStateOf(1.0) }
    var dropIndicatorIndex by remember { mutableStateOf<Int?>(null) }
    var dropIndicatorType by remember { mutableStateOf("HORIZONTAL") }

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

    val draggableOptions = remember(m) {
        val options = mutableListOf<DraggableOption>()

        // Special item: Line
        options.add(DraggableOption("SEPARATOR", "Line", ""))
        options.add(DraggableOption("PROPERTY", "Name", m.name))

        if (!m.imageUrl.isNullOrBlank()) {
            val imgData = js("{}")
            val url = m.imageUrl!!
            if (url.startsWith("http") || url.startsWith("data:")) {
                imgData.url = url
            } else {
                val baseUrl = getBaseUrl()
                imgData.url = if (url.startsWith("/")) "$baseUrl$url" else "$baseUrl/$url"
            }
            imgData.scale = 1.0
            imgData.align = "right"
            options.add(DraggableOption("IMAGE", "Monster Image", JSON.stringify(imgData)))
        }

        options.add(DraggableOption("PROPERTY", "AC", "${m.armorClass.value}"))
        options.add(DraggableOption("PROPERTY", "HP", m.hitPoints))
        options.add(DraggableOption("PROPERTY", "Speed", m.speed))

        // Stats Block
        val statsObj = js("{}")
        statsObj["STR"] = m.str.value
        statsObj["DEX"] = m.dex.value
        statsObj["CON"] = m.con.value
        statsObj["INT"] = m.int.value
        statsObj["WIS"] = m.wis.value
        statsObj["CHA"] = m.cha.value
        options.add(DraggableOption("STATS_BLOCK", "Stats", JSON.stringify(statsObj)))

        if (!m.savingThrows.isNullOrBlank()) {
            options.add(DraggableOption("PROPERTY", "Saving Throws", m.savingThrows!!))
        }
        if (!m.skills.isNullOrBlank()) {
            options.add(DraggableOption("PROPERTY", "Skills", m.skills!!))
        }

        options.add(DraggableOption("PROPERTY", "Challenge", m.challenge))
        options.add(DraggableOption("PROPERTY", "Senses", m.senses))
        options.add(DraggableOption("PROPERTY", "Languages", m.languages))

        if (m.traits.isNotEmpty()) {
            options.add(DraggableOption("GROUP_TRAITS", "All Traits", "Drag to add all traits"))
            m.traits.forEach {
                options.add(DraggableOption("TRAIT", it.name, it.description))
            }
        }

        if (m.actions.isNotEmpty()) {
            options.add(DraggableOption("GROUP_ACTIONS", "All Actions", "Drag to add all actions"))
            m.actions.forEach {
                options.add(DraggableOption("ACTION", it.name, it.description))
            }
        }

        if (m.reactions.isNotEmpty()) {
            options.add(DraggableOption("GROUP_REACTIONS", "All Reactions", "Drag to add all reactions"))
            m.reactions.forEach {
                options.add(DraggableOption("REACTION", it.name, it.description))
            }
        }
        options
    }

    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            width(100.percent)
            height(100.percent)

            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            padding(20.px)
            gap(20.px)
            property("box-sizing", "border-box")
            backgroundColor(Color("#2b2b2b"))
            color(Color.white)
            overflow("hidden") // Prevent root scrolling
        }
    }) {
        // Top Menu Bar
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(15.px)
                alignItems(AlignItems.Center)
                width(100.percent)
                paddingBottom(10.px)
                property("border-bottom", "1px solid #ccc")
                flexWrap(FlexWrap.Wrap)
                flexShrink(0)
            }
        }) {
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                onClick { onBack() }
            }) { Text("Back") }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                onClick {
                    val newItems = mutableListOf<PostcardItem>()
                    var currentId = Date.now()

                    draggableOptions.forEach { option ->
                        if (option.type != "SEPARATOR" && !option.type.startsWith("GROUP_")) {
                            newItems.add(
                                PostcardItem(
                                    id = currentId++,
                                    type = option.type,
                                    label = option.label,
                                    value = option.value
                                )
                            )
                        }
                    }

                    val groupedItems = mutableListOf<PostcardItem>()

                    // Add properties
                    newItems.filter { it.type == "PROPERTY" || it.type == "STATS_BLOCK" || it.type == "IMAGE" }
                        .forEach { groupedItems.add(it) }

                    // Add Traits Group
                    val traits = newItems.filter { it.type == "TRAIT" }
                    if (traits.isNotEmpty()) {
                        groupedItems.add(PostcardItem(currentId++, "GROUP_TRAITS", "Traits", "", traits))
                    }

                    // Add Actions Group
                    val actions = newItems.filter { it.type == "ACTION" }
                    if (actions.isNotEmpty()) {
                        groupedItems.add(PostcardItem(currentId++, "GROUP_ACTIONS", "Actions", "", actions))
                    }

                    // Add Reactions Group
                    val reactions = newItems.filter { it.type == "REACTION" }
                    if (reactions.isNotEmpty()) {
                        groupedItems.add(PostcardItem(currentId++, "GROUP_REACTIONS", "Reactions", "", reactions))
                    }

                    postcardItems = groupedItems
                }
            }) { Text("Add All Items") }

            // Font Size
            ControlGroup(
                "Font", "${(fontSizeScale * 100).toInt()}%",
                { fontSizeScale = (fontSizeScale + 0.1).coerceAtMost(2.0) },
                { fontSizeScale = (fontSizeScale - 0.1).coerceAtLeast(0.5) }
            )

            // Padding
            ControlGroup(
                "Pad", "${(paddingScale * 100).toInt()}%",
                { paddingScale = (paddingScale + 0.1).coerceAtMost(2.0) },
                { paddingScale = (paddingScale - 0.1).coerceAtLeast(0.0) }
            )

            // Margin
            ControlGroup(
                "Marg", "${(marginScale * 100).toInt()}%",
                { marginScale = (marginScale + 0.1).coerceAtMost(2.0) },
                { marginScale = (marginScale - 0.1).coerceAtLeast(0.0) }
            )

            // Zoom
            ControlGroup(
                "Zoom", "${(zoomScale * 100).toInt()}%",
                { zoomScale = (zoomScale + 0.1).coerceAtMost(3.0) },
                { zoomScale = (zoomScale - 0.1).coerceAtLeast(0.5) }
            )

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                onClick { isLandscape = !isLandscape }
            }) { Text(if (isLandscape) "Portrait" else "Landscape") }

            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
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
            }) { Text("Download PNG") }
        }

        // Main Content
        Div({
            style {
                display(DisplayStyle.Flex)
                flex(1)
                gap(20.px)
                overflow("hidden")
                width(100.percent)
                height(100.percent)
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
                    height(100.percent)
                    flexShrink(0)
                }
            }) {
                H3({
                    classes(MonsterSheetStyle.header)
                    style { textAlign("center") }
                }) { Text("Drag items to postcard") }

                draggableOptions.forEach { option ->
                    val isGroup = option.type.startsWith("GROUP_")

                    // Check if item exists as top-level or inside a group
                    val isAlreadyAdded = if (option.type == "SEPARATOR" || isGroup) false else postcardItems.any {
                        (it.type == option.type && it.label == option.label && it.value == option.value) ||
                                (it.children.any { child -> child.type == option.type && child.label == option.label && child.value == option.value })
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
                                backgroundColor(Color("#f0e6d2"))
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
                        when (option.type) {
                            "SEPARATOR" -> {
                                Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { Text("--- Line ---") }
                            }

                            "STATS_BLOCK" -> {
                                Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { Text("Ability Scores") }
                                Span({ style { fontSize(12.px) } }) { Text("STR | DEX | CON | INT | WIS | CHA") }
                            }

                            "IMAGE" -> {
                                Span({ style { fontWeight("bold"); color(Color("#58180d")) } }) { Text("Monster Image") }
                            }

                            else -> {
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
                                        val prefix = when (option.type) {
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
                }
            }

            // Right Panel: Postcard Preview
            Div({
                style {
                    flex(1)
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                    overflow("hidden") // Prevent scrolling
                    height(100.percent)
                    backgroundColor(Color("#333"))
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
                        property("overflow", "visible")
                        display(DisplayStyle.Block)
                        property("transform", "scale($zoomScale)")
                    }
                    onDragOver { event ->
                        event.preventDefault()
                        val container = event.currentTarget as HTMLElement
                        val (index, type) = findDropTarget(
                            event.nativeEvent as org.w3c.dom.events.MouseEvent,
                            container.children
                        )
                        dropIndicatorIndex = index
                        dropIndicatorType = type
                    }
                    onDragLeave { event ->
                        val container = event.currentTarget as HTMLElement
                        val related = event.relatedTarget as? Node
                        if (related == null || !container.contains(related)) {
                            dropIndicatorIndex = null
                        }
                    }
                    onDrop { event ->
                        event.preventDefault()
                        dropIndicatorIndex = null
                        val data = event.dataTransfer?.getData("text/plain")
                        if (data != null) {
                            try {
                                val json = JSON.parse<dynamic>(data)
                                val source = json.source as String
                                val type = json.type as String
                                val label = json.label as String
                                val value = json.value as String
                                val id =
                                    if (source == "postcard" || source == "child") json.id as Double else Date.now()

                                if (source == "child") return@onDrop

                                // Determine insertion index
                                val container = event.currentTarget as HTMLElement
                                val (foundIndex, _) = findDropTarget(
                                    event.nativeEvent as org.w3c.dom.events.MouseEvent,
                                    container.children
                                )
                                var insertIndex = foundIndex

                                // Helper to merge into existing group
                                fun mergeIntoGroup(groupType: String, items: List<PostcardItem>) {
                                    val existingGroupIndex = postcardItems.indexOfFirst { it.type == groupType }
                                    if (existingGroupIndex != -1) {
                                        val group = postcardItems[existingGroupIndex]
                                        val newChildren = group.children.toMutableList()
                                        items.forEach { newItem ->
                                            if (newChildren.none { it.label == newItem.label && it.value == newItem.value }) {
                                                newChildren.add(newItem)
                                            }
                                        }
                                        val newGroup = group.copy(children = newChildren)
                                        val newList = postcardItems.toMutableList()
                                        newList[existingGroupIndex] = newGroup
                                        postcardItems = newList
                                    } else {
                                        // Create new group
                                        val groupLabel = when (groupType) {
                                            "GROUP_ACTIONS" -> "Actions"
                                            "GROUP_REACTIONS" -> "Reactions"
                                            "GROUP_TRAITS" -> "Traits"
                                            else -> ""
                                        }
                                        val newGroup = PostcardItem(Date.now(), groupType, groupLabel, "", items)
                                        val newList = postcardItems.toMutableList()
                                        if (insertIndex > newList.size) insertIndex = newList.size
                                        newList.add(insertIndex, newGroup)
                                        postcardItems = newList
                                    }
                                }

                                // Handle Group Drops (from list)
                                if (type.startsWith("GROUP_")) {
                                    if (source == "list") {
                                        val childrenItems = mutableListOf<PostcardItem>()
                                        var currentId = Date.now()
                                        when (type) {
                                            "GROUP_TRAITS" -> m.traits.forEach {
                                                childrenItems.add(
                                                    PostcardItem(
                                                        currentId++,
                                                        "TRAIT",
                                                        it.name,
                                                        it.description
                                                    )
                                                )
                                            }

                                            "GROUP_ACTIONS" -> m.actions.forEach {
                                                childrenItems.add(
                                                    PostcardItem(
                                                        currentId++,
                                                        "ACTION",
                                                        it.name,
                                                        it.description
                                                    )
                                                )
                                            }

                                            "GROUP_REACTIONS" -> m.reactions.forEach {
                                                childrenItems.add(
                                                    PostcardItem(
                                                        currentId++,
                                                        "REACTION",
                                                        it.name,
                                                        it.description
                                                    )
                                                )
                                            }
                                        }
                                        if (childrenItems.isNotEmpty()) {
                                            mergeIntoGroup(type, childrenItems)
                                        }
                                    } else if (source == "postcard") {
                                        // Reordering a group
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
                                    return@onDrop
                                }

                                // Handle Individual Item Drops (from list) -> Enforce into Group
                                if (source == "list" && (type == "ACTION" || type == "REACTION" || type == "TRAIT")) {
                                    val groupType = "GROUP_${type}S" // e.g. GROUP_ACTIONS
                                    val newItem = PostcardItem(id, type, label, value)
                                    mergeIntoGroup(groupType, listOf(newItem))
                                    return@onDrop
                                }

                                // Handle other items (PROPERTY, SEPARATOR, STATS_BLOCK, IMAGE)
                                if (source == "list") {
                                    if (type == "SEPARATOR" || postcardItems.none { it.type == type && it.label == label && it.value == value }) {
                                        val newItem = PostcardItem(id, type, label, value)
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
                    postcardItems.forEachIndexed { index, item ->
                        if (dropIndicatorIndex == index) {
                            if (dropIndicatorType == "HORIZONTAL") {
                                Div({
                                    style {
                                        width(100.percent)
                                        height(0.px)
                                        property("border-top", "2px dotted #58180d")
                                        margin(5.px, 0.px)
                                        property("clear", "both")
                                    }
                                })
                            } else {
                                Div({
                                    style {
                                        display(DisplayStyle.InlineBlock)
                                        width(0.px)
                                        height(20.px)
                                        property("border-left", "2px dotted #58180d")
                                        margin(0.px, 5.px)
                                        property("vertical-align", "middle")
                                    }
                                })
                            }
                        }
                        Div({
                            draggable(Draggable.True)
                            attr("data-is-item", "true")
                            style {
                                marginBottom((2 * paddingScale).px)
                                padding(2.px)
                                border(1.px, LineStyle.Dashed, Color.transparent)
                                cursor("move")
                                backgroundColor(Color.transparent)
                                property("user-select", "none")
                                fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
                                color(Color("#58180d"))
                                position(Position.Relative)

                                // Default to InlineBlock to allow side-by-side
                                display(DisplayStyle.InlineBlock)
                                property("vertical-align", "top")

                                if (item.width != null) {
                                    width(item.width.px)
                                }

                                if (item.type == "SEPARATOR") {
                                    if (item.width != null) {
                                        width(item.width.px)
                                    } else {
                                        width(100.percent)
                                    }
                                } else if (item.type == "IMAGE") {
                                    val imgData = JSON.parse<dynamic>(item.value)
                                    val align = imgData.align as String

                                    if (item.width == null) {
                                        when (align) {
                                            "left" -> {
                                                property("float", "left")
                                                marginRight(10.px)
                                                property("width", "auto")
                                            }

                                            "right" -> {
                                                property("float", "right")
                                                marginLeft(10.px)
                                                property("width", "auto")
                                            }

                                            else -> {
                                                display(DisplayStyle.Block)
                                                property("width", "100%")
                                                textAlign("center")
                                                property("clear", "both")
                                            }
                                        }
                                    } else {
                                        // If width is manually set, respect alignment floats but use manual width
                                        when (align) {
                                            "left" -> {
                                                property("float", "left")
                                                marginRight(10.px)
                                            }

                                            "right" -> {
                                                property("float", "right")
                                                marginLeft(10.px)
                                            }

                                            else -> {
                                                display(DisplayStyle.Block)
                                                textAlign("center")
                                                property("clear", "both")
                                                property("margin", "0 auto")
                                            }
                                        }
                                    }
                                } else {
                                    if (item.width == null) {
                                        property("width", "fit-content")
                                    }
                                    marginRight(10.px) // Add spacing between items
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
                            // Group Drop Zone
                            if (item.type.startsWith("GROUP_")) {
                                onDragOver { event -> event.preventDefault() }
                                onDrop { event ->
                                    event.preventDefault()
                                    event.stopPropagation() // Handle drop within group
                                    val data = event.dataTransfer?.getData("text/plain")
                                    if (data != null) {
                                        try {
                                            val json = JSON.parse<dynamic>(data)
                                            val source = json.source as String
                                            val id = if (source == "child") json.id as Double else Date.now()

                                            if (source == "child") {
                                                val oldChildIndex = item.children.indexOfFirst { it.id == id }
                                                if (oldChildIndex != -1) {
                                                    val childItem = item.children[oldChildIndex]
                                                    val newChildren = item.children.toMutableList()
                                                    newChildren.removeAt(oldChildIndex)
                                                    var insertIdx = index
                                                    if (oldChildIndex < index) insertIdx--
                                                    if (insertIdx < 0) insertIdx = 0
                                                    if (insertIdx > newChildren.size) insertIdx = newChildren.size
                                                    newChildren.add(insertIdx, childItem)
                                                    val newItemWithChildren = item.copy(children = newChildren)
                                                    val groupIndex = postcardItems.indexOfFirst { it.id == item.id }
                                                    if (groupIndex != -1) {
                                                        val newList = postcardItems.toMutableList()
                                                        newList[groupIndex] = newItemWithChildren
                                                        postcardItems = newList
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            console.error(e)
                                        }
                                    }
                                }
                            }
                            onMouseEnter {
                                (it.target as HTMLElement).style.border = "1px dashed #58180d"
                                (it.currentTarget as HTMLElement).querySelector(".delete-btn")
                                    ?.unsafeCast<HTMLElement>()?.style?.opacity = "1"
                                (it.currentTarget as HTMLElement).querySelector(".resize-handle")
                                    ?.unsafeCast<HTMLElement>()?.style?.opacity = "0.5"
                            }
                            onMouseLeave {
                                (it.target as HTMLElement).style.border = "1px dashed transparent"
                                (it.currentTarget as HTMLElement).querySelector(".delete-btn")
                                    ?.unsafeCast<HTMLElement>()?.style?.opacity = "0"
                                (it.currentTarget as HTMLElement).querySelector(".resize-handle")
                                    ?.unsafeCast<HTMLElement>()?.style?.opacity = "0"
                            }
                        }) {
                            // Content Container
                            Div({ style { width(100.percent) } }) {
                                if (item.type.startsWith("GROUP_")) {
                                    // Render Group Header
                                    val headerText = when (item.type) {
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
                                                marginTop((5 * marginScale).px) // Reduced top margin
                                            }
                                        }) { Text(headerText) }
                                    }

                                    // Render Children
                                    item.children.forEachIndexed { index, child ->
                                        Div({
                                            draggable(Draggable.True)
                                            style {
                                                display(DisplayStyle.Flex)
                                                justifyContent(JustifyContent.SpaceBetween)
                                                alignItems(AlignItems.Start)
                                                width(100.percent)
                                                position(Position.Relative)
                                                cursor("move")
                                            }
                                            onDragStart { event ->
                                                event.stopPropagation()
                                                val dragData = js("{}")
                                                dragData.source = "child"
                                                dragData.id = child.id
                                                dragData.type = child.type
                                                dragData.label = child.label
                                                dragData.value = child.value
                                                event.dataTransfer?.setData("text/plain", JSON.stringify(dragData))
                                                event.dataTransfer?.effectAllowed = "move"
                                            }
                                            onDragOver { event -> event.preventDefault() }
                                            onDrop { event ->
                                                event.preventDefault()
                                                event.stopPropagation()
                                                val data = event.dataTransfer?.getData("text/plain")
                                                if (data != null) {
                                                    try {
                                                        val json = JSON.parse<dynamic>(data)
                                                        val source = json.source as String
                                                        val id =
                                                            if (source == "child") json.id as Double else Date.now()

                                                        if (source == "child") {
                                                            val oldChildIndex =
                                                                item.children.indexOfFirst { it.id == id }
                                                            if (oldChildIndex != -1) {
                                                                val childItem = item.children[oldChildIndex]
                                                                val newChildren = item.children.toMutableList()
                                                                newChildren.removeAt(oldChildIndex)
                                                                var insertIdx = index
                                                                if (oldChildIndex < index) insertIdx--
                                                                if (insertIdx < 0) insertIdx = 0
                                                                if (insertIdx > newChildren.size) insertIdx =
                                                                    newChildren.size
                                                                newChildren.add(insertIdx, childItem)
                                                                val newItemWithChildren =
                                                                    item.copy(children = newChildren)
                                                                val groupIndex =
                                                                    postcardItems.indexOfFirst { it.id == item.id }
                                                                if (groupIndex != -1) {
                                                                    val newList = postcardItems.toMutableList()
                                                                    newList[groupIndex] = newItemWithChildren
                                                                    postcardItems = newList
                                                                }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        console.error(e)
                                                    }
                                                }
                                            }
                                            onMouseEnter {
                                                (it.currentTarget as HTMLElement).querySelector(".child-delete-btn")
                                                    ?.unsafeCast<HTMLElement>()?.style?.opacity = "1"
                                            }
                                            onMouseLeave {
                                                (it.currentTarget as HTMLElement).querySelector(".child-delete-btn")
                                                    ?.unsafeCast<HTMLElement>()?.style?.opacity = "0"
                                            }
                                        }) {
                                            Div({ style { flex(1) } }) {
                                                if (child.type == "SEPARATOR") {
                                                    Div({
                                                        style {
                                                            width(100.percent)
                                                            height(2.px)
                                                            backgroundColor(Color("#58180d"))
                                                            marginTop((5 * paddingScale).px)
                                                            marginBottom((5 * paddingScale).px)
                                                        }
                                                    }) {}
                                                } else {
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
                                            }
                                            Span({
                                                classes("child-delete-btn")
                                                attr("data-html2canvas-ignore", "true")
                                                style {
                                                    position(Position.Absolute)
                                                    right((-8).px)
                                                    top((-8).px)
                                                    width(16.px)
                                                    height(16.px)
                                                    backgroundColor(Color("#58180d"))
                                                    color(Color.white)
                                                    borderRadius(50.percent)
                                                    cursor("pointer")
                                                    fontWeight("bold")
                                                    fontSize(12.px)
                                                    display(DisplayStyle.Flex)
                                                    justifyContent(JustifyContent.Center)
                                                    alignItems(AlignItems.Center)
                                                    opacity(0) // Hidden by default
                                                    property("transition", "opacity 0.2s")
                                                    property("z-index", "10")
                                                }
                                                onClick {
                                                    val newChildren = item.children.filter { it.id != child.id }
                                                    val newItem = item.copy(children = newChildren)
                                                    val groupIndex = postcardItems.indexOfFirst { it.id == item.id }
                                                    if (groupIndex != -1) {
                                                        val newList = postcardItems.toMutableList()
                                                        if (newChildren.isEmpty()) {
                                                            newList.removeAt(groupIndex)
                                                        } else {
                                                            newList[groupIndex] = newItem
                                                        }
                                                        postcardItems = newList
                                                    }
                                                }
                                            }) { Text("-") }
                                        }
                                    }
                                } else {
                                    // Render Individual Item (PROPERTY, SEPARATOR, STATS_BLOCK, IMAGE)
                                    when (item.type) {
                                        "SEPARATOR" -> {
                                            Div({
                                                style {
                                                    width(100.percent)
                                                    height(2.px)
                                                    backgroundColor(Color("#58180d"))
                                                    marginTop((5 * paddingScale).px)
                                                    marginBottom((5 * paddingScale).px)
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

                                        "IMAGE" -> {
                                            val imgData = JSON.parse<dynamic>(item.value)
                                            val url = imgData.url as String
                                            val scale = imgData.scale as Double
                                            val align = imgData.align as String

                                            Div({
                                                style {
                                                    display(DisplayStyle.InlineBlock)
                                                    position(Position.Relative)
                                                }
                                                onMouseEnter {
                                                    (it.currentTarget as HTMLElement).querySelector(".image-controls")
                                                        ?.unsafeCast<HTMLElement>()?.style?.opacity = "1"
                                                }
                                                onMouseLeave {
                                                    (it.currentTarget as HTMLElement).querySelector(".image-controls")
                                                        ?.unsafeCast<HTMLElement>()?.style?.opacity = "0"
                                                }
                                            }) {
                                                Img(src = url, alt = "Monster Image") {
                                                    draggable(Draggable.False)
                                                    style {
                                                        if (item.width != null) {
                                                            width(100.percent)
                                                        } else {
                                                            width((200 * scale).px)
                                                        }
                                                        maxWidth(100.percent)
                                                        property("display", "block")
                                                    }
                                                }

                                                // Controls
                                                Div({
                                                    classes("image-controls")
                                                    attr("data-html2canvas-ignore", "true")
                                                    style {
                                                        position(Position.Absolute)
                                                        bottom(5.px)
                                                        left(5.percent)
                                                        backgroundColor(Color("rgba(255, 255, 255, 0.9)"))
                                                        padding(4.px)
                                                        borderRadius(4.px)
                                                        display(DisplayStyle.Flex)
                                                        gap(4.px)
                                                        opacity(0)
                                                        property("transition", "opacity 0.2s")
                                                        border(1.px, LineStyle.Solid, Color("#ccc"))
                                                        property("z-index", "20")
                                                    }
                                                }) {
                                                    fun update(newScale: Double, newAlign: String) {
                                                        val newData = js("{}")
                                                        newData.url = url
                                                        newData.scale = newScale
                                                        newData.align = newAlign
                                                        val newItem = item.copy(value = JSON.stringify(newData))
                                                        val idx = postcardItems.indexOfFirst { it.id == item.id }
                                                        if (idx != -1) {
                                                            val list = postcardItems.toMutableList()
                                                            list[idx] = newItem
                                                            postcardItems = list
                                                        }
                                                    }

                                                    Button({
                                                        style { padding(2.px, 5.px); cursor("pointer") }
                                                        onClick { update((scale - 0.1).coerceAtLeast(0.1), align) }
                                                    }) { Text("-") }

                                                    Button({
                                                        style { padding(2.px, 5.px); cursor("pointer") }
                                                        onClick { update((scale + 0.1).coerceAtMost(3.0), align) }
                                                    }) { Text("+") }

                                                    Span({ style { width(5.px) } }) {}

                                                    Button({
                                                        style {
                                                            padding(
                                                                2.px,
                                                                5.px
                                                            ); cursor("pointer"); fontWeight(if (align == "left") "bold" else "normal")
                                                        }
                                                        onClick { update(scale, "left") }
                                                    }) { Text("L") }

                                                    Button({
                                                        style {
                                                            padding(
                                                                2.px,
                                                                5.px
                                                            ); cursor("pointer"); fontWeight(if (align == "center") "bold" else "normal")
                                                        }
                                                        onClick { update(scale, "center") }
                                                    }) { Text("C") }

                                                    Button({
                                                        style {
                                                            padding(
                                                                2.px,
                                                                5.px
                                                            ); cursor("pointer"); fontWeight(if (align == "right") "bold" else "normal")
                                                        }
                                                        onClick { update(scale, "right") }
                                                    }) { Text("R") }
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
                                                            fontFamily(
                                                                "Book Antiqua",
                                                                "Palatino Linotype",
                                                                "Palatino",
                                                                "serif"
                                                            )
                                                        }
                                                    }) { Text(item.value) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Resize Handle
                            Div({
                                classes("resize-handle")
                                attr("data-html2canvas-ignore", "true")
                                style {
                                    width(10.px)
                                    height(100.percent)
                                    position(Position.Absolute)
                                    right((-5).px)
                                    top(0.px)
                                    cursor("col-resize")
                                    property("z-index", "20")
                                    opacity(0) // Hidden by default
                                }
                                onMouseEnter {
                                    (it.target as HTMLElement).style.opacity = "1"
                                    (it.target as HTMLElement).style.backgroundColor = "#ccc"
                                }
                                onMouseLeave {
                                    (it.target as HTMLElement).style.opacity = "0.5"
                                    (it.target as HTMLElement).style.backgroundColor = "transparent"
                                }
                                onMouseDown { event ->
                                    event.stopPropagation()
                                    event.preventDefault()
                                    val startX = event.clientX
                                    val parent = (event.target as HTMLElement).parentElement as HTMLElement
                                    val startWidth = item.width ?: parent.getBoundingClientRect().width

                                    var onMouseMove: ((org.w3c.dom.events.Event) -> Unit)? = null
                                    var onMouseUp: ((org.w3c.dom.events.Event) -> Unit)? = null

                                    onMouseMove = { e ->
                                        val mouseEvent = e as org.w3c.dom.events.MouseEvent
                                        val newWidth = (startWidth + (mouseEvent.clientX - startX)).coerceAtLeast(20.0)

                                        val idx = postcardItems.indexOfFirst { it.id == item.id }
                                        if (idx != -1) {
                                            val newList = postcardItems.toMutableList()
                                            newList[idx] = newList[idx].copy(width = newWidth)
                                            postcardItems = newList
                                        }
                                    }

                                    onMouseUp = { e ->
                                        window.removeEventListener("mousemove", onMouseMove)
                                        window.removeEventListener("mouseup", onMouseUp)
                                    }

                                    window.addEventListener("mousemove", onMouseMove)
                                    window.addEventListener("mouseup", onMouseUp)
                                }
                            })

                            // Delete button for parent item
                            if (!item.type.startsWith("GROUP_") || item.children.size > 1) {
                                Span({
                                    classes("delete-btn")
                                    attr("data-html2canvas-ignore", "true")
                                    style {
                                        position(Position.Absolute)
                                        if (item.type.startsWith("GROUP_")) {
                                            right(50.percent)
                                            property("transform", "translateX(50%)")
                                            top((-8).px)
                                        } else {
                                            right((-8).px)
                                            top((-8).px)
                                        }
                                        width(16.px)
                                        height(16.px)
                                        backgroundColor(Color("#58180d"))
                                        color(Color.white)
                                        borderRadius(50.percent)
                                        cursor("pointer")
                                        fontWeight("bold")
                                        fontSize(12.px)
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.Center)
                                        alignItems(AlignItems.Center)
                                        opacity(0) // Hidden by default
                                        property("transition", "opacity 0.2s")
                                        property("z-index", "10")
                                    }
                                    onClick {
                                        postcardItems = postcardItems.filter { it.id != item.id }
                                    }
                                }) { Text("-") }
                            }
                        }
                    }
                    if (dropIndicatorIndex == postcardItems.size) {
                        if (dropIndicatorType == "HORIZONTAL") {
                            Div({
                                style {
                                    width(100.percent)
                                    height(0.px)
                                    property("border-top", "2px dotted #58180d")
                                    margin(5.px, 0.px)
                                    property("clear", "both")
                                }
                            })
                        } else {
                            Div({
                                style {
                                    display(DisplayStyle.InlineBlock)
                                    width(0.px)
                                    height(20.px)
                                    property("border-left", "2px dotted #58180d")
                                    margin(0.px, 5.px)
                                    property("vertical-align", "middle")
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlGroup(label: String, value: String, onInc: () -> Unit, onDec: () -> Unit) {
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(5.px)
            alignItems(AlignItems.Center)
            backgroundColor(Color("rgba(255,255,255,0.1)"))
            padding(5.px)
            borderRadius(4.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                minWidth(40.px)
            }
        }) {
            Span({ style { color(Color.white); fontSize(10.px) } }) { Text(label) }
            Span({ style { color(Color.white); fontWeight("bold") } }) { Text(value) }
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(2.px)
            }
        }) {
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    padding(0.px, 4.px)
                    fontSize(10.px)
                    lineHeight(1.2.em)
                    height(16.px)
                }
                onClick { onInc() }
            }) { Text("+") }
            Button(attrs = {
                classes(MonsterSheetStyle.dndButton)
                style {
                    padding(0.px, 4.px)
                    fontSize(10.px)
                    lineHeight(1.2.em)
                    height(16.px)
                }
                onClick { onDec() }
            }) { Text("-") }
        }
    }
}

fun findDropTarget(event: org.w3c.dom.events.MouseEvent, children: org.w3c.dom.HTMLCollection): Pair<Int, String> {
    var targetChild: HTMLElement? = null
    var targetIndex = -1
    var itemIndex = 0

    // Check for direct hover
    for (i in 0 until children.length) {
        val child = children.item(i) as HTMLElement
        if (child.getAttribute("data-is-item") != "true") continue

        val rect = child.getBoundingClientRect()
        if (event.clientX >= rect.left && event.clientX <= rect.right &&
            event.clientY >= rect.top && event.clientY <= rect.bottom
        ) {
            targetChild = child
            targetIndex = itemIndex
            break
        }
        itemIndex++
    }

    if (targetChild != null) {
        val rect = targetChild.getBoundingClientRect()
        val x = event.clientX - rect.left
        val y = event.clientY - rect.top
        val w = rect.width
        val h = rect.height

        val nx = x / w
        val ny = y / h

        val dTop = ny
        val dBottom = 1 - ny
        val dLeft = nx
        val dRight = 1 - nx

        val min = minOf(dTop, dBottom, dLeft, dRight)

        return when (min) {
            dTop -> targetIndex to "HORIZONTAL"
            dBottom -> targetIndex + 1 to "HORIZONTAL"
            dLeft -> targetIndex to "VERTICAL"
            dRight -> targetIndex + 1 to "VERTICAL"
            else -> targetIndex to "HORIZONTAL"
        }
    }

    // Find closest
    var closestDist = Double.MAX_VALUE
    var closestIndex = -1
    var closestSide = "bottom"

    itemIndex = 0
    for (i in 0 until children.length) {
        val child = children.item(i) as HTMLElement
        if (child.getAttribute("data-is-item") != "true") continue
        val rect = child.getBoundingClientRect()

        val cx = rect.left + rect.width / 2
        val cy = rect.top + rect.height / 2
        val dist = kotlin.math.sqrt((event.clientX - cx).let { it * it } + (event.clientY - cy).let { it * it })

        if (dist < closestDist) {
            closestDist = dist
            closestIndex = itemIndex

            val dx = event.clientX - cx
            val dy = event.clientY - cy
            val w = rect.width
            val h = rect.height

            if (kotlin.math.abs(dy) / h > kotlin.math.abs(dx) / w) {
                closestSide = if (dy < 0) "top" else "bottom"
            } else {
                closestSide = if (dx < 0) "left" else "right"
            }
        }
        itemIndex++
    }

    if (closestIndex != -1) {
        return when (closestSide) {
            "top" -> closestIndex to "HORIZONTAL"
            "bottom" -> closestIndex + 1 to "HORIZONTAL"
            "left" -> closestIndex to "VERTICAL"
            "right" -> closestIndex + 1 to "VERTICAL"
            else -> closestIndex to "HORIZONTAL"
        }
    }

    return itemIndex to "HORIZONTAL"
}
