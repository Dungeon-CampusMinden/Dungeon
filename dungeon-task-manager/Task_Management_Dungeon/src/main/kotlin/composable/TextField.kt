package composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable function for a textfield
 * @param modifier Modify appearance of
 * @param value Value inside the textfield
 * @param onValueChange Function that runs when the input value changes
 * @param label Label of the textfield
 * @param isError Statement to define the error. True if you don't want any error
 */
@Composable
fun inputTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label : String,
    isError: Boolean
) {
    var text by remember { mutableStateOf(value) }
    OutlinedTextField(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        value = text,
        onValueChange = { text = it
            onValueChange(it)},
        label = { Text(label) },
        isError = isError
    )
}