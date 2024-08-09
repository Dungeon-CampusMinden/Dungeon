package classes

import androidx.compose.runtime.mutableStateListOf

open class Question(
    val id: Long = 0,
    var description: String = "",
    var points: Int = 0,
    var pointsToPass: Int = 0,
    var explanation: String = "",
    val tags: List<String> = mutableStateListOf(),
    val type : QuestionType
) {
}