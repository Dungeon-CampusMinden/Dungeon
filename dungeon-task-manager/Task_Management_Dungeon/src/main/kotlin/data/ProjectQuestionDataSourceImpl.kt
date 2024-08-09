package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class ProjectQuestionDataSourceImpl(db: Database): ProjectQuestionDataSource {
    private val queries = db.projectQuestionQueries
    override fun getAllQuestionsByProjectId(projectId: Long): Flow<List<Question>> {
        return queries.getAllQuestionsByProjectId(projectId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertProjectQuestion(projectId: Long, questionId: Long) {
        return withContext(Dispatchers.IO){
            queries.insertProjectQuestion(projectId,questionId)
        }
    }

    override suspend fun deleteProjectQuestionByQuestionId(projectId: Long, questionId: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteProjectQuestionByQuestionId(questionId, projectId)
        }
    }
}