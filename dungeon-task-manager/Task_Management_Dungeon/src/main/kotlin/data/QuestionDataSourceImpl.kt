package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Question
import db.QuestionQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuestionDataSourceImpl(db : Database) : QuestionDataSource {
    private val queries = db.questionQueries
    override suspend fun getQuestionById(id: Long): Question? {
        return withContext(Dispatchers.IO){
            queries.getQuestionById(id).executeAsOneOrNull()
        }
    }

    override fun getAllQuestions(): Flow<List<Question>> {
        return queries.getAllQuestions().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun getQuestionId(description: String, explanation: String, points: Long, pointsToPass: Long): Long? {
        return withContext(Dispatchers.IO){
            queries.getQuestionId(description,explanation,points,pointsToPass).executeAsOneOrNull()
        }

    }


    override suspend fun deleteQuestionById(id: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteQuestionById(id)
        }
    }

    override suspend fun insertQuestion(
        description: String,
        explanation: String,
        points: Long,
        pointsToPass: Long,
        type: String,
        id: Long?
    ) {
        return withContext(Dispatchers.IO){
            queries.insertQuestion(id,description,explanation,points,pointsToPass,type)
        }

    }
}