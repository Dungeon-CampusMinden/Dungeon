package databaseInteraction

import Task_Management_Dungeon.Database
import app.cash.sqldelight.db.SqlDriver
import data.*
/**
 * Provides data Sources to interact with the Database
 */
object Provider {

    fun provideQuestionDataSource(driver : SqlDriver): QuestionDataSource {
        return QuestionDataSourceImpl(Database(driver))
    }

    fun provideAnswerDataSource(driver : SqlDriver): AnswerDataSource {
        return AnswerDataSourceImpl(Database(driver))
    }

    fun provideAssignmentDataSource(driver : SqlDriver): AssignmentDataSource {
        return AssignmentDataSourceImpl(Database(driver))
    }
    fun provideCorrectAnswerDataSource(driver : SqlDriver): CorrectAnswerDataSource {
        return CorrectAnswerDataSourceImpl(Database(driver))
    }
    fun provideCorrectAssignmentDataSource(driver : SqlDriver): CorrectAssignmentDataSource{
        return CorrectAssignmentDataSourceImpl(Database(driver))
    }
    fun provideDependencyDataSource(driver : SqlDriver): DependencyDataSource {
        return DependencyDataSourceImpl(Database(driver))
    }
    fun provideProjectQuestionDataSource(driver : SqlDriver): ProjectQuestionDataSource {
        return ProjectQuestionDataSourceImpl(Database(driver))
    }
    fun provideProjectDataSource(driver : SqlDriver): ProjectDataSource {
        return ProjectDataSourceImpl(Database(driver))
    }
    fun provideQuestionTagDataSource(driver : SqlDriver): QuestionTagDataSource {
        return QuestionTagDataSourceImpl(Database(driver))
    }
    fun provideTagDataSource(driver : SqlDriver): TagDataSource {
        return TagDataSourceImpl(Database(driver))
    }


}