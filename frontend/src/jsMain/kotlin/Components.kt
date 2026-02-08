import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.fontStyle
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.*

@Composable
fun EditableText(
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    renderText: @Composable () -> Unit
) {
    if (isEditing) {
        Input(InputType.Text) {
            classes(MonsterSheetStyle.inputField)
            value(value)
            onInput { onValueChange(it.value) }
        }
    } else {
        renderText()
    }
}

@Composable
fun StatBox(label: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        B { Text("$label ") }
        EditableText(value, isEditing, onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun AbilityScore(name: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.abilityScore) }) {
        Div({ style { fontWeight("bold") } }) { Text(name) }
        EditableText(value, isEditing, onValueChange) {
            Div { Text(value) }
        }
    }
}

@Composable
fun PropertyLine(label: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.propertyLine) }) {
        B { Text("$label ") }
        EditableText(value, isEditing, onValueChange) {
            Text(value)
        }
    }
}

@Composable
fun TraitBlock(name: String, description: String, isEditing: Boolean, onNameChange: (String) -> Unit, onDescChange: (String) -> Unit) {
    Div({ classes(MonsterSheetStyle.traitBlock) }) {
        if (isEditing) {
            Input(InputType.Text) {
                classes(MonsterSheetStyle.inputField)
                value(name)
                placeholder("Trait Name")
                onInput { onNameChange(it.value) }
            }
            TextArea {
                classes(MonsterSheetStyle.inputField)
                value(description)
                placeholder("Description")
                onInput { onDescChange(it.value) }
            }
        } else {
            B({ style { fontStyle("italic") } }) { Text("$name. ") }
            Text(description)
        }
    }
}