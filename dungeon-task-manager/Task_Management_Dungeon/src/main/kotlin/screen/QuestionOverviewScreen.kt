package screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import classes.*
import com.example.compose.AppTheme
import composable.checkBoxFilter
import composable.expandableItem
import composable.inputTextField
import databaseInteraction.Driver
import databaseInteraction.Provider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * Scrren to get an overview of all Questions
 */
class QuestionOverviewScreen : Screen {
    private fun filterSearchbar(searchBar: String, item: Question): Boolean {
        if (item.description.lowercase().contains(searchBar.lowercase())) {
            return true
        } else if (item.explanation.lowercase().contains(searchBar.lowercase())) {
            return true
        }
        return false
    }


    private suspend fun getAnswersToQuestionId(questionId: Long): List<String> {
        val answerData = Provider.provideAnswerDataSource(Driver.createDriver())
        val answerDataList = answerData.getAnswersByQuestionId(questionId).firstOrNull()
        //sval aaa = answerDataList.forEach {  }
        val answers = mutableListOf<String>()
        answerDataList!!.forEach(){answer ->
            answers.add(answer.answer)
        }
        //LOAD ANSWER
        return answers
    }

    private suspend fun getCorrectAnswersByQuestionId(questionId: Long): List<String> {
        val answerData = Provider.provideAnswerDataSource(Driver.createDriver())
        val answerList = answerData.getCorrectAnswersByQuestionId(questionId).firstOrNull()
        //LOAD ANSWER
        var answers = mutableListOf<String>()
        answerList!!.forEach(){answer ->
            answers.add(answer.answer)
        }
        //LOAD ANSWER
        return answers
    }


    private suspend fun getAssignmentsToQuestionId(questionId: Long): List<Assignment> {
        val assignmentData = Provider.provideAssignmentDataSource(Driver.createDriver())
        val assignmentList = assignmentData.getAssignmentsByQuestionId(questionId).firstOrNull()
        val assignments = mutableListOf<Assignment>()
        if (assignmentList!= null){
            assignmentList.forEach{
                assignments.add(Assignment(it.termA!!,it.termB!!))
            }
        }
        return assignments
    }

    private suspend fun getTagsToQuestionId(questionId: Long): List<String> {
        val tagData = Provider.provideTagDataSource(Driver.createDriver())
        val tagQuestionData = Provider.provideQuestionTagDataSource(Driver.createDriver())
        val tagDataList = tagData.getTagsByQuestionId(questionId).firstOrNull()
        //LOAD ANSWER
        var tags = mutableListOf<String>()
        tagDataList!!.forEach(){tag ->
            tags.add(tag)
        }
        //LOAD TAGS
        return tags
    }

    private suspend fun getQuestions(): List<db.Question> {
        val questionData = Provider.provideQuestionDataSource(Driver.createDriver())
        val tagQuestionData = Provider.provideQuestionTagDataSource(Driver.createDriver())
        val questionList = mutableStateListOf<db.Question>()
        return questionList
    }


    private suspend fun getAllQuestionsAsClasses(questions: List<db.Question>): List<Question> {
        val questionData = Provider.provideQuestionDataSource(Driver.createDriver())
        //val answerData = Provider.provideAnswerDataSource(Driver.createDriver())
        val tagData = Provider.provideTagDataSource(Driver.createDriver())
        val questionList = mutableStateListOf<Question>()
        var tags: List<String> = mutableStateListOf()
        var answers: List<String> = mutableStateListOf()
        var correctAnswers: List<String> = mutableStateListOf()
        var correctAnswerIndices = mutableListOf<Int>()
        var assignments: List<Assignment> = mutableStateListOf()
        //LOAD QuestionsDATA
        val questionDataList = getQuestions()
        //FOR EACH QUESTION ->
        questions.forEach { question ->
            //GET ANSWERS
            tags = getTagsToQuestionId(question.id)
            if (question.type == "SINGLE_CHOICE_QUESTION") {
                answers = getAnswersToQuestionId(question.id)
                correctAnswers = getCorrectAnswersByQuestionId(question.id)
                answers.forEachIndexed() { index, answer ->
                    if (correctAnswers.contains(answer)) {
                        correctAnswerIndices.add(index)
                    }
                }
                questionList.add(
                    SingleChoiceQuestion(
                        0,
                        question.description,
                        question.points.toInt(),
                        question.pointsToPass.toInt(),
                        question.explanation,
                        answers,
                        tags,
                        correctAnswerIndices[0]
                    )
                )
                correctAnswerIndices = mutableStateListOf()
            } else if (question.type == "MULTIPLE_CHOICE_QUESTION") {
                answers = getAnswersToQuestionId(question.id)
                correctAnswers = getCorrectAnswersByQuestionId(question.id)
                answers.forEachIndexed() { index, answer ->
                    if (correctAnswers.contains(answer)) {
                        correctAnswerIndices.add(index)
                    }
                }
                questionList.add(
                    MultipleChoiceQuestion(
                        0,
                        question.description,
                        question.points.toInt(),
                        question.pointsToPass.toInt(),
                        question.explanation,
                        answers = answers,
                        tags = tags,
                        correctAnswerIndices = correctAnswerIndices
                    )
                )
                correctAnswerIndices = mutableStateListOf()
            } else if (question.type == "ASSIGN_QUESTION") {
                tags = getTagsToQuestionId(question.id)
                assignments = getAssignmentsToQuestionId(questionId = question.id)
                questionList.add(
                    AssignQuestion(
                        0,
                        question.description,
                        question.points.toInt(),
                        question.pointsToPass.toInt(),
                        question.explanation,
                        assignments = assignments
                    )
                )
            } else {
            }
            // GET TAGS
        }
        return questionList
    }

