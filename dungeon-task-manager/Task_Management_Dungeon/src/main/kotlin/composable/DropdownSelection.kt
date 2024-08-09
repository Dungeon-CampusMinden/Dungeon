
package composable

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Composable Function to show a selection in a dropdown menu
 * @param value Chosen value in the selection
 * @param itemList List of values to choose
 * @param modifier Modify appearance of the menu
 * @param onItemClick Function that runs if an item is clicked
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun dropdownSelection(
    value: String,
    itemList: List<String>,
    modifier: Modifier,
    onItemClick: (String) -> Unit
) {
    var showDropdown by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var copyValue by rememberSaveable { mutableStateOf(value) }
    ExposedDropdownMenuBox(
        expanded = showDropdown,
        onExpandedChange = {showDropdown = it }
    ){
        TextField(
            value = copyValue,
            modifier = modifier
                .menuAnchor(),
            readOnly = true,
            onValueChange = {},
            label = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)},
        )
        ExposedDropdownMenu(
            expanded = showDropdown,
            onDismissRequest = {showDropdown = false}
        ){
            itemList.forEachIndexed{index, item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        copyValue = item
                        onItemClick(item)
                        selectedIndex = index
                        showDropdown = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            onItemClick(copyValue)
        }

    }
}


