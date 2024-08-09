package data

import db.Answer

interface CorrectAnswerDataSource {
    suspend fun getCorrectAnswerByQuestionId(questionId: Long): Answer?

    suspend fun insertCorrectAnswer(questionId: Long, answerId:Long)

    suspend fun deleteCorrectAnswerByAnswerId(answerId: Long, questinId: Long)

}