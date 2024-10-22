package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TagDataSourceImpl(db : Database): TagDataSource {
    private val queries = db.tagQueries
    override suspend fun getTagById(id: Long): Tag? {
        return withContext(Dispatchers.IO){
            queries.getTagById(id).executeAsOneOrNull()
        }
    }

    override fun getTagsByQuestionId(questionId: Long): Flow<List<String>> {
        return queries.getTagsByQuestionId(questionId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun getTagByName(name: String): Long? {
        return withContext(Dispatchers.IO){
            queries.getTagByName(name).executeAsOneOrNull()
        }
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return queries.getAllTags().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertTag(tag: String, id: Long?) {
        return withContext(Dispatchers.IO){
            queries.insertTag(tag = tag, id = id )
        }
    }

    override suspend fun deleteTagById(id: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteTagById(id)
        }
    }
}