package screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import classes.*
import com.example.compose.AppTheme
import composable.*
import databaseInteraction.Driver
import databaseInteraction.Provider
import icon.addIcon
import kotlinx.coroutines.runBlocking

/**
 * Screen to edit a project
 * @param projectId Project to edit
 */
class EditProjectScreen(private val projectId: Long) : Screen {
    @Composable
    @Preview
    override fun Content() {
        val projectData = Provider.provideProjectDataSource(Driver.createDriver())
        val dependencyData = Provider.provideDependencyDataSource(Driver.createDriver())
        val project = runBlocking { projectData.getProjectById(projectId) }
        var projectDependencies = dependencyData.getAllDependenciesByProjectId(projectId).collectAsState(initial = emptyList()).value
        val dependencyCopyList = SnapshotStateList<db.Dependency>()
        projectDependencies.forEach{dependencyCopyList.add(it)}
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        if (project != null) {
                            title(project.name)
                        } else {
                            title("FEHLER! Bitte gehen Sie zurück ins Hauptmenü")
                        }

                    },
                    bottomBar = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
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
                                    navigator.popUntilRoot()
                                }) {
                                Text("Fertig")
                            }
                        }
                    }
                ) {
                    LazyColumn(Modifier.padding(it)) {
                        item {
                            title("Spielablauf:")
                            bodyText("Bitte wählen Sie Fragen aus, die in das Spiel integriert werden sollen. Jede Frage muss in Abhängigkeit zu einer anderen Frage stehen. Die Art der Abhängigkeiten kann dabei frei gewählt werden. Hat eine Frage mehrere Abhängigkeiten, muss die Frage mehrmals ausgewählt und zugeordnet werden.")

                        }
                        items(items = dependencyCopyList) { dependency ->
                            dependencyCard(dependency, action = {dependencyCopyList.remove(dependency) })
                        }
                        item {
                            Card(
                                Modifier.padding(top = 16.dp, start = 128.dp, end = 128.dp).clickable {
                                    navigator.push(QuestionChooserScreen(Dependency(projectId)))
                                }
                            ) {
                                Row(
                                    Modifier.padding(start = 48.dp, end = 48.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        addIcon(MaterialTheme.colorScheme.onBackground),
                                        "Add",
                                        Modifier.padding(4.dp)
                                    )
                                    Text("Neuen Aufgabenteil hinzufügen", textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
