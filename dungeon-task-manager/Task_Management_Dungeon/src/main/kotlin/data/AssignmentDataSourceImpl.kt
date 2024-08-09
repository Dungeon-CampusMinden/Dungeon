package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Assignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssignmentDataSourceImpl(db:Database): AssignmentDataSource {
    private val queries = db.assignmentQueries
    override suspend fun getAssignmentById(id: Long): Assignment? {
        return withContext(Dispatchers.IO){
            queries.getAssignmentById(id).executeAsOneOrNull()
        }
    }

    override suspend fun getAssignmentId(questionId: Long, termA: String, termB: String): Long? {
        return withContext(Dispatchers.IO){
            queries.getAssignmentId(questionId, termB = termB, termA = termA ).executeAsOneOrNull()
        }
    }

    override fun getAssignmentsByQuestionId(id: Long): Flow<List<Assignment>> {
        return queries.getAssignmentByQuestionId(id).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertAssignment(questionId: Long, termA: String, termB: String, id: Long?) {
        return withContext(Dispatchers.IO){
            queries.insertAssignment(questionID = questionId, termA = termA, termB = termB, id = id)
        }
    }

    override suspend fun deleteAssignmentById(id: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteAssignmentById(id)
        }
    }
}