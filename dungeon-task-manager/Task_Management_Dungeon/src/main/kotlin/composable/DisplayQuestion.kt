package composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import classes.AssignQuestion
import classes.MultipleChoiceQuestion
import classes.SingleChoiceQuestion

/**
 * Composable Function to display an overview of the question
 * @param question Single choice question to display
 * @param modifier Modifier to manipulate the composable
 * @param showQuestion defines if the question.description is shown
 */
@Composable
fun QuestionDisplay(question: SingleChoiceQuestion, modifier: Modifier = Modifier, showQuestion : Boolean= true){
    Box (modifier.fillMaxSize().clip(shape = RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSecondary)){
        Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
            if(showQuestion){
                Row {
                    bodyText("Frage: ", modifier =  Modifier.weight(1f))
                    bodyText(question.description, modifier = Modifier.weight(4f))
                }
                Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            }
            Row {
                bodyText("Antworten:", modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(4f)) {
                    question.answers.forEachIndexed { index, answer ->
                        Row {
                            bodyText("Antwort ${index+1}: ")
                            bodyText(answer)
                        }
                    }
                }
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Korrekte Antwort:", modifier = Modifier.weight(1f))
                bodyText("Antwort ${(question.correctAnswerIndex + 1)}", modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Erklärung:", modifier =  Modifier.weight(1f))
                bodyText(question.explanation, modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Punkte:", modifier = Modifier.weight(1f))
                bodyText("${question.points} (Punkte zum Bestehen: ${question.pointsToPass})", modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Tags:", 20, Modifier.weight(1f))
                if(question.tags.isNotEmpty()){
                    Column(Modifier.weight(4f)) {
                        question.tags.forEach { tag ->
                            bodyText(tag)
                        }
                    }
                }else{
                    bodyText("Keine Tags vorhanden", modifier = Modifier.weight(4f))
                }
            }
        }
    }
}

/**
 * Composable Function to display an overview of the question
 * @param question multiple choice question to display
 * @param modifier Modifier to manipulate the composable
 * @param showQuestion defines if the question.description is shown
 */
@Composable
fun QuestionDisplay(question: MultipleChoiceQuestion, modifier: Modifier = Modifier, showQuestion : Boolean= true){
    Box (modifier.fillMaxSize().clip(shape = RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSecondary)){
        Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
            if(showQuestion){
                Row {
                    bodyText("Frage: ", modifier =  Modifier.weight(1f))
                    bodyText(question.description, modifier = Modifier.weight(4f))
                }
                Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            }
            Row {
                bodyText("Antworten:", modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(4f)) {
                    question.answers.forEachIndexed { index, answer ->
                        Row {
                            bodyText("Antwort ${index+1}: ")
                            bodyText(answer)
                        }
                    }
                }
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Korrekte Antworten:", modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(4f)) {
                    question.correctAnswerIndices.forEach{  index ->
                        bodyText("Antwort ${index+1} ")
                    }
                }
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Erklärung:", modifier =  Modifier.weight(1f))
                bodyText(question.explanation, modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Punkte:", modifier = Modifier.weight(1f))
                bodyText("${question.points} (Punkte zum Bestehen: ${question.pointsToPass})", modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Tags:", 20, Modifier.weight(1f))
                if(question.tags.isNotEmpty()){
                    Column(Modifier.weight(4f)) {
                        question.tags.forEach { tag ->
                            bodyText(tag)
                        }
                    }
                }else{
                    bodyText("Keine Tags vorhanden", modifier = Modifier.weight(4f))
                }
            }
        }
    }
}/**
 * Composable Function to display an overview of the question
 * @param question Assign question to display
 * @param modifier Modifier to manipulate the composable
 * @param showQuestion defines if the question.description is shown
 */


@Composable
fun QuestionDisplay(question: AssignQuestion, modifier: Modifier = Modifier, showQuestion : Boolean= true){
    Box (modifier.fillMaxSize().clip(shape = RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSecondary)){
        Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
            if(showQuestion){
                Row {
                    bodyText("Frage: ", modifier =  Modifier.weight(1f))
                    bodyText(question.description, modifier = Modifier.weight(4f))
                }
                Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Antworten:", modifier = Modifier.weight(1f))
                Column(modifier = Modifier.weight(4f)) {
                    question.assignments.forEachIndexed { index, assignment ->
                        Row {
                            bodyText("Lösung ${index+1}: ")
                            bodyText("A: ${assignment.termA}, B: ${assignment.termB}")
                        }
                    }
                }
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Erklärung:", modifier =  Modifier.weight(1f))
                bodyText(question.explanation, modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Punkte:", modifier = Modifier.weight(1f))
                bodyText("${question.points} (Punkte zum Bestehen: ${question.pointsToPass})", modifier = Modifier.weight(4f))
            }
            Divider (color = MaterialTheme.colorScheme.background, modifier = Modifier.height(2.dp).fillMaxWidth())
            Row {
                bodyText("Tags:", 20, Modifier.weight(1f))
                if(question.tags.isNotEmpty()){
                    Column(Modifier.weight(4f)) {
                        question.tags.forEach { tag ->
                            bodyText(tag)
                        }
                    }
                }else{
                    bodyText("Keine Tags vorhanden", modifier = Modifier.weight(4f))
                }
            }
        }
    }
}