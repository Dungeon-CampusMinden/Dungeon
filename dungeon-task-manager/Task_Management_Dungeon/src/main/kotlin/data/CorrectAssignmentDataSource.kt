package data

import db.Answer
import db.Assignment

interface CorrectAssignmentDataSource {
    suspend fun getCorrectAssignmentByQuestionId(questionId: Long): Assignment?

    suspend fun insertCorrectAssignment(questionId: Long, assignmentId:Long)

    suspend fun deleteCorrectAssignmentByAssignmentId(assignmentId: Long, questinId: Long)
}