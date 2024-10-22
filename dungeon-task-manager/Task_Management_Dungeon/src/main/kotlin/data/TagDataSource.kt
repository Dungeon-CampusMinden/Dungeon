package data

import db.Question
import db.Tag
import kotlinx.coroutines.flow.Flow

interface TagDataSource {

    suspend fun getTagById(id: Long): Tag?

    fun getTagsByQuestionId(questionId: Long): Flow<List<String>>
    suspend fun getTagByName(name: String) : Long?

    fun getAllTags(): Flow<List<Tag>>

    suspend fun insertTag(tag: String, id: Long? = null)

    suspend fun deleteTagById(id: Long)
}