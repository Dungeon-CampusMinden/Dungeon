package screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import classes.SingleChoiceQuestion
import com.example.compose.AppTheme
import composable.QuestionDisplay
import composable.title
import databaseInteraction.Driver
import databaseInteraction.Provider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

/**
 * Screen to check the Question before saving it to the Database
 * @param question Single Choice Question created in Screen before
 */
class CheckSingleChoiceQuestionScreen(val question: SingleChoiceQuestion) : Screen {
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
                                        addSingleChoiceQuestion(question)
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
        /* Fehlerhafte Lösung
        val tagData = Provider.provideTagDataSource(Driver.createDriver())
        val tagQuestionData = Provider.provideQuestionTagDataSource(Driver.createDriver())
        var tagsFromDB = tagData.getAllTags().firstOrNull()
        var tagsToAdd = mutableListOf<String>()
        if (tagsFromDB != null) {
            newTags.forEach { tag ->
                tagsToAdd.add(tag)
            }
            tagsFromDB.forEach { tagDB ->
                if (tagsToAdd.contains(tagDB.tag)) {
                    tagQuestionData.insertQuestionTag(questionId = questionId, tagData.getTagByName(tagDB.tag+"1")!!)
                    tagsToAdd.remove(tagDB.tag)
                }
            }
            while (tagsToAdd != null){
                tagsToAdd[0]
                tagData.insertTag(tagsToAdd[0])
                tagQuestionData.insertQuestionTag(questionId, tagData.getTagByName(tagsToAdd.get(0))!!)
                tagsToAdd.remove(tagsToAdd[0])
            }
        } else {
            while (tagsToAdd.isNotEmpty()){
                tagsToAdd.get(0)
                tagData.insertTag(tagsToAdd[0])
                tagQuestionData.insertQuestionTag(questionId, tagData.getTagByName(tagsToAdd[0])!!)
                tagsToAdd.remove(tagsToAdd[0])
            }
        }*/
    }

    private fun addSingleChoiceQuestion(question: SingleChoiceQuestion) {
        val questionData = Provider.provideQuestionDataSource(Driver.createDriver())
        val answerData = Provider.provideAnswerDataSource(Driver.createDriver())
        runBlocking {
            //Frage einfügen
            questionData.insertQuestion(
                question.description,
                question.explanation,
                question.points.toLong(),
                question.pointsToPass.toLong(),
                "SINGLE_CHOICE_QUESTION",
            )
            //Antworten einfügen
            question.answers.forEachIndexed { index, answer ->
                answerData.insertAnswer(
                    answer = answer,
                    questionId = questionData.getQuestionId(
                        question.description,
                        question.explanation,
                        question.points.toLong(),
                        question.pointsToPass.toLong()
                    )!!
                )
                //Korrekte Anworten anfügen
                if (question.correctAnswerIndex == index) {
                    answerData.setCorrectAnswer(
                        answerData.getAnswerId(
                            answer = answer,
                            questionId = questionData.getQuestionId(
                                question.description,
                                question.explanation,
                                question.points.toLong(),
                                question.pointsToPass.toLong()
                            )!!
                        )!!
                    )
                }
                //addTags(questionId = questionData.getQuestionId(question.description, question.explanation, question.points.toLong(),question.pointsToPass.toLong())!!, question.tags)
            }
        }
    }
}