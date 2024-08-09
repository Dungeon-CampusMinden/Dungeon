package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Answer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AnswerDataSourceImpl(db: Database): AnswerDataSource {
    private val queries = db.answerQueries
    override suspend fun getAnswerById(id: Long): Answer? {
        return withContext(Dispatchers.IO){
            queries.getAnswerById(id).executeAsOneOrNull()
        }
    }

    override suspend fun getAnswerId(questionId: Long, answer: String): Long? {
        return withContext(Dispatchers.IO){
            queries.getAnswerId(questionId,answer).executeAsOneOrNull()
        }
    }

    override fun getAnswersByQuestionId(id: Long): Flow<List<Answer>> {
        return queries.getAnswersByQuestionId(id).asFlow().mapToList(Dispatchers.IO)
    }

    override fun getCorrectAnswersByQuestionId(questionId: Long): Flow<List<Answer>> {
        return queries.getCorrectAnswersByQuestionId(questionId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun setCorrectAnswer(id: Long) {
        return withContext(Dispatchers.IO){
            queries.setCorrectAnswer(id)
        }
    }

    override suspend fun insertAnswer(questionId: Long, answer: String, id: Long?) {
        return withContext(Dispatchers.IO){
            queries.insertAnswer(questionID = questionId, answer = answer, id = id, correct = 0)
        }
    }

    override suspend fun deleteAnswerById(id: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteAnswerById(id)
        }
    }
}