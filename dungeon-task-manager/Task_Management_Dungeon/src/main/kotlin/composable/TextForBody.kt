package composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable predefined text for this application
 * @param text Shown message
 * @param size Text size
 * @param modifier Modifier to modify text appearance
 */
@Composable
fun bodyText(text : String, size: Int = 16, modifier: Modifier = Modifier){
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Start,
        fontSize = size.sp,
        lineHeight = size.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(16.dp)
    )
}