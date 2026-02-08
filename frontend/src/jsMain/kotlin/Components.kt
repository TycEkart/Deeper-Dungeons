import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

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
            Button(attrs = { onClick { isEditing = false } }) { Text("OK") }
        }
    } else {
        Span({
            if (isEditingEnabled) {
                onClick { isEditing = true }
                style {
                    cursor("pointer")
                    property("text-decoration", "underline dotted #ccc")
                }
                title("Click to edit")
            }
        }) {
            renderText()
        }
    }
}

@Composable
fun StatBox(label: String, value: String, isEditingEnabled: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        B { Text("$label ") }
        EditableText(value, isEditingEnabled = isEditingEnabled, onValueChange = onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun AbilityScore(name: String, value: String, isEditingEnabled: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.abilityScore) }) {
        Div({ style { fontWeight("bold") } }) { Text(name) }
        EditableText(value, isEditingEnabled = isEditingEnabled, onValueChange = onValueChange) {
            Div { Text(value) }
        }
    }
}

@Composable
fun PropertyLine(label: String, value: String, isEditingEnabled: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        B { Text("$label ") }
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
            B({ style { fontStyle("italic") } }) { Text("$name. ") }
        }
        EditableText(description, multiline = true, isEditingEnabled = isEditingEnabled, onValueChange = onDescChange) {
            Text(description)
        }
    }
}

@Composable
fun AddButton(label: String, onClick: () -> Unit) {
    Button(attrs = {
        style {
            marginTop(5.px)
            fontSize(12.px)
            cursor("pointer")
        }
        onClick { onClick() }
    }) {
        Text("+ $label")
    }
}