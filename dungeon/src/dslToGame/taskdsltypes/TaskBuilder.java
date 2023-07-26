package dslToGame.taskdsltypes;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import task.Task;
import task.quizquestion.Quiz;

/*public class TaskBuilder {
    @DSLTypeAdapter
    public static Task buildQuizFromSingleChoiceTask(
        @DSLTypeMember(name = "taskObject") SingleChoiceTask task) {
        Quiz quiz = new Quiz(Quiz.Type.SINGLE_CHOICE, task.description);

        for (Quiz.Content answer : task.answers) {
            quiz.addAnswer(answer);
        }

        return quiz;
    }

    @DSLTypeAdapter
    public static Task buildQuizFromMultpleChoiceTask(
        @DSLTypeMember(name = "taskObject") MultipleChoiceTask task) {
        Quiz quiz = new Quiz(Quiz.Type.SINGLE_CHOICE, task.description);

        for (Quiz.Content answer : task.answers) {
            quiz.addAnswer(answer);
        }

        return quiz;
    }
}
*/
