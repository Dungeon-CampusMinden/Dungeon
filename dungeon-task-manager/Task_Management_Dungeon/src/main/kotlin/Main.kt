import Task_Management_Dungeon.Database
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import databaseInteraction.Driver
import screen.HomeScreen
import java.awt.Dimension
import java.io.File


const val MIN_HEIGHT = 300
const val MIN_WIDTH = 400

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
        
    }
}

fun main() = application {
    //val driver = Driver.createDriver()
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = "Dungeon Task Manager",
    ) {
        window.minimumSize = Dimension(500, 500)
        Navigator(HomeScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
