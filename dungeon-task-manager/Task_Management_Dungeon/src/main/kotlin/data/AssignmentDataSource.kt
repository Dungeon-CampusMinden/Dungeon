package data

import db.Assignment
import kotlinx.coroutines.flow.Flow

interface AssignmentDataSource {
    suspend fun getAssignmentById(id: Long): Assignment?

    suspend fun getAssignmentId(questionId: Long,termA: String, termB: String): Long?

    fun getAssignmentsByQuestionId(id: Long): Flow<List<Assignment>>

    suspend fun insertAssignment(questionId: Long, termA: String, termB: String, id: Long? = null)

    suspend fun deleteAssignmentById(id: Long)
}