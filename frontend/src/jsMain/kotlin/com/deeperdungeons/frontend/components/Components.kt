package com.deeperdungeons.frontend.components

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import com.deeperdungeons.frontend.styles.MonsterSheetStyle
import com.deeperdungeons.shared.StatDto
import kotlin.math.floor

@Composable
fun EditableText(
    value: String,
    multiline: Boolean = false,
    isEditingEnabled: Boolean = true,
    onValueChange: (String) -> Unit,
    renderText: @Composable () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    // Reset editing state if editing is disabled globally
    if (!isEditingEnabled && isEditing) {
        isEditing = false
    }

    if (isEditing && isEditingEnabled) {
        Div({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(5.px); width(100.percent) } }) {
            if (multiline) {
                TextArea {
                    classes(MonsterSheetStyle.inputField)
                    value(value)
                    onInput { onValueChange(it.value) }
                }
            } else {
                Input(InputType.Text) {
                    classes(MonsterSheetStyle.inputField)
                    value(value)
                    onInput { onValueChange(it.value) }
                    onKeyDown { if (it.key == "Enter") isEditing = false }
                }
            }
            Button(attrs = { 
                classes(MonsterSheetStyle.dndButton)
                onClick { isEditing = false } 
            }) { Text("OK") }
        }
    } else {
        Span({
            if (isEditingEnabled) {
                onClick { isEditing = true }
                style {
                    cursor("pointer")
                    property("text-decoration", "underline dotted #ccc")
                    // Ensure empty fields are clickable in edit mode
                    if (value.isBlank()) {
                        display(DisplayStyle.InlineBlock)
                        minWidth(50.px)
                        backgroundColor(Color("#f0f0f0"))
                        padding(2.px)
                    }
                }
                title("Click to edit")
            }
        }) {
            if (value.isBlank() && isEditingEnabled) {
                Text("[Empty]")
            } else {
                renderText()
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, isEditingEnabled: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        Span({ classes(MonsterSheetStyle.propertyLabel) }) { Text("$label ") }
        EditableText(value, isEditingEnabled = isEditingEnabled, onValueChange = onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun AbilityScore(name: String, stat: StatDto, isEditingEnabled: Boolean, onValueChange: (StatDto) -> Unit) {
    Div({ classes(MonsterSheetStyle.abilityScore) }) {
        Div({ classes(MonsterSheetStyle.abilityScoreLabel) }) { Text(name) }
        
        if (isEditingEnabled) {
            Input(InputType.Number) {
                classes(MonsterSheetStyle.inputField)
                style { textAlign("center"); width(50.px) }
                value(stat.value)
                onInput { event ->
                    val newValue = event.value?.toString()?.toIntOrNull() ?: 10
                    // Modifier is calculated on backend, but for immediate UI feedback we can approximate or wait for save
                    // Here we just update the value, the modifier will be updated on save/reload or we can calc locally
                    val newModifier = floor((newValue - 10) / 2.0).toInt()
                    onValueChange(StatDto(newValue, newModifier))
                }
            }
        } else {
            Div { Text("${stat.value} (${if (stat.modifier >= 0) "+" else ""}${stat.modifier})") }
        }
    }
}

@Composable
fun PropertyLine(label: String, value: String, isEditingEnabled: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        Span({ classes(MonsterSheetStyle.propertyLabel) }) { Text("$label ") }
        EditableText(value, isEditingEnabled = isEditingEnabled, onValueChange = onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun TraitBlock(
    name: String, 
    description: String, 
    isEditingEnabled: Boolean,
    onNameChange: (String) -> Unit, 
    onDescChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Div({ 
        classes(MonsterSheetStyle.traitBlock)
        style { position(Position.Relative) }
    }) {
        if (isEditingEnabled) {
            Button(attrs = {
                style {
                    property("float", "right")
                    fontSize(10.px)
                    color(Color.red)
                    border(0.px)
                    backgroundColor(Color.transparent)
                    cursor("pointer")
                }
                onClick { onDelete() }
                title("Delete Trait")
            }) { Text("✕") }
        }

        EditableText(name, isEditingEnabled = isEditingEnabled, onValueChange = onNameChange) {
            Span({ classes(MonsterSheetStyle.traitName) }) { Text("$name.") }
        }
        EditableText(description, multiline = true, isEditingEnabled = isEditingEnabled, onValueChange = onDescChange) {
            Text(description)
        }
    }
}

@Composable
fun TaperedRule() {
    Hr({ classes(MonsterSheetStyle.taperedRule) })
}

@Composable
fun AddButton(label: String, onClick: () -> Unit) {
    Button(attrs = {
        classes(MonsterSheetStyle.dndButton)
        style {
            marginTop(5.px)
        }
        onClick { onClick() }
    }) {
        Text("+ $label")
    }
}