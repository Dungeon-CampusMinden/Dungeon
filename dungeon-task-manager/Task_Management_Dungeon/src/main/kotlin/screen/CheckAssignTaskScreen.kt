package screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import classes.AssignQuestion
import com.example.compose.AppTheme
import composable.QuestionDisplay
import composable.title
import databaseInteraction.Driver
import databaseInteraction.Provider
import kotlinx.coroutines.runBlocking

/**
 * Screen to check assign question before saving
 * @param question Question to check
 */
class CheckAssignTaskScreen(val question : AssignQuestion) : Screen{
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold {
                    LazyColumn(
                        Modifier.padding(
                            start = 24.dp,
                            top = 24.dp,
                            end = 24.dp
                        )
                    ) {
                        item {
                            title("Bitte kontrollieren Sie die Angaben")
                        }
                        item {
                            QuestionDisplay(question, Modifier.fillMaxWidth())
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
                                        //ADD QUESTION TO DATABASE
                                        addAssignQuestion(question)
                                        navigator.popUntilRoot()
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

    private suspend fun addTags(questionId: Long, newTags: List<String>) {
        val tagData = Provider.provideTagDataSource(Driver.createDriver())
        val tagQuestionData = Provider.provideQuestionTagDataSource(Driver.createDriver())
        newTags.forEach{newTag ->
            if (tagData.getTagByName(newTag)!= null){
                tagQuestionData.insertQuestionTag(questionId = questionId,tagData.getTagByName(newTag)!!)
            }else{
                tagData.insertTag(newTag)
                tagQuestionData.insertQuestionTag(questionId, tagData.getTagByName(newTag)!!)
            }
        }
    }

    private fun addAssignQuestion(question : AssignQuestion) {
        val questionData = Provider.provideQuestionDataSource(Driver.createDriver())
        val answerData = Provider.provideAssignmentDataSource(Driver.createDriver())
        runBlocking {
            //Frage einfügen
            questionData.insertQuestion(
                question.description,
                question.explanation,
                question.points.toLong(),
                question.pointsToPass.toLong(),
                "ASSIGN_QUESTION",
            )
            //Antworten einfügen
            question.assignments.forEach { assignment ->
                answerData.insertAssignment(
                    termA = assignment.termA,
                    termB = assignment.termB,
                    questionId = questionData.getQuestionId(
                        question.description,
                        question.explanation,
                        question.points.toLong(),
                        question.pointsToPass.toLong()
                    )!!
                )
                //TAGS einfügen
               // addTags(questionId = questionData.getQuestionId(question.description, question.explanation, question.points.toLong(), question.pointsToPass.toLong())!!,question.tags)
            }
        }
    }
}