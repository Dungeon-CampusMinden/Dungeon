package data

import db.Assignment
import db.Dependency
import kotlinx.coroutines.flow.Flow

interface DependencyDataSource {

    suspend fun getDependencyById(id: Long): Dependency?

    fun getAllDependenciesByProjectId(projectId: Long ): Flow<List<Dependency>>

    suspend fun insertDependency(questionAId: Long, questionBId: Long, projectId: Long, position: Long, dependency: String,id: Long? = null)

    suspend fun deleteDependencyById(id: Long)
}