package data

import Task_Management_Dungeon.Database
import db.Answer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CorrectAnswerDataSourceImpl(db:Database): CorrectAnswerDataSource {
    private val queries = db.correctAnswerQueries
    override suspend fun getCorrectAnswerByQuestionId(questionId: Long): Answer? {
       /* return withContext(Dispatchers.IO){
            queries.getCorrectAnswerByQuestionId(questionId).executeAsOneOrNull()
        }*/
        TODO()
    }

    override suspend fun insertCorrectAnswer(questionId: Long, answerId: Long) {
        return withContext(Dispatchers.IO){
            queries.insertCorrectAnswer(questionID = questionId, answerID = answerId)
        }
    }

    override suspend fun deleteCorrectAnswerByAnswerId(answerId: Long, questinId: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteCorrectAnswerByAnswerId(answerId = answerId, questionId = questinId)
        }
    }
}