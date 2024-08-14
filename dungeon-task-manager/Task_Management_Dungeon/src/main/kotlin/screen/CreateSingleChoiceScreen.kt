package screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import classes.SingleChoiceQuestion
import com.example.compose.AppTheme
import composable.createStringList
import composable.inputNumberField
import composable.inputTextField
import composable.title
import kotlinx.coroutines.launch

/**
 * Screen to Create a Single Choice Question and pass it further to the next Screen (Creates everything but the correctAnswerIndex)
 */
class CreateSingleChoiceScreen : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        //Variablen für Single Choice Frage
        var questionText by rememberSaveable { mutableStateOf("") }
        var answers = remember { mutableStateListOf<String>() }
        var points by rememberSaveable { mutableStateOf("") }
        var pointsToPass by rememberSaveable { mutableStateOf("") }
        var explanation by rememberSaveable { mutableStateOf("") }
        var tags = remember { mutableStateListOf<String>() }
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
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
                                    if (questionText.isNotEmpty() && points.isNotEmpty() && explanation.isNotEmpty() && pointsToPass.isNotEmpty()) {
                                        navigator.push(
                                            SingleChoiceChooseAnswerIndexScreen(
                                                SingleChoiceQuestion(
                                                    0,
                                                    questionText,
                                                    points.toInt(),
                                                    pointsToPass.toInt(),
                                                    explanation,
                                                    answers,
                                                    tags
                                                )
                                            )
                                        )
                                    } else if (answers.size < 2) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Bitte geben Sie mindestens 2 Antwortmöglichkeiten an",
                                                withDismissAction = true
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Bitte füllen Sie alle Felder aus",
                                                withDismissAction = true
                                            )
                                        }
                                    }
                                }) {
                                Text("Weiter")
                            }
                        }
                    }
                ) {
                    LazyColumn(
                        Modifier.padding(
                            start = 48.dp,
                            top = 20.dp,
                            end = 48.dp,
                            bottom = 76.dp
                        )
                    ) {
                        item {
                            title("Single Choice Frage erstellen")
                        }
                        //Single_Choice_Frage erstellen
                        item {
                            inputTextField(
                                Modifier,
                                questionText,
                                onValueChange = { questionText = it },
                                "Frage eingeben",
                                questionText.isEmpty()
                            )
                        }
                        item {
                            createStringList(
                                Modifier,
                                onValueChange = { answers = it },
                                taskLabel = "Bitte mindestens 2 Antworten angeben",
                                outputLabel = "Antworten:",
                                textFieldLabel = "Antwort angeben",
                                minAmount = 2
                            )
                        }
                        item {
                            createStringList(
                                Modifier,
                                onValueChange = { tags = it },
                                taskLabel = "Bitte Tags angeben",
                                outputLabel = "Tags:",
                                textFieldLabel = "Tag angeben",
                                minAmount = 0
                            )
                        }
                        item {
                            inputTextField(
                                Modifier,
                                explanation,
                                onValueChange = { explanation = it },
                                "Erklärung angeben",
                                explanation.isEmpty()
                            )
                        }
                        item {
                            Row {
                                inputNumberField(
                                    Modifier.width(300.dp),
                                    points,
                                    onValueChange = { points = it },
                                    "Punkte"
                                )
                                inputNumberField(
                                    Modifier.width(300.dp),
                                    points,
                                    onValueChange = { pointsToPass = it },
                                    "Punkte zum bestehen"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}