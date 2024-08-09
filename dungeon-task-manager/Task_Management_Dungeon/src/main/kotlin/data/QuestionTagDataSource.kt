package data

import db.Question
import db.Tag
import kotlinx.coroutines.flow.Flow

interface QuestionTagDataSource {

    fun getTagsByQuestionId(questionId: Long): Flow<List<String>>

    suspend fun insertQuestionTag(questionId: Long, tagId: Long)

    suspend fun deleteQuestionTag(questionId: Long, tagId: Long)
}