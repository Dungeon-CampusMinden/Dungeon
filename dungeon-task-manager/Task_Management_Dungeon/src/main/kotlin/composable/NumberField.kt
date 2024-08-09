package composable

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * @param modifier Modify appearance of
 * @param value Value inside the textfield
 * @param onValueChange Function that runs when the input value changes
 * @param label Label of the textfield
 */
@Composable
fun inputNumberField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label : String
) {
    var text by remember { mutableStateOf(value) }

    OutlinedTextField(
        modifier = modifier.padding(8.dp),
        value = text,
        onValueChange = {
            text = it.filter { symbol ->
                symbol.isDigit()
            }
            onValueChange(it)},
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = text.isEmpty()
    )
}

