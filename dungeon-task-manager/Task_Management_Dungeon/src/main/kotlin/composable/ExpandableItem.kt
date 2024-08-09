package composable

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classes.AssignQuestion
import classes.MultipleChoiceQuestion
import classes.Question
import classes.SingleChoiceQuestion
import icon.addIcon
import icon.deleteIcon
import icon.editIcon


@Composable
fun expandableItem(question: Question, action: (Question) -> Unit, modifier: Modifier = Modifier, mode: Int = 0) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f
    )
    Card(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ).padding(8.dp),
        shape = shape,
        onClick = {
            expandedState = !expandedState
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(6f),
                    text = question.description,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.2f)
                        .rotate(rotationState),
                    onClick = {
                        expandedState = !expandedState
                    }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow"
                    )
                }
            }
            if (expandedState) {
                when (question) {
                    is SingleChoiceQuestion -> {
                        QuestionDisplay(question, showQuestion = false)
                    }

                    is MultipleChoiceQuestion -> {
                        QuestionDisplay(question, showQuestion = false)
                    }

                    is AssignQuestion -> {
                        QuestionDisplay(question, showQuestion = false)
                    }
                }
                when (mode) {
                    0 -> {
                        Image(
                            deleteIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                            "Remove Item",
                            Modifier.padding(10.dp).align(Alignment.End).clickable { action(question) })
                    }
                    1 -> {
                        Image(
                            addIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                            "add Item",
                            Modifier.padding(10.dp).align(Alignment.End).clickable { action(question) })
                    }
                    2 -> {
                        Image(
                            editIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                            "add Item",
                            Modifier.padding(10.dp).align(Alignment.End).clickable { action(question) })
                    }
                    3 -> {}
                }

            }

        }
    }
}