package screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import classes.SingleChoiceQuestion
import com.example.compose.AppTheme
import composable.bodyText
import composable.title
import kotlinx.coroutines.launch

/**
 * Screen to choose the Correct Answer of the created Question and pass the Question further to the next Screen
 * @param question Single Choice Question created in Screen before
 */
class SingleChoiceChooseAnswerIndexScreen(private val question: SingleChoiceQuestion) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var selectedIndex by remember { mutableStateOf(-1) }
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold (
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    ){
                    LazyColumn(
                        Modifier.padding(
                            start = 24.dp,
                            top = 24.dp,
                            end = 24.dp
                        ).fillMaxSize()
                    ) {
                        item {
                            title("Bitte wählen Sie die korrekte Antwort aus")
                        }
                        itemsIndexed(items = question.answers) { index, answer ->
                            bodyText(answer, modifier = Modifier.selectable(
                                selected = selectedIndex == index,
                                onClick = {
                                    question.correctAnswerIndex = index
                                    selectedIndex = index
                                }
                            ).clip(shape = RoundedCornerShape(10.dp)).background(
                                if (index == question.correctAnswerIndex){
                                    MaterialTheme.colorScheme.onSecondary
                                }else MaterialTheme.colorScheme.background
                            ).fillParentMaxWidth().padding(start = 16.dp, end = 16.dp))
                            Spacer(modifier = Modifier.padding(8.dp))
                        }
                        item {
                            Row(//verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    modifier = Modifier.padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(),
                                    onClick = {
                                        navigator.pop()
                                    }) {
                                    Text("Zurück")
                                }
                                Button(
                                    modifier = Modifier.padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(),
                                    onClick = {
                                        if (question.correctAnswerIndex !=-1) {
                                            navigator.push(
                                                CheckSingleChoiceQuestionScreen(question = question)
                                            )
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Bitte wählen Sie die korrekte Antwort aus",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                    }) {
                                    Text("Weiter")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}