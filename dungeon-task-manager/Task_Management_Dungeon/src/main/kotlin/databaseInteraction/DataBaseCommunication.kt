package databaseInteraction

import classes.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

/**
 * Functions to interact with the database
 */
object DataBaseCommunication {
    /**
     * returns answers from the db
     * @param questionId questionID to get the answers for
     */
    private suspend fun getAnswersToQuestionId(questionId: Long): List<String> {
        val answerData = Provider.provideAnswerDataSource(Driver.createDriver())
        val answerDataList = answerData.getAnswersByQuestionId(questionId).firstOrNull()
        val answers = mutableListOf<String>()
        answerDataList!!.forEach{answer ->
            answers.add(answer.answer)
        }
        return answers
    }

    /**
     * return the correct answers to a question
     * @param questionId ID of the question
     */
    private suspend fun getCorrectAnswersByQuestionId(questionId: Long): List<String> {
        val answerData = Provider.provideAnswerDataSource(Driver.createDriver())
        val answerList = answerData.getCorrectAnswersByQuestionId(questionId).firstOrNull()
        val answers = mutableListOf<String>()
        answerList!!.forEach{answer ->
            answers.add(answer.answer)
        }
        return answers
    }

    /**
     * returns the assignments to a question
     * @param questionId ID of the question
     */
     private fun getAssignmentsToQuestionId(questionId: Long): List<Assignment> {
         val assignmentData = Provider.provideAssignmentDataSource(Driver.createDriver())
         val assignmentList = runBlocking { assignmentData.getAssignmentsByQuestionId(questionId).firstOrNull() }
         val assignments = mutableListOf<Assignment>()
         assignmentList?.forEach{
             assignments.add(Assignment(it.termA!!,it.termB!!))
         }
         return assignments
    }

    /**
     * returns a list of questions from a dependency (removes doubles)
     * @param dependencies List of dependencies
     */
    fun getQuestionsFromDependencyList(dependencies:List<db.Dependency>?): List<Question>{
        val questions = mutableListOf<Question>()
        val questionData = runBlocking {  Provider.provideQuestionDataSource(Driver.createDriver()) }
        val addedId = mutableListOf<Long>()
        dependencies?.forEach{ dep ->
            runBlocking {
                if(!addedId.contains(dep.questionAID)){
                    questions.add(getQuestionAsClass(questionData.getQuestionById(dep.questionAID)!!)!!)
                    addedId.add(dep.questionAID)
                }
                if(!addedId.contains(dep.questionBID)){
                    questions.add(getQuestionAsClass(questionData.getQuestionById(dep.questionBID)!!)!!)
                    addedId.add(dep.questionBID)
                }
            }
        }
        return questions
    }

    /**
     * transforms a question from the db to a question class
     * @param question question to transform
     */
    suspend fun getQuestionAsClass(question: db.Question): Question? {
        when (question.type) {
            "SINGLE_CHOICE_QUESTION" -> {
                val answers = getAnswersToQuestionId(question.id)
                val correctAnswers = getCorrectAnswersByQuestionId(question.id)
                val correctAnswerIndices= mutableListOf<Int> ()
                answers.forEachIndexed { index, answer ->
                    if (correctAnswers.contains(answer)) {
                        correctAnswerIndices.add(index)
                    }
                }
                return SingleChoiceQuestion(
                    id = question.id,
                    description = question.description,
                    explanation = question.explanation,
                    points = question.points.toInt(),
                    pointsToPass = question.pointsToPass.toInt(),
                    answers = answers,
                    correctAnswerIndex =correctAnswerIndices[0]
                )
            }

            "MULTIPLE_CHOICE_QUESTION" -> {
                val answers = getAnswersToQuestionId(question.id)
                val correctAnswers = getCorrectAnswersByQuestionId(question.id)
                val correctAnswerIndices= mutableListOf<Int> ()
                answers.forEachIndexed { index, answer ->
                    if (correctAnswers.contains(answer)) {
                        correctAnswerIndices.add(index)
                    }
                }
                return MultipleChoiceQuestion(
                    id = question.id,
                    description = question.description,
                    explanation = question.explanation,
                    points = question.points.toInt(),
                    pointsToPass = question.pointsToPass.toInt(),
                    answers = answers,
                    correctAnswerIndices =correctAnswerIndices
                )
            }

            "ASSIGN_QUESTION" -> {
                return AssignQuestion(
                    id = question.id,
                    description = question.description,
                    explanation = question.explanation,
                    points = question.points.toInt(),
                    pointsToPass = question.pointsToPass.toInt(),
                    assignments = getAssignmentsToQuestionId(questionId = question.id)
                )
            }

        }
        return null
    }
}