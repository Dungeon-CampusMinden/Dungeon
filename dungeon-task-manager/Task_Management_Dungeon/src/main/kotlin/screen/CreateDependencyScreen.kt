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
import classes.Dependency
import com.example.compose.AppTheme
import composable.bodyText
import composable.title
import databaseInteraction.Driver
import databaseInteraction.Provider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Screen to choose the dependency type
 * @param dependency dependency to add
 */
class CreateDependencyScreen(private var dependency: Dependency) : Screen {
    @Composable
    override fun Content() {
        val dependencyData = Provider.provideDependencyDataSource(Driver.createDriver())
        val questionData = Provider.provideQuestionDataSource(Driver.createDriver())
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var selectedIndex by remember { mutableStateOf(-1) }
        val dependencyList = mutableListOf(
            "Sequenz",
            "Pflicht Unteraufgabe",
            "Optionale Unteraufgabe",
            "Bei falscher Antwort",
            "Bei richtiger Antwort"
        )

        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) {
                    LazyColumn(
                        Modifier.padding(
                            start = 24.dp,
                            top = 24.dp,
                            end = 24.dp
                        ).fillMaxSize()
                    ) {
                        item {
                            title("Bitte wählen Sie die Abhängigkeit aus")
                        }
                        itemsIndexed(items = dependencyList) { index, answer ->
                            bodyText(answer, modifier = Modifier.selectable(
                                selected = selectedIndex == index,
                                onClick = {
                                    dependency.dependency = dependencyList[index]
                                    selectedIndex = index
                                }
                            ).clip(shape = RoundedCornerShape(10.dp)).background(
                                if (selectedIndex == index) {
                                    MaterialTheme.colorScheme.onSecondary
                                } else MaterialTheme.colorScheme.background
                            ).fillParentMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp))
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
                                        if (dependencyList.contains(dependency.dependency)) {
                                            runBlocking {
                                                dependencyData.insertDependency(
                                                    questionAId = questionData.getQuestionId(
                                                        dependency.questionA!!.description,
                                                        dependency.questionA!!.explanation,
                                                        dependency.questionA!!.points.toLong(),
                                                        dependency.questionA!!.pointsToPass.toLong()
                                                    )!!,
                                                    questionData.getQuestionId(
                                                        dependency.questionB!!.description,
                                                        dependency.questionB!!.explanation,
                                                        dependency.questionB!!.points.toLong(),
                                                        dependency.questionB!!.pointsToPass.toLong()
                                                    )!!,
                                                    projectId = dependency.projectId,
                                                    0,
                                                    dependencyList[selectedIndex]
                                                )
                                            }
                                            navigator.popUntil {it is EditProjectScreen }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Bitte wählen Sie eine Bedingung aus",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                    }) {
                                    Text("Speichern")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}