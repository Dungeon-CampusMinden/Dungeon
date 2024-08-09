package classes

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

data class Project(
    val name: String,
) {
    var questions: List<Question> = mutableListOf()
    val dependencies = mutableStateListOf<Dependency>()

    fun addDependency(dependency: Dependency){
        dependencies.add(dependency)
    }

}