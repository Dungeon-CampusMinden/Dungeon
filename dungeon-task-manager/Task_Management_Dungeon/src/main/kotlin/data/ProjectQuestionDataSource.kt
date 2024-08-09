package data

import db.Question
import kotlinx.coroutines.flow.Flow

interface ProjectQuestionDataSource {
    fun getAllQuestionsByProjectId(projectId: Long): Flow<List<Question>>


    suspend fun insertProjectQuestion(projectId: Long, questionId: Long)

    suspend fun deleteProjectQuestionByQuestionId(projectId: Long, questionId:Long)
}