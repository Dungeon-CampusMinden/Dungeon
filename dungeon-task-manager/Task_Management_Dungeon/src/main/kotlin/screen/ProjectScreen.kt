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
import classes.*
import com.example.compose.AppTheme
import composable.bodyText
import composable.inputTextField
import composable.title
import databaseInteraction.Driver
import databaseInteraction.Provider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Welcome project screen. Passes the chosen project to the next screen
 */
class ProjectScreen : Screen {
    @Composable
    @Preview
    override fun Content() {
        val projectData = Provider.provideProjectDataSource(Driver.createDriver())
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        var newProjectName by rememberSaveable { mutableStateOf("") }
        var selectedIndex by rememberSaveable { mutableStateOf(-1) }
        val projects = projectData.getAllProjects().collectAsState(initial = emptyList()).value

        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) } ,
                    topBar = {
                        Column {
                            title("Projekte")
                            bodyText(
                                "Erstellen Sie ein neues Projekt erstellen oder bearbeiten Sie ein vorhandenes",
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                            )
                        }
                    },
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
                                    scope.launch {
                                        if (selectedIndex == -1 && newProjectName.isEmpty()){
                                            snackbarHostState.showSnackbar(
                                                message = "Bitte wählen Sie ein Projekt aus oder tragen Sie einen Namen für das neue Projekt ein",
                                                withDismissAction = true
                                            )
                                        }else if (selectedIndex != -1 && newProjectName.isNotEmpty()){
                                            snackbarHostState.showSnackbar(
                                                message = "Bitte wählen Sie ein Projekt aus oder tragen Sie einen Namen für das neue Projekt ein.\nBeides ist nicht zulässig!",
                                                withDismissAction = true
                                            )
                                        }else if(selectedIndex != -1){
                                            navigator.push(EditProjectScreen(projects.get(selectedIndex).id))
                                        }else{
                                            projectData.insertProject(newProjectName)
                                            navigator.push(EditProjectScreen(projectData.getProjectId(newProjectName)!!))
                                        }
                                    }
                                }) {
                                Text("Weiter")
                            }

                        }
                    }
                ) {
                    Column(modifier = Modifier.padding(it)) {
                        inputTextField(
                            label = "Neues Projekt anlegen",
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp),
                            value = newProjectName,
                            onValueChange = { newProjectName = it },
                            isError = false
                        )
                        LazyColumn(
                            Modifier.padding(
                                start = 24.dp,
                                top = 24.dp,
                                end = 24.dp
                            ).fillMaxSize()
                        ) {
                            item {
                                title("Vorhandene Projekte")
                            }
                            itemsIndexed(items = projects) { index, project ->
                                bodyText(project.name, modifier = Modifier.selectable(
                                    selected = selectedIndex == index,
                                    onClick = {
                                        selectedIndex = if(selectedIndex != index) {
                                            index
                                        } else {
                                            -1
                                        }
                                    }
                                ).clip(shape = RoundedCornerShape(10.dp)).background(
                                    if (index == selectedIndex) {
                                        MaterialTheme.colorScheme.onSecondary
                                    } else MaterialTheme.colorScheme.background
                                ).fillParentMaxWidth().padding(start = 16.dp, end = 16.dp))
                                Spacer(modifier = Modifier.padding(8.dp))
                            }


                        }

                    }
                }

            }
        }
    }
}