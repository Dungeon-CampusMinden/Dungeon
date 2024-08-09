package screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.compose.AppTheme
import composable.bodyText
import composable.inputTextField
import composable.title
import databaseInteraction.Driver
import databaseInteraction.Provider
import databaseInteraction.TaskFileWriter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Screen to create the dungeon task file
 */
class CreateTaskFileScreen : Screen {
    @Composable
    @Preview
    override fun Content() {
        val projectData = Provider.provideProjectDataSource(Driver.createDriver())
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        var filename by rememberSaveable { mutableStateOf("") }
        val projects = projectData.getAllProjects().collectAsState(initial = emptyList()).value
        val selectedIndices = remember { mutableStateListOf<Int>() }
        val selectedProjectIds = remember { mutableStateListOf<Long>() }
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        Column {
                            title("Aufgabendatei erstellen")
                            bodyText("Bitte wählen Sie die Projekte, die in die Aufgabendatei aufgenommen werden sollen und bestätigen Sie mit weiter")
                        }
                    },
                    bottomBar = {
                        Row(//verticalAlignment = Alignment.Bottom,
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
                                    if (selectedIndices.isEmpty()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Bitte wählen Sie mindestens ein Projekt aus",
                                                withDismissAction = true
                                            )
                                        }
                                    } else if (filename.isEmpty()){
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Bitte geben Sie einen Dateinamen an",
                                                withDismissAction = true
                                            )
                                        }
                                    } else {
                                        selectedProjectIds.forEach {
                                            TaskFileWriter.writeProjectToFile(it, filename)
                                        }
                                        while (selectedIndices.isNotEmpty()){
                                            selectedIndices.removeLast()
                                            filename= ""
                                        }
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Datei wurde erstellt!",
                                                withDismissAction = true
                                            )
                                        }
                                    }
                                }) {
                                Text("Datei Speichern")
                            }
                        }
                    }
                ) {
                    LazyColumn(
                        Modifier.padding(it).fillMaxSize()
                    ) {
                        item {
                            inputTextField(
                                label = "Dateinamen angeben",
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp),
                                value = filename,
                                onValueChange = { filename = it },
                                isError = filename.isEmpty()
                            )
                        }
                        itemsIndexed(items = projects) { index, project ->
                            bodyText(project.name, modifier = Modifier.selectable(
                                selected = selectedIndices.contains(index),
                                onClick = {
                                    if (!selectedIndices.contains(index)) {
                                        selectedIndices.add(index)
                                        selectedProjectIds.add(project.id)
                                    } else {
                                        selectedIndices.remove(index)
                                        selectedProjectIds.remove(project.id)
                                    }
                                }
                            ).clip(shape = RoundedCornerShape(10.dp)).background(
                                if (selectedIndices.contains(index)) {
                                    MaterialTheme.colorScheme.onSecondary
                                } else MaterialTheme.colorScheme.background
                            ).fillParentMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp))
                            Spacer(modifier = Modifier.padding(8.dp))
                        }

                    }
                }

            }
        }
    }
}
