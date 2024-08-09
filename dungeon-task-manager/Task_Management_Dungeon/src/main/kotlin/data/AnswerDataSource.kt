package data

import db.Answer
import kotlinx.coroutines.flow.Flow

interface AnswerDataSource {

    suspend fun getAnswerById(id: Long): Answer?

    suspend fun getAnswerId(questionId: Long,answer: String): Long?

    fun getAnswersByQuestionId(id: Long): Flow<List<Answer>>

    fun getCorrectAnswersByQuestionId(questionId: Long): Flow<List<Answer>>

    suspend fun setCorrectAnswer(id: Long)

    suspend fun insertAnswer(questionId: Long, answer: String, id: Long? = null)

    suspend fun deleteAnswerById(id: Long)
}