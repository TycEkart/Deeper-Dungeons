package com.deeperdungeons.frontend.screens.details

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.accept
import org.jetbrains.compose.web.attributes.readOnly
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.selected
import org.w3c.dom.DataTransferItem
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import com.deeperdungeons.frontend.api.uploadMonsterImage
import com.deeperdungeons.frontend.api.getBaseUrl
import com.deeperdungeons.frontend.components.AbilityScore
import com.deeperdungeons.frontend.components.AddButton
import com.deeperdungeons.frontend.components.EditableText
import com.deeperdungeons.frontend.components.PropertyLine
import com.deeperdungeons.frontend.components.StatBox
import com.deeperdungeons.frontend.components.TaperedRule
import com.deeperdungeons.frontend.components.TraitBlock
import com.deeperdungeons.frontend.components.html2canvas
import com.deeperdungeons.shared.Alignment
import com.deeperdungeons.shared.MonsterDto
import com.deeperdungeons.shared.MonsterSize
import com.deeperdungeons.shared.MonsterType
import com.deeperdungeons.shared.Skill
import com.deeperdungeons.shared.Stat
import com.deeperdungeons.shared.TraitDto
import org.jetbrains.compose.web.attributes.placeholder

@Composable
fun MonsterSheet(initialMonster: MonsterDto, onBack: () -> Unit, onExport: () -> Unit, onSave: (MonsterDto) -> Unit) {
    var monster by remember { mutableStateOf(initialMonster) }
    var isEditingEnabled by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    var pastedImageFile by remember { mutableStateOf<File?>(null) }
    var pastedImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Update local state when initialMonster changes (e.g. after save)
    LaunchedEffect(initialMonster) {
        monster = initialMonster
    }

    val classicPrompt = """
        Generate a fantasy portrait of a D&D monster: ${monster.name} (${monster.size.label} ${monster.type.label}, ${monster.alignment.label}).
        Do not generate text based on this prompt!
        Armor Class: ${monster.armorClass}. Hit Points: ${monster.hitPoints}. Speed: ${monster.speed}. 
        Stats: STR ${monster.str.value}, DEX ${monster.dex.value}, CON ${monster.con.value}, INT ${monster.int.value}, WIS ${monster.wis.value}, CHA ${monster.cha.value}. 
        Traits: ${monster.traits.joinToString(", ") { "${it.name}: ${it.description}" }}. 
        Actions: ${monster.actions.joinToString(", ") { "${it.name}: ${it.description}" }}. 
        Style: Detailed, oil painting, dark fantasy.
    """.trimIndent().replace("\n", " ")

    val customPrompt = monster.imagePrompt

    Div({ classes(MonsterSheetStyle.mainContainer) }) {
        // Controls (Back, ID & Edit Button) outside the sheet
        Div({ classes(MonsterSheetStyle.controlsContainer) }) {
            Div({ 
                style { cursor("pointer"); textDecoration("underline") }
                onClick { onBack() }
            }) {
                Text("← Back to List")
            }
            
            Div({ style { display(DisplayStyle.Flex); gap(5.px) } }) {
                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick {
                        showPrompt = !showPrompt
                        pastedImageFile = null
                        pastedImageUrl = null
                    }
                }) {
                    Text("Generate Portrait Prompt")
                }

                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
                    onClick {
                        val element = document.getElementById("monster-sheet-content") as? HTMLElement
                        if (element != null) {
                            // Use components.html2canvas with useCORS: true to allow capturing cross-origin images
                            val options = js("{}")
                            options.useCORS = true
                            options.allowTaint = true
                            html2canvas(element, options).then { canvas ->
                                val link = document.createElement("a") as HTMLAnchorElement
                                link.download = "${monster.name.replace(" ", "_")}.png"
                                link.href = (canvas as HTMLCanvasElement).toDataURL()
                                link.click()
                                null
                            }
                        }
                    }
                }) {
                    Text("Print PNG")
                }

                // Export Button
                if (monster.id != null) {
                    Button(attrs = {
                        classes(MonsterSheetStyle.dndButton)
                        onClick { onExport() }
                    }) {
                        Text("Export Postcard")
                    }

                    A(href = "${getBaseUrl()}/monsters/${monster.id}/export", attrs = {
                        target(ATarget.Blank)
                        classes(MonsterSheetStyle.dndButton)
                        style { textDecoration("none") }
                    }) {
                        Text("Export JSON")
                    }
                }

                Button(attrs = {
                    classes(MonsterSheetStyle.dndButton)
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
                P { Text("Copy one of these prompts and paste it into Gemini or an image generator:") }
                
                // Classic Prompt
                Div({ style { marginBottom(10.px) } }) {
                    Label { Text("Classic Prompt (Auto-generated):") }
                    TextArea(
                        attrs = {
                            style {
                                width(100.percent)
                                height(60.px)
                                marginBottom(5.px)
                            }
                            readOnly()
                        },
                        value = classicPrompt
                    )
                    Button(attrs = {
                        classes(MonsterSheetStyle.dndButton)
                        onClick {
                            window.navigator.clipboard.writeText(classicPrompt)
                            window.alert("Classic prompt copied to clipboard!")
                        }
                    }) { Text("Copy Classic Prompt") }
                }

                // Custom Prompt (if available)
                if (!customPrompt.isNullOrBlank()) {
                    Div({ style { marginBottom(10.px) } }) {
                        Label { Text("Custom Prompt (From AI/Database):") }
                        TextArea(
                            attrs = {
                                style {
                                    width(100.percent)
                                    height(60.px)
                                    marginBottom(5.px)
                                }
                                if (!isEditingEnabled) {
                                    readOnly()
                                }
                                onInput { event ->
                                    if (isEditingEnabled) {
                                        monster = monster.copy(imagePrompt = event.value)
                                    }
                                }
                            },
                            value = customPrompt
                        )
                        Button(attrs = {
                            classes(MonsterSheetStyle.dndButton)
                            onClick {
                                window.navigator.clipboard.writeText(customPrompt)
                                window.alert("Custom prompt copied to clipboard!")
                            }
                        }) { Text("Copy Custom Prompt") }
                    }
                } else if (isEditingEnabled) {
                     Div({ style { marginBottom(10.px) } }) {
                        Label { Text("Custom Prompt (Editable):") }
                        TextArea(
                            attrs = {
                                style {
                                    width(100.percent)
                                    height(60.px)
                                    marginBottom(5.px)
                                }
                                placeholder("Enter a custom image prompt here...")
                                onInput { event ->
                                    monster = monster.copy(imagePrompt = event.value)
                                }
                            },
                            value = ""
                        )
                    }
                }

                Hr()
                
                P { Text("Paste generated image here:") }
                TextArea(
                    attrs = {
                        style {
                            width(100.percent)
                            height(40.px)
                            marginBottom(5.px)
                        }
                        placeholder("Paste image here (Ctrl+V)")
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
                                            pastedImageUrl = URL.createObjectURL(file)
                                        } else {
                                            console.error("Failed to get file from item")
                                        }
                                    }
                                }
                            } else {
                                console.log("No clipboard items found")
                            }
                        }
                    }
                )
                
                Div({ style { display(DisplayStyle.Flex); gap(10.px); marginTop(5.px) } }) {
                    A(
                        href = "https://gemini.google.com/app",
                        attrs = { 
                            target(ATarget.Blank)
                            classes(MonsterSheetStyle.dndButton)
                            style { textDecoration("none") }
                        }) {
                        Text("Open Gemini")
                    }
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
                                classes(MonsterSheetStyle.dndButton)
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
                                classes(MonsterSheetStyle.dndButton)
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
                        Img(src = "${getBaseUrl()}${monster.imageUrl}", alt = monster.name) {
                            style {
                                // Scale the image based on monster.imageScale
                                width((300 * monster.imageScale).px)
                                // Allow height to adjust automatically to maintain aspect ratio, or scale max-height
                                property("max-width", "100%")
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
                            
                            // Scale Controls
                            Div({
                                style {
                                    position(Position.Absolute)
                                    bottom((-25).px)
                                    left(0.px)
                                    display(DisplayStyle.Flex)
                                    gap(5.px)
                                    backgroundColor(Color.white)
                                    padding(2.px)
                                    border(1.px, LineStyle.Solid, Color("#ccc"))
                                    borderRadius(4.px)
                                    property("z-index", "10")
                                    alignItems(AlignItems.Center)
                                }
                            }) {
                                Button(attrs = {
                                    style { fontSize(10.px); padding(2.px, 5.px); cursor("pointer") }
                                    onClick {
                                        val newScale = (monster.imageScale - 0.1f).coerceAtLeast(0.1f)
                                        monster = monster.copy(imageScale = newScale)
                                    }
                                }) { Text("-") }
                                
                                Span({ style { fontSize(10.px) } }) { 
                                    Text("${(monster.imageScale * 100).toInt()}%") 
                                }
                                
                                Button(attrs = {
                                    style { fontSize(10.px); padding(2.px, 5.px); cursor("pointer") }
                                    onClick {
                                        val newScale = (monster.imageScale + 0.1f).coerceAtMost(3.0f)
                                        monster = monster.copy(imageScale = newScale)
                                    }
                                }) { Text("+") }
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
                    H1({ classes(MonsterSheetStyle.monsterName) }) { Text(monster.name) }
                }
                
                Div({ classes(MonsterSheetStyle.subHeader) }) {
                    if (isEditingEnabled) {
                        Div({ style { display(DisplayStyle.Flex); gap(5.px); alignItems(AlignItems.Center); flexWrap(FlexWrap.Wrap) } }) {
                            // Size Dropdown
                            Select({
                                classes(MonsterSheetStyle.inputField)
                                style { width(100.px); margin(0.px) }
                                onInput { event ->
                                    val value = event.value
                                    val newSize = MonsterSize.values().find { it.name == value } ?: MonsterSize.Medium
                                    monster = monster.copy(size = newSize)
                                }
                            }) {
                                MonsterSize.values().forEach { size ->
                                    Option(size.name, attrs = {
                                        if (size == monster.size) selected()
                                    }) {
                                        Text(size.label)
                                    }
                                }
                            }
                            
                            // Type Dropdown
                            Select({
                                classes(MonsterSheetStyle.inputField)
                                style { width(120.px); margin(0.px) }
                                onInput { event ->
                                    val value = event.value
                                    val newType = MonsterType.values().find { it.name == value } ?: MonsterType.Humanoid
                                    monster = monster.copy(type = newType)
                                }
                            }) {
                                MonsterType.values().forEach { type ->
                                    Option(type.name, attrs = {
                                        if (type == monster.type) selected()
                                    }) {
                                        Text(type.label)
                                    }
                                }
                            }
                            
                            Text(", ")
                            
                            // Alignment Dropdown
                            Select({
                                classes(MonsterSheetStyle.inputField)
                                style { width(150.px); margin(0.px) }
                                onInput { event ->
                                    val value = event.value
                                    val newAlignment = Alignment.values().find { it.name == value } ?: Alignment.Unaligned
                                    monster = monster.copy(alignment = newAlignment)
                                }
                            }) {
                                Alignment.values().forEach { alignment ->
                                    Option(alignment.name, attrs = {
                                        if (alignment == monster.alignment) selected()
                                    }) {
                                        Text(alignment.label)
                                    }
                                }
                            }
                        }
                    } else {
                        Text("${monster.size.label} ${monster.type.label}, ${monster.alignment.label}")
                    }
                }
            }

            TaperedRule()

            // Stats Block
            Div({ classes(MonsterSheetStyle.statsGrid) }) {
                // Armor Class Editor
                Div({ classes(MonsterSheetStyle.propertyLine) }) {
                    Span({ classes(MonsterSheetStyle.propertyLabel) }) { Text("Armor Class ") }
                    if (isEditingEnabled) {
                        Div({ style { display(DisplayStyle.Flex); gap(5.px); alignItems(AlignItems.Center) } }) {
                            Input(InputType.Number) {
                                classes(MonsterSheetStyle.inputField)
                                style { width(60.px); margin(0.px) }
                                value(monster.armorClass.value)
                                onInput { event ->
                                    val newVal = event.value?.toString()?.toIntOrNull() ?: 10
                                    monster = monster.copy(armorClass = monster.armorClass.copy(value = newVal))
                                }
                            }
                            Input(InputType.Text) {
                                classes(MonsterSheetStyle.inputField)
                                style { width(150.px); margin(0.px) }
                                placeholder("(description)")
                                value(monster.armorClass.description ?: "")
                                onInput { event ->
                                    val newDesc = event.value
                                    monster = monster.copy(armorClass = monster.armorClass.copy(description = if (newDesc.isBlank()) null else newDesc))
                                }
                            }
                        }
                    } else {
                        Text(monster.armorClass.toString())
                    }
                }
                
                StatBox("Hit Points", monster.hitPoints, isEditingEnabled) { monster = monster.copy(hitPoints = it) }
                StatBox("Speed", monster.speed, isEditingEnabled) { monster = monster.copy(speed = it) }
            }

            TaperedRule()

            // Ability Scores
            Div({ classes(MonsterSheetStyle.abilityScoreContainer) }) {
                AbilityScore("STR", monster.str, isEditingEnabled) { monster = monster.copy(str = it) }
                AbilityScore("DEX", monster.dex, isEditingEnabled) { monster = monster.copy(dex = it) }
                AbilityScore("CON", monster.con, isEditingEnabled) { monster = monster.copy(con = it) }
                AbilityScore("INT", monster.int, isEditingEnabled) { monster = monster.copy(int = it) }
                AbilityScore("WIS", monster.wis, isEditingEnabled) { monster = monster.copy(wis = it) }
                AbilityScore("CHA", monster.cha, isEditingEnabled) { monster = monster.copy(cha = it) }
            }

            TaperedRule()

            // Skills & Senses
            Div({ classes(MonsterSheetStyle.section) }) {
                // Saving Throws Section with Custom Editor
                SavingThrowEditor(
                    savingThrows = monster.savingThrows ?: "",
                    isEditingEnabled = isEditingEnabled,
                    onValueChange = { monster = monster.copy(savingThrows = it) }
                )
                
                // Skills Section with Custom Editor
                SkillEditor(
                    skills = monster.skills ?: "",
                    isEditingEnabled = isEditingEnabled,
                    onValueChange = { monster = monster.copy(skills = it) }
                )

                if (monster.senses.isNotBlank() || isEditingEnabled) {
                    PropertyLine("Senses", monster.senses, isEditingEnabled) { monster = monster.copy(senses = it) }
                }
                if (monster.languages.isNotBlank() || isEditingEnabled) {
                    PropertyLine("Languages", monster.languages, isEditingEnabled) {
                        monster = monster.copy(languages = it)
                    }
                }
                if (monster.challenge.isNotBlank() || isEditingEnabled) {
                    PropertyLine("Challenge", monster.challenge, isEditingEnabled) {
                        monster = monster.copy(challenge = it)
                    }
                }
            }

            TaperedRule()

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
            if (monster.actions.isNotEmpty() || isEditingEnabled) {
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

            // Reactions
            if (monster.reactions.isNotEmpty() || isEditingEnabled) {
                H3({ classes(MonsterSheetStyle.sectionHeader) }) { Text("Reactions") }
                Div({ classes(MonsterSheetStyle.section) }) {
                    monster.reactions.forEachIndexed { index, reaction ->
                        TraitBlock(
                            reaction.name,
                            reaction.description,
                            isEditingEnabled,
                            onNameChange = { newName ->
                                val newReactions = monster.reactions.toMutableList()
                                newReactions[index] = reaction.copy(name = newName)
                                monster = monster.copy(reactions = newReactions)
                            },
                            onDescChange = { newDesc ->
                                val newReactions = monster.reactions.toMutableList()
                                newReactions[index] = reaction.copy(description = newDesc)
                                monster = monster.copy(reactions = newReactions)
                            },
                            onDelete = {
                                val newReactions = monster.reactions.toMutableList()
                                newReactions.removeAt(index)
                                monster = monster.copy(reactions = newReactions)
                            }
                        )
                    }
                    if (isEditingEnabled) {
                        AddButton("Add Reaction") {
                            val newReactions = monster.reactions.toMutableList()
                            newReactions.add(TraitDto("New Reaction", "Description"))
                            monster = monster.copy(reactions = newReactions)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingThrowEditor(
    savingThrows: String,
    isEditingEnabled: Boolean,
    onValueChange: (String) -> Unit
) {
    if (isEditingEnabled) {
        Div({ classes(MonsterSheetStyle.propertyLine) }) {
            Span({ classes(MonsterSheetStyle.propertyLabel) }) { Text("Saving Throws ") }
            
            Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(5.px) } }) {
                // List of current saving throws
                if (savingThrows.isNotBlank()) {
                    Div({ style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(5.px) } }) {
                        savingThrows.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { saveStr ->
                            Div({
                                style {
                                    backgroundColor(Color("#eee"))
                                    padding(2.px, 6.px)
                                    borderRadius(4.px)
                                    border(1.px, LineStyle.Solid, Color("#ccc"))
                                    display(DisplayStyle.Flex)
                                    alignItems(AlignItems.Center)
                                    gap(5.px)
                                    fontSize(12.px)
                                }
                            }) {
                                Text(saveStr)
                                Span({
                                    style { 
                                        cursor("pointer")
                                        color(Color("#58180d"))
                                        fontWeight("bold")
                                        marginLeft(2.px)
                                    }
                                    onClick {
                                        val currentList = savingThrows.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        val newList = currentList.filter { it != saveStr }
                                        onValueChange(newList.joinToString(", "))
                                    }
                                    title("Remove saving throw")
                                }) { Text("×") }
                            }
                        }
                    }
                }

                // Add new saving throw controls
                var selectedStat by remember { mutableStateOf(Stat.STR) }
                var bonus by remember { mutableStateOf(0) }

                Div({ style { display(DisplayStyle.Flex); gap(5.px); alignItems(AlignItems.Center) } }) {
                    Select({
                        classes(MonsterSheetStyle.inputField)
                        style { width(80.px); margin(0.px) }
                        onInput { event ->
                            val value = event.value
                            selectedStat = Stat.values().find { it.name == value } ?: Stat.STR
                        }
                    }) {
                        Stat.values().forEach { stat ->
                            Option(stat.name, attrs = {
                                if (stat == selectedStat) selected()
                            }) {
                                Text(stat.label)
                            }
                        }
                    }

                    Input(InputType.Number) {
                        classes(MonsterSheetStyle.inputField)
                        style { width(60.px); margin(0.px) }
                        value(bonus)
                        onInput { event -> bonus = event.value?.toString()?.toIntOrNull() ?: 0 }
                    }

                    Button(attrs = {
                        classes(MonsterSheetStyle.dndButton)
                        onClick {
                            val sign = if (bonus >= 0) "+" else ""
                            val newEntry = "${selectedStat.label} $sign$bonus"
                            val currentList = if (savingThrows.isBlank()) emptyList() else savingThrows.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            
                            // Remove existing entry for this stat if exists
                            val statName = selectedStat.label
                            val filteredList = currentList.filter { !it.startsWith(statName) }
                            
                            val newList = filteredList + newEntry
                            // Sort by Stat enum order
                            val sortedList = newList.sortedBy { entry ->
                                val entryStatName = entry.split(" ").firstOrNull() ?: ""
                                Stat.values().find { it.label == entryStatName }?.ordinal ?: 999
                            }
                            
                            onValueChange(sortedList.joinToString(", "))
                        }
                    }) { Text("Add") }
                }
            }
        }
    } else {
        if (savingThrows.isNotBlank()) {
            PropertyLine("Saving Throws", savingThrows, isEditingEnabled = false) {}
        }
    }
}

@Composable
fun SkillEditor(
    skills: String,
    isEditingEnabled: Boolean,
    onValueChange: (String) -> Unit
) {
    if (isEditingEnabled) {
        Div({ classes(MonsterSheetStyle.propertyLine) }) {
            Span({ classes(MonsterSheetStyle.propertyLabel) }) { Text("Skills ") }
            
            Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(5.px) } }) {
                // List of current skills
                if (skills.isNotBlank()) {
                    Div({ style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(5.px) } }) {
                        skills.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { skillStr ->
                            Div({
                                style {
                                    backgroundColor(Color("#eee"))
                                    padding(2.px, 6.px)
                                    borderRadius(4.px)
                                    border(1.px, LineStyle.Solid, Color("#ccc"))
                                    display(DisplayStyle.Flex)
                                    alignItems(AlignItems.Center)
                                    gap(5.px)
                                    fontSize(12.px)
                                }
                            }) {
                                Text(skillStr)
                                Span({
                                    style { 
                                        cursor("pointer")
                                        color(Color("#58180d"))
                                        fontWeight("bold")
                                        marginLeft(2.px)
                                    }
                                    onClick {
                                        val currentList = skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        val newList = currentList.filter { it != skillStr }
                                        onValueChange(newList.joinToString(", "))
                                    }
                                    title("Remove skill")
                                }) { Text("×") }
                            }
                        }
                    }
                }

                // Add new skill controls
                var selectedSkill by remember { mutableStateOf(Skill.Acrobatics) }
                var bonus by remember { mutableStateOf(0) }

                Div({ style { display(DisplayStyle.Flex); gap(5.px); alignItems(AlignItems.Center) } }) {
                    Select({
                        classes(MonsterSheetStyle.inputField)
                        style { width(150.px); margin(0.px) }
                        onInput { event ->
                            val value = event.value
                            selectedSkill = Skill.values().find { it.name == value } ?: Skill.Acrobatics
                        }
                    }) {
                        Skill.values().forEach { skill ->
                            Option(skill.name, attrs = {
                                if (skill == selectedSkill) selected()
                            }) {
                                Text(skill.label)
                            }
                        }
                    }

                    Input(InputType.Number) {
                        classes(MonsterSheetStyle.inputField)
                        style { width(60.px); margin(0.px) }
                        value(bonus)
                        onInput { event -> bonus = event.value?.toString()?.toIntOrNull() ?: 0 }
                    }

                    Button(attrs = {
                        classes(MonsterSheetStyle.dndButton)
                        onClick {
                            val sign = if (bonus >= 0) "+" else ""
                            val newEntry = "${selectedSkill.label} $sign$bonus"
                            val currentList = if (skills.isBlank()) emptyList() else skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            
                            // Remove existing entry for this skill if exists to avoid duplicates/conflicts
                            val skillName = selectedSkill.label
                            val filteredList = currentList.filter { !it.startsWith(skillName) }
                            
                            val newList = filteredList + newEntry
                            val sortedList = newList.sorted()
                            
                            onValueChange(sortedList.joinToString(", "))
                        }
                    }) { Text("Add") }
                }
            }
        }
    } else {
        if (skills.isNotBlank()) {
            PropertyLine("Skills", skills, isEditingEnabled = false) {}
        }
    }
}