    @Composable
    @Preview
    override fun Content() {
        val tagData = Provider.provideTagDataSource(Driver.createDriver())
        val tagList = tagData.getAllTags().collectAsState(initial = emptyList()).value
        val tagFilterList = remember { mutableStateListOf<String>() }
        //Get all questions from DB
        val questionData = Provider.provideQuestionDataSource(Driver.createDriver())
        val questionDataList = questionData.getAllQuestions().collectAsState(initial = emptyList()).value
        //Turn all Questions to full Question classes and connect them to Answers and Tags
        var questionList = runBlocking {
            getAllQuestionsAsClasses(questionDataList)
        }
        var searchBar by rememberSaveable { mutableStateOf("") }
        val navigator = LocalNavigator.currentOrThrow
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    topBar = {
                        inputTextField(Modifier, searchBar, onValueChange = { searchBar = it }, "Suche", false)
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
                        }
                    }
                ) {
                    Row {
                        Column(
                            Modifier.padding(
                                it
                            ).weight(1f)
                        ) {
                            tagList.forEach { tag ->
                                checkBoxFilter(tag.tag,  onCheckedTrue = {
                                    tagFilterList.add(tag.tag)
                                },
                                    onCheckedFalse = {
                                        tagFilterList.remove(tag.tag)
                                    })
                            }
                        }
                        LazyColumn(
                            Modifier.padding(
                                it
                            ).weight(5f)
                        ) {
                            items(items = questionList) { item ->
                                if (tagFilterList.isNotEmpty() && searchBar.isNotEmpty()) {
                                    tagFilterList.forEach {
                                        if (item.tags.contains(it)) {
                                            if (filterSearchbar(it, item)) {
                                                if (filterSearchbar(searchBar, item)) {
                                                    expandableItem(question = item, action = {
                                                        runBlocking {
                                                            questionData.deleteQuestionById(
                                                                questionData.getQuestionId(
                                                                    item.description,
                                                                    item.explanation,
                                                                    item.points.toLong(),
                                                                    item.pointsToPass.toLong()
                                                                )!!
                                                            )
                                                            getAllQuestionsAsClasses(questionDataList)
                                                        }

                                                    }, modifier = Modifier.fillMaxWidth())
                                                }
                                            }
                                        }
                                    }
                                } else if (searchBar.isEmpty() && tagFilterList.isNotEmpty()) {
                                    tagFilterList.forEach {
                                        if (item.tags.contains(it)) {
                                            if (filterSearchbar(it, item)) {
                                                expandableItem(
                                                    question = item,
                                                    action = { runBlocking {
                                                        questionData.deleteQuestionById(
                                                            questionData.getQuestionId(
                                                                item.description,
                                                                item.explanation,
                                                                item.points.toLong(),
                                                                item.pointsToPass.toLong()
                                                            )!!
                                                        )
                                                        getAllQuestionsAsClasses(questionDataList)
                                                    }},
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                } else if (searchBar.isNotEmpty() && tagFilterList.isEmpty()) {
                                    if (filterSearchbar(searchBar, item)) {
                                        expandableItem(
                                            question = item,
                                            action = { runBlocking {
                                                questionData.deleteQuestionById(
                                                    questionData.getQuestionId(
                                                        item.description,
                                                        item.explanation,
                                                        item.points.toLong(),
                                                        item.pointsToPass.toLong()
                                                    )!!
                                                )
                                                getAllQuestionsAsClasses(questionDataList)
                                            }},
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                }
                                if (searchBar.isEmpty() && tagFilterList.isEmpty()) {
                                    expandableItem(
                                        question = item,
                                        action = { runBlocking {
                                            questionData.deleteQuestionById(
                                                questionData.getQuestionId(
                                                    item.description,
                                                    item.explanation,
                                                    item.points.toLong(),
                                                    item.pointsToPass.toLong()
                                                )!!
                                            )
                                            getAllQuestionsAsClasses(questionDataList)
                                        }},
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }

    }
}
