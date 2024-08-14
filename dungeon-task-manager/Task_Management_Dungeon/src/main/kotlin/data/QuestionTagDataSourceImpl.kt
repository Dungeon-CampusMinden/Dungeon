package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuestionTagDataSourceImpl(db : Database): QuestionTagDataSource {
    private val queries = db.questionTagQueries
    override fun getTagsByQuestionId(questionId: Long): Flow<List<String>> {
        return queries.getTagsByQuestionId(questionId).asFlow().mapToList(Dispatchers.IO)
    }


    override suspend fun insertQuestionTag(questionId: Long, tagId: Long) {
        return withContext(Dispatchers.IO){
            queries.insertQuestionTag(questionID = questionId, tagID =  tagId )
        }
    }

    override suspend fun deleteQuestionTag(questionId: Long, tagId: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteQuestionTag(questionId = questionId, tagId = tagId )
        }
    }
}