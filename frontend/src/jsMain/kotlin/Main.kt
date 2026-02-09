import androidx.compose.runtime.*
import com.example.shared.MonsterDto
import com.example.shared.TraitDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.accept
import org.jetbrains.compose.web.attributes.readOnly
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.DataTransferItem
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

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

@Composable
fun MonsterSheet(initialMonster: MonsterDto, onSave: (MonsterDto) -> Unit) {
    var monster by remember { mutableStateOf(initialMonster) }
    var isEditingEnabled by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    var pastedImageFile by remember { mutableStateOf<org.w3c.files.File?>(null) }
    var pastedImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Update local state when initialMonster changes (e.g. after save)
    LaunchedEffect(initialMonster) {
        monster = initialMonster
    }

    Div({ classes(MonsterSheetStyle.mainContainer) }) {
        // Controls (Back, ID & Edit Button) outside the sheet
        Div({ classes(MonsterSheetStyle.controlsContainer) }) {
            Div({ 
                style { cursor("pointer"); textDecoration("underline") }
                onClick { window.location.href = "/" }
            }) {
                Text("← Back to List")
            }
            
            Div({ style { display(DisplayStyle.Flex); gap(5.px) } }) {
                Button(attrs = {
                    style {
                        fontSize(12.px)
                        padding(5.px, 10.px)
                        cursor("pointer")
                    }
                    onClick {
                        showPrompt = !showPrompt
                        pastedImageFile = null
                        pastedImageUrl = null
                    }
                }) {
                    Text("Generate Portrait Prompt")
                }

                Button(attrs = {
                    style {
                        fontSize(12.px)
                        padding(5.px, 10.px)
                        cursor("pointer")
                    }
                    onClick {
                        val element = document.getElementById("monster-sheet-content") as? HTMLElement
                        if (element != null) {
                            // Use html2canvas with useCORS: true to allow capturing cross-origin images
                            val options = js("{}")
                            options.useCORS = true
                            options.allowTaint = true
                            
                            html2canvas(element, options).then { canvas ->
                                val link = document.createElement("a") as org.w3c.dom.HTMLAnchorElement
                                link.download = "${monster.name.replace(" ", "_")}.png"
                                link.href = (canvas as org.w3c.dom.HTMLCanvasElement).toDataURL()
                                link.click()
                                null
                            }
                        }
                    }
                }) {
                    Text("Print PNG")
                }

                Button(attrs = {
                    style {
                        fontSize(12.px)
                        padding(5.px, 10.px)
                        cursor("pointer")
                    }
                    onClick {
                        if (isEditingEnabled) {
                            // Save when clicking "Done"
                            onSave(monster)
                        }
                        isEditingEnabled = !isEditingEnabled
                    }
                }) {
                    Text(if (isEditingEnabled) "Done" else "Edit")
                }
            }
        }

        if (showPrompt) {
            Div({
                style {
                    backgroundColor(Color("#f0f0f0"))
                    padding(10.px)
                    marginBottom(10.px)
                    border(1.px, LineStyle.Solid, Color("#ccc"))
                    borderRadius(5.px)
                    fontSize(12.px)
                }
            }) {
                P { Text("Copy this prompt and paste it into Gemini or an image generator:") }
                TextArea(
                    attrs = {
                        style {
                            width(100.percent)
                            height(80.px)
                            marginBottom(5.px)
                        }
                        readOnly()
                        onPaste { event ->
                            console.log("Paste event triggered")
                            val items = event.clipboardData?.items
                            if (items != null) {
                                console.log("Clipboard items found: ${items.length}")
                                for (i in 0 until items.length) {
                                    val item = items.asDynamic()[i] as? DataTransferItem
                                    console.log("Item $i type: ${item?.type}, kind: ${item?.kind}")
                                    if (item?.type?.startsWith("image") == true) {
                                        console.log("Image item found")
                                        val file = item.getAsFile()
                                        if (file != null) {
                                            pastedImageFile = file
                                            // Create a local URL to preview the image
                                            pastedImageUrl = org.w3c.dom.url.URL.createObjectURL(file)
                                        } else {
                                            console.error("Failed to get file from item")
                                        }
                                    }
                                }
                            } else {
                                console.log("No clipboard items found")
                            }
                        }
                    },
                    value = """
                        Generate a fantasy portrait of a D&D monster: ${monster.name} (${monster.meta}).
                        Do not generate text based on this prompt!
                        Armor Class: ${monster.armorClass}. Hit Points: ${monster.hitPoints}. Speed: ${monster.speed}. 
                        Stats: STR ${monster.str}, DEX ${monster.dex}, CON ${monster.con}, INT ${monster.int}, WIS ${monster.wis}, CHA ${monster.cha}. 
                        Traits: ${monster.traits.joinToString(", ") { "${it.name}: ${it.description}" }}. 
                        Actions: ${monster.actions.joinToString(", ") { "${it.name}: ${it.description}" }}. 
                        Style: Detailed, oil painting, dark fantasy.
                    """.trimIndent().replace("\n", " ")
                )
                A(
                    href = "https://gemini.google.com/app",
                    attrs = { target(org.jetbrains.compose.web.attributes.ATarget.Blank) }) {
                    Text("Open Gemini")
                }
                
                if (pastedImageUrl != null) {
                    Div({ style { marginTop(10.px); border(1.px, LineStyle.Dashed, Color("#999")); padding(10.px) } }) {
                        Text("Pasted Image Preview:")
                        Br()
                        Img(src = pastedImageUrl!!, alt = "Pasted Image") {
                            style {
                                maxWidth(200.px)
                                maxHeight(200.px)
                                marginTop(5.px)
                                marginBottom(5.px)
                            }
                        }
                        Div({ style { display(DisplayStyle.Flex); gap(10.px); marginTop(5.px) } }) {
                            Button(attrs = {
                                onClick {
                                    if (pastedImageFile != null && monster.id != null) {
                                        scope.launch {
                                            try {
                                                val updatedMonster = uploadMonsterImage(monster.id!!, pastedImageFile!!)
                                                console.log("Image uploaded successfully", updatedMonster)
                                                monster = updatedMonster // Update local state to show new image
                                                showPrompt = false // Close prompt area
                                                pastedImageFile = null
                                                pastedImageUrl = null
                                                window.alert("Image pasted and uploaded successfully!")
                                            } catch (e: Exception) {
                                                console.error("Failed to upload pasted image", e)
                                                window.alert("Failed to upload image: ${e.message}")
                                            }
                                        }
                                    } else if (monster.id == null) {
                                        window.alert("Please save the monster first before uploading.")
                                    }
                                }
                            }) { Text("Save Image") }
                            
                            Button(attrs = {
                                onClick {
                                    pastedImageFile = null
                                    pastedImageUrl = null
                                }
                            }) { Text("Reject") }
                        }
                    }
                }
            }
        }

        // The Monster Sheet itself
        Div({ 
            id("monster-sheet-content")
            classes(MonsterSheetStyle.sheetContainer) 
        }) {
            // Image Section
            if (monster.imageUrl != null || isEditingEnabled) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        marginBottom(20.px)
                        position(Position.Relative) // For delete button positioning
                        
                        // Dragging logic for image position
                        if (isEditingEnabled) {
                            cursor("move")
                        }
                        
                        // Apply position based on monster.imagePosition
                        when (monster.imagePosition) {
                            "right" -> {
                                property("float", "right")
                                marginLeft(20.px)
                            }
                            "left" -> {
                                property("float", "left")
                                marginRight(20.px)
                            }
                            "bottom" -> {
                                // Bottom positioning usually requires flex column order change or absolute positioning
                                // For simplicity in this layout, we might just move the DOM element or use order if flex
                            }
                            // "top" is default
                        }
                    }
                }) {
                    if (monster.imageUrl != null) {
                        Img(src = "http://localhost:8090${monster.imageUrl}", alt = monster.name) {
                            style {
                                maxWidth(100.percent)
                                maxHeight(300.px)
                                border(2.px, LineStyle.Solid, Color("#58180d"))
                                property("box-shadow", "0px 0px 10px rgba(0,0,0,0.5)")
                            }
                        }
                        
                        if (isEditingEnabled) {
                            // Position controls
                            Div({
                                style {
                                    position(Position.Absolute)
                                    top((-25).px)
                                    left(0.px)
                                    display(DisplayStyle.Flex)
                                    gap(5.px)
                                    backgroundColor(Color.white)
                                    padding(2.px)
                                    border(1.px, LineStyle.Solid, Color("#ccc"))
                                    borderRadius(4.px)
                                    property("z-index", "10")
                                }
                            }) {
                                val positions = listOf("top", "left", "right") // Bottom is tricky in this flow
                                positions.forEach { pos ->
                                    Button(attrs = {
                                        style {
                                            fontSize(10.px)
                                            padding(2.px, 5.px)
                                            cursor("pointer")
                                            if (monster.imagePosition == pos) {
                                                fontWeight("bold")
                                                backgroundColor(Color("#ddd"))
                                            }
                                        }
                                        onClick {
                                            monster = monster.copy(imagePosition = pos)
                                        }
                                    }) { Text(pos.replaceFirstChar { it.uppercase() }) }
                                }
                            }

                            Button(attrs = {
                                style {
                                    position(Position.Absolute)
                                    top(5.px)
                                    right(5.px)
                                    backgroundColor(Color.red)
                                    color(Color.white)
                                    border(0.px)
                                    borderRadius(50.percent)
                                    width(20.px)
                                    height(20.px)
                                    cursor("pointer")
                                    display(DisplayStyle.Flex)
                                    justifyContent(JustifyContent.Center)
                                    alignItems(AlignItems.Center)
                                    fontSize(12.px)
                                }
                                onClick {
                                    if (window.confirm("Are you sure you want to delete this image?")) {
                                        val updatedMonster = monster.copy(imageUrl = null)
                                        onSave(updatedMonster)
                                    }
                                }
                                title("Delete Image")
                            }) { Text("✕") }
                        }
                    }
                    
                    if (isEditingEnabled && monster.imageUrl == null) {
                        Label(attrs = {
                            style {
                                display(DisplayStyle.Block)
                                marginTop(10.px)
                                cursor("pointer")
                                color(Color.blue)
                                textDecoration("underline")
                            }
                        }) {
                            Text("Upload Image")
                            Input(InputType.File) {
                                style { display(DisplayStyle.None) }
                                accept("image/*")
                                onChange { event ->
                                    val file = (event.target as HTMLInputElement).files?.item(0)
                                    if (file != null && monster.id != null) {
                                        scope.launch {
                                            try {
                                                val updatedMonster = uploadMonsterImage(monster.id!!, file)
                                                monster = updatedMonster
                                            } catch (e: Exception) {
                                                console.error("Failed to upload image", e)
                                            }
                                        }
                                    } else if (monster.id == null) {
                                        window.alert("Please save the monster first before uploading an image.")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Header Section
            Div({ classes(MonsterSheetStyle.header) }) {
                EditableText(
                    monster.name,
                    isEditingEnabled = isEditingEnabled,
                    onValueChange = { monster = monster.copy(name = it) }) {
                    H1 { Text(monster.name) }
                }
                
                Div({ classes(MonsterSheetStyle.subHeader) }) {
                    EditableText(
                        monster.meta,
                        isEditingEnabled = isEditingEnabled,
                        onValueChange = { monster = monster.copy(meta = it) }) {
                        Text(monster.meta)
                    }
                }
            }

            Hr { }

            // Stats Block
            Div({ classes(MonsterSheetStyle.statsGrid) }) {
                StatBox("Armor Class", monster.armorClass, isEditingEnabled) { monster = monster.copy(armorClass = it) }
                StatBox("Hit Points", monster.hitPoints, isEditingEnabled) { monster = monster.copy(hitPoints = it) }
                StatBox("Speed", monster.speed, isEditingEnabled) { monster = monster.copy(speed = it) }
            }

            Hr { }

            // Ability Scores
            Div({ classes(MonsterSheetStyle.abilityScoreContainer) }) {
                AbilityScore("STR", monster.str, isEditingEnabled) { monster = monster.copy(str = it) }
                AbilityScore("DEX", monster.dex, isEditingEnabled) { monster = monster.copy(dex = it) }
                AbilityScore("CON", monster.con, isEditingEnabled) { monster = monster.copy(con = it) }
                AbilityScore("INT", monster.int, isEditingEnabled) { monster = monster.copy(int = it) }
                AbilityScore("WIS", monster.wis, isEditingEnabled) { monster = monster.copy(wis = it) }
                AbilityScore("CHA", monster.cha, isEditingEnabled) { monster = monster.copy(cha = it) }
            }

            Hr { }

            // Skills & Senses
            Div({ classes(MonsterSheetStyle.section) }) {
                PropertyLine("Saving Throws", monster.savingThrows ?: "", isEditingEnabled) {
                    monster = monster.copy(savingThrows = it)
                }
                PropertyLine("Skills", monster.skills ?: "", isEditingEnabled) { monster = monster.copy(skills = it) }
                PropertyLine("Senses", monster.senses, isEditingEnabled) { monster = monster.copy(senses = it) }
                PropertyLine("Languages", monster.languages, isEditingEnabled) {
                    monster = monster.copy(languages = it)
                }
                PropertyLine("Challenge", monster.challenge, isEditingEnabled) {
                    monster = monster.copy(challenge = it)
                }
            }

            Hr { }

            // Traits
            Div({ classes(MonsterSheetStyle.section) }) {
                monster.traits.forEachIndexed { index, trait ->
                    TraitBlock(
                        trait.name,
                        trait.description,
                        isEditingEnabled,
                        onNameChange = { newName ->
                            val newTraits = monster.traits.toMutableList()
                            newTraits[index] = trait.copy(name = newName)
                            monster = monster.copy(traits = newTraits)
                        },
                        onDescChange = { newDesc ->
                            val newTraits = monster.traits.toMutableList()
                            newTraits[index] = trait.copy(description = newDesc)
                            monster = monster.copy(traits = newTraits)
                        },
                        onDelete = {
                            val newTraits = monster.traits.toMutableList()
                            newTraits.removeAt(index)
                            monster = monster.copy(traits = newTraits)
                        }
                    )
                }
                if (isEditingEnabled) {
                    AddButton("Add Trait") {
                        val newTraits = monster.traits.toMutableList()
                        newTraits.add(TraitDto("New Trait", "Description"))
                        monster = monster.copy(traits = newTraits)
                    }
                }
            }

            // Actions
            H3({ classes(MonsterSheetStyle.sectionHeader) }) { Text("Actions") }
            Div({ classes(MonsterSheetStyle.section) }) {
                monster.actions.forEachIndexed { index, action ->
                    TraitBlock(
                        action.name,
                        action.description,
                        isEditingEnabled,
                        onNameChange = { newName ->
                            val newActions = monster.actions.toMutableList()
                            newActions[index] = action.copy(name = newName)
                            monster = monster.copy(actions = newActions)
                        },
                        onDescChange = { newDesc ->
                            val newActions = monster.actions.toMutableList()
                            newActions[index] = action.copy(description = newDesc)
                            monster = monster.copy(actions = newActions)
                        },
                        onDelete = {
                            val newActions = monster.actions.toMutableList()
                            newActions.removeAt(index)
                            monster = monster.copy(actions = newActions)
                        }
                    )
                }
                if (isEditingEnabled) {
                    AddButton("Add Action") {
                        val newActions = monster.actions.toMutableList()
                        newActions.add(TraitDto("New Action", "Description"))
                        monster = monster.copy(actions = newActions)
                    }
                }
            }
        }
    }
}