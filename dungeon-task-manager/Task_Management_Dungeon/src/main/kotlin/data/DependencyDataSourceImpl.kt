package data

import Task_Management_Dungeon.Database
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import db.Dependency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DependencyDataSourceImpl(db: Database): DependencyDataSource {
    private val queries = db.dependencyQueries
    override suspend fun getDependencyById(id: Long): Dependency? {
        return withContext(Dispatchers.IO){
            queries.getDependencyById(id).executeAsOneOrNull()
        }
    }

    override fun getAllDependenciesByProjectId(projectId: Long): Flow<List<Dependency>> {
        return queries.getAllDependenciesByProjectId(projectId).asFlow().mapToList(Dispatchers.IO)
    }
    
    override suspend fun insertDependency(
        questionAId: Long,
        questionBId: Long,
        projectId: Long,
        position: Long,
        dependency: String,
        id: Long?
    ) {
        return withContext(Dispatchers.IO){
            queries.insertDependency(id,questionAId,questionBId,projectId,position,dependency)
        }
    }

    override suspend fun deleteDependencyById(id: Long) {
        return withContext(Dispatchers.IO){
            queries.deleteDependencyById(id)
        }
    }
}