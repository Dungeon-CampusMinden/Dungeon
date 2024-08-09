package data

import db.Question
import kotlinx.coroutines.flow.Flow

interface QuestionDataSource {

    suspend fun getQuestionById(id: Long): Question?

    fun getAllQuestions(): Flow<List<Question>>

    suspend fun getQuestionId(description: String,explanation: String,points: Long,pointsToPass: Long): Long?
    suspend fun deleteQuestionById(id: Long)

    suspend fun insertQuestion(description: String, explanation: String, points: Long, pointsToPass: Long, type: String, id: Long? = null)

}