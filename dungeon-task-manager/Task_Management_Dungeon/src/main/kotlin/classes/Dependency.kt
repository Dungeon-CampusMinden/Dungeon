package classes

class Dependency(
    val projectId: Long

) {
    var questionA: Question? = null
        get() = field
        set(value) {
            field = value
        }
    var questionB: Question? = null
        get() = field
        set(value) {
            field = value
        }
    var dependency: String = ""
        get() = field
        set(value) {
            field = value
        }

}