package data

import Task_Management_Dungeon.Database
import db.Assignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CorrectAssignmentDataSourceImpl(db:Database): CorrectAssignmentDataSource {
    private val queries = db.correctAssignmentQueries
    override suspend fun getCorrectAssignmentByQuestionId(questionId: Long): Assignment? {
        /*return withContext(Dispatchers.IO){
            queries.getCorrectAssignmentByQuestionId(questionId).executeAsOneOrNull()
        }*/TODO()
    }

    override suspend fun insertCorrectAssignment(questionId: Long, assignmentId: Long) {
        return withContext(Dispatchers.IO){
            queries.insertCorrectAssignment(questionID = questionId, assignmentID =assignmentId)
        }
    }

    override suspend fun deleteCorrectAssignmentByAssignmentId(assignmentId: Long, questinId: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteCorrectAssignmentByAssignmentId(assignmentId = assignmentId, questionId = questinId)
        }
    }

}