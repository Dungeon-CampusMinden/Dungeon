package composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import icon.addIcon
import icon.deleteIcon

/**
 * Composable function to create a Stringlist
 * @param modifier Modify the used fields
 * @param onValueChange Function that runs when the addIcon is clicked. Uses String Value
 * @param taskLabel Textfield Label
 * @param outputLabel is shown above the output
 * @param minAmount TextField shows error if there are fewer assignments than minAmount
  */
@Composable
fun createStringList(
    modifier: Modifier,
    onValueChange: (SnapshotStateList<String>) -> Unit,
    taskLabel: String,
    textFieldLabel: String,
    outputLabel: String,
    minAmount: Int
) {
    var text by rememberSaveable { mutableStateOf("") }
    val strings = remember { mutableStateListOf<String>() }
    Column {
        bodyText(taskLabel)
        Row(content = {
            Image(
                addIcon(MaterialTheme.colorScheme.onBackground),
                "Add",
                Modifier.padding(4.dp, top = 26.dp).clickable {
                    if (text.isNotEmpty()) {
                        strings.add(text)
                    }
                    text = ""
                })
            OutlinedTextField(
                modifier = modifier.padding(8.dp).fillMaxWidth(),
                value = text,
                onValueChange = { text = it },
                label = { Text(textFieldLabel) },
                isError = strings.size < minAmount
            )
        })
        LazyColumn(Modifier.fillMaxWidth().size(200.dp).clip(shape = RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSecondary)) {
            item {
                Text(
                    text = outputLabel,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = modifier.padding(bottom = 10.dp, start = 8.dp, top = 8.dp)
                )
            }

            items(items = strings) { answer ->
                Card(
                    modifier = modifier.fillMaxWidth().padding(20.dp, end = 16.dp, bottom = 10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Box {
                        Row(
                            modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                deleteIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Remove Item",
                                Modifier.padding(10.dp).clickable { strings.remove(answer) })
                            Text(
                                "Antwort: $answer",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 24.sp,
                                lineHeight = 25.sp,
                                modifier = modifier.clickable {})
                        }
                    }
                }
            }
            onValueChange(strings)
        }
    }
}
