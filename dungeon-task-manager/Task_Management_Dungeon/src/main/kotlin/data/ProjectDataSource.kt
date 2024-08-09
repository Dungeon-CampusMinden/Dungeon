package data

import db.Project
import db.Question
import kotlinx.coroutines.flow.Flow

interface ProjectDataSource {

    suspend fun getProjectById(id: Long): Project?

    fun getAllProjects(): Flow<List<Project>>

    suspend fun getProjectId(name : String): Long?

    suspend fun insertProject(name: String, id: Long? = null)

    suspend fun deleteProjectById(id: Long)

}