import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun EditableText(
    value: String,
    multiline: Boolean = false,
    onValueChange: (String) -> Unit,
    renderText: @Composable () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
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
            onClick { isEditing = true }
            style {
                cursor("pointer")
                property("text-decoration", "underline dotted #ccc")
            }
            title("Click to edit")
        }) {
            renderText()
        }
    }
}

@Composable
fun StatBox(label: String, value: String, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        B { Text("$label ") }
        EditableText(value, onValueChange = onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun AbilityScore(name: String, value: String, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.abilityScore) }) {
        Div({ style { fontWeight("bold") } }) { Text(name) }
        EditableText(value, onValueChange = onValueChange) {
            Div { Text(value) }
        }
    }
}

@Composable
fun PropertyLine(label: String, value: String, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        B { Text("$label ") }
        EditableText(value, onValueChange = onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun TraitBlock(name: String, description: String, onNameChange: (String) -> Unit, onDescChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.traitBlock) }) {
        EditableText(name, onValueChange = onNameChange) {
            B({ style { fontStyle("italic") } }) { Text("$name. ") }
        }
        EditableText(description, multiline = true, onValueChange = onDescChange) {
            Text(description)
        }
    }
}