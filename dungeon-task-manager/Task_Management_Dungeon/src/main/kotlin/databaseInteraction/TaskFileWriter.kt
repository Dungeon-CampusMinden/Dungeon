package databaseInteraction

import classes.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File


/**
 * Writes a Taskfile
 */
object TaskFileWriter {

    /**
     * Writes a taskfile from a projectId
     * @param projectId ID of the project
     * @param filename name of the File
     */
    fun writeProjectToFile(projectId: Long, filename: String) {
        val projectData = Provider.provideProjectDataSource(Driver.createDriver())
        val dependencyData = Provider.provideDependencyDataSource(Driver.createDriver())
        val project = runBlocking { projectData.getProjectById(projectId) }
        val dependencies = runBlocking { dependencyData.getAllDependenciesByProjectId(projectId).firstOrNull() }
        // create Questionlist
        val questions = DataBaseCommunication.getQuestionsFromDependencyList(dependencies)
        File("$filename.dng").createNewFile()
        //ProjektAnleitung zum Schreiben
        try {
            File("$filename.dng").appendText("// $project")
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        }
        questions.forEach {
            writeQuestion(it, filename)
        }
        writeGraph(dependencies!!, filename, project!!.name)
        writeSzenarioDefinitions(filename)
    }

    private fun writeQuestion(question: Question, filename: String) {
        try {
            when (question) {
                is SingleChoiceQuestion -> {
                    File("$filename.dng").appendText(
                        "\nsingle_choice_task t${question.id} {" +
                                "\n\tdescription: \"${question.description}\"," +
                                "\n\texplanation: \"${question.explanation}\"," +
                                "\n\tpoints: ${question.points}," +
                                "\n\tpoints_to_pass: ${question.pointsToPass}," +
                                "\n\tanswers: ["
                    )
                    question.answers.forEachIndexed { index, answer ->
                        if (index == 0) {
                            File("$filename.dng").appendText("\n\t\"${answer.trim()}\"")
                        } else {
                            File("$filename.dng").appendText(",\n\t\"${answer.trim()}\"")
                        }
                    }
                    File("$filename.dng").appendText("],\n\tcorrect_answer_index: ${question.correctAnswerIndex}\n}\n")
                }

                is MultipleChoiceQuestion -> {
                    File("$filename.dng").appendText(
                        "\nmultiple_choice_task t${question.id} {" +
                                "\n\tdescription: \"${question.description}\"," +
                                "\n\texplanation: \"${question.explanation}\"," +
                                "\n\tpoints: ${question.points}," +
                                "\n\tpoints_to_pass: ${question.pointsToPass}," +
                                "\n\tanswers: ["
                    )
                    question.answers.forEachIndexed { index, answer ->
                        if (index == 0) {
                            File("$filename.dng").appendText("\n\t\"${answer.trim()}\"")
                        } else {
                            File("$filename.dng").appendText("\n\t\"${answer.trim()}\"")
                        }
                    }
                    File("$filename.dng").appendText("],\n\tcorrect_answer_index: [")
                    question.correctAnswerIndices.forEachIndexed { index, correctAnswer ->
                        if (index == 0) {
                            File("$filename.dng").appendText("$correctAnswer")
                        } else {
                            File("$filename.dng").appendText(", $correctAnswer")
                        }
                    }
                    File("$filename.dng").appendText("] \n}\n")
                }

                is AssignQuestion -> {
                    File("$filename.dng").appendText(
                        "\nassign_task t${question.id} {" +
                                "\n\tdescription: \"${question.description}\"," +
                                "\n\texplanation: \"${question.explanation}\"," +
                                "\n\tpoints: ${question.points}," +
                                "\n\tpoints_to_pass: ${question.pointsToPass}," +
                                "\n\tsolution: <"
                    )
                    question.assignments.forEachIndexed { index, assignment ->
                        if (index == 0) {
                            if (assignment.termA == "_") {
                                File("$filename.dng").appendText("\n\t\t[_, \"${assignment.termB.trim()}\"]")
                            } else {
                                if (assignment.termB == "_") {
                                    File("$filename.dng").appendText("\n\t\t[\"${assignment.termA.trim()}\", _]")
                                } else {
                                    File("$filename.dng").appendText("\n\t\t[\"${assignment.termA.trim()}\", \"${assignment.termB.trim()}\"]")
                                }
                            }
                        } else {
                            if (assignment.termA == "_") {
                                File("$filename.dng").appendText(",\n\t\t[_, \"${assignment.termB.trim()}\"]")
                            } else {
                                if (assignment.termB == "_") {
                                    File("$filename.dng").appendText(",\n\t\t[\"${assignment.termA.trim()}\", _]")
                                } else {
                                    File("$filename.dng").appendText(",\n\t\t[\"${assignment.termA.trim()}\", \"${assignment.termB.trim()}\"]")
                                }
                            }
                        }
                    }
                    File("$filename.dng").appendText("\n\t>\n}\n")
                }
            }
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        }

    }

    private fun writeGraph(dependencies: List<db.Dependency>, filename: String, projectName :String) {
        File("$filename.dng").appendText("\ngraph p${dependencies[0].projectID}_graph {")

        dependencies.forEach { dependency ->
            File("$filename.dng").appendText("\n\tt${dependency.questionAID} -> t${dependency.questionBID}")
            when (dependency.dependency) {
                "Sequenz" -> {
                    File("$filename.dng").appendText(" [type=seq];")
                }

                "Pflicht Unteraufgabe" -> {
                    File("$filename.dng").appendText(" [type=st_m];")
                }

                "Optionale Unteraufgabe" -> {
                    File("$filename.dng").appendText(" [type=st_o];")
                }

                "Bei falscher Antwort" -> {
                    File("$filename.dng").appendText(" [type=c_f];")
                }

                "Bei richtiger Antwort" -> {
                    File("$filename.dng").appendText(" [type=c_c];")
                }
            }
        }
        File("$filename.dng").appendText(
            "\n}\n\ndungeon_config $projectName {" +
                    "\n\t\tdependency_graph:  p${dependencies[0].projectID}_graph" +
                    "\n}\n\n"
        )
    }

    private fun writeSzenarioDefinitions(filename: String){
        File("szenario_definitions").forEachLine { line ->
            File("$filename.dng").appendText( line +"\n")
        }
    }
}
