@file:OptIn(ExperimentalLayoutApi::class)

package screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.compose.AppTheme
import composable.clickableIconCardExit
import composable.clickableIconCardNavigate
import composable.title
import icon.*

/**
 * Homescreen of the Application. Allows to navigate to all the functionalities.
 */
class HomeScreen : Screen {
    @Composable
    @Preview
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box (
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    title("Willkommen im Dungeon Task Manager")
                    Box(
                        Modifier.fillMaxSize().padding(top = 30.dp).align(Alignment.Center)
                    ) {
                        FlowRow(
                            Modifier.height(intrinsicSize = IntrinsicSize.Max)
                                .width(intrinsicSize = IntrinsicSize.Max)
                                .align(Alignment.Center).padding(top = 60.dp),
                            maxItemsInEachRow = 3,
                        ) {
                            clickableIconCardNavigate(
                                projectIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Projekte",
                                Modifier.padding(8.dp),
                                navigator,
                                ProjectScreen()
                            )
                            clickableIconCardNavigate(
                                addFileIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Aufgabendatei\ngenerieren",
                                Modifier.padding(8.dp),
                                navigator,
                                CreateTaskFileScreen()
                            )
                            clickableIconCardNavigate(
                                squarePlusIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Neue Aufgabe",
                                Modifier.padding(8.dp),
                                navigator,
                                QuestionChoiceScreen()
                            )
                            /*
                            clickableIconCardNavigate(
                                walkingIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Simulieren",
                                Modifier.padding(8.dp),
                                navigator,
                                HomeScreen()
                            )*/
                            clickableIconCardNavigate(
                                rectangleListIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Fragenliste",
                                Modifier.padding(8.dp),
                                navigator,
                                QuestionOverviewScreen()
                            )
                            clickableIconCardExit(
                                exitIcon(MaterialTheme.colorScheme.onSurfaceVariant),
                                "Beenden",
                                Modifier.padding(8.dp)
                            )
                        }
                    }

                }
            }
        }
    }
}