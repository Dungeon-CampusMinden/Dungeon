package classes

/**
 * Assignments use a Term and a Definition as solutions for assign tasks
 * @param termA term A of the assignment. If there is no solution to it, leave it as default
 * @param termB term B of the assignment. If there is no solution to it, leave it as default
 */
data class Assignment(
    val termA: String = "_",
    val termB: String = "_"
)