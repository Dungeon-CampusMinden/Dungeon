package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Project
import db.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProjectDataSourceImpl(db: Database): ProjectDataSource {
    private val queries = db.projectQueries
    override suspend fun getProjectById(id: Long): Project? {
        return withContext(Dispatchers.IO){
            queries.getProjectById(id).executeAsOneOrNull()
        }
    }

    override fun getAllProjects(): Flow<List<Project>> {
        return queries.getAllProjects().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun getProjectId(name: String): Long? {
        return withContext(Dispatchers.IO){
            queries.getProjectId(name).executeAsOneOrNull()
        }
    }

    override suspend fun insertProject(name: String, id: Long?) {
        return withContext(Dispatchers.IO){
            queries.insertProject(id,name)
        }
    }

    override suspend fun deleteProjectById(id: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteProjectById(id)
        }
    }
}