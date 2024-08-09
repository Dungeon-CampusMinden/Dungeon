package classes

import androidx.compose.runtime.mutableStateListOf

class SingleChoiceQuestion(
    id: Long = 0,
    description: String = "",
    points: Int = 0,
    pointsToPass: Int = 0,
    explanation: String = "",
    val answers: List<String> = mutableStateListOf(),
    tags: List<String> = mutableStateListOf(),
    var correctAnswerIndex: Int = -1
) : Question(
    id= id,
    description = description,
    points = points,
    pointsToPass = pointsToPass,
    explanation = explanation,
    tags = tags,
    type = QuestionType.SingleChoice
) {

}