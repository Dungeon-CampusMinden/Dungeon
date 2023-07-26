package dslToGame.taskdsltypes;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import task.Task;
import task.quizquestion.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@DSLType
public class MultipleChoiceTask {
    @DSLTypeMember
    String description;
    @DSLTypeMember
    ArrayList<Quiz.Content> answers;
    @DSLTypeMember
    List<Integer> correctAnswerIndices;
    @DSLTypeMember
    BiFunction<SingleChoiceTask, List<Quiz.Content>, Float> scoreFunction;

    @DSLTypeAdapter(createPseudoDSLType = false)
    public static Task buildQuizFromMultipleChoiceTask(
        @DSLTypeMember(name = "multipleChoiceTask") MultipleChoiceTask task) {
        Quiz quiz = new Quiz(Quiz.Type.MULTIPLE_CHOICE, task.description);

        for (Quiz.Content answer : task.answers) {
            quiz.addAnswer(answer);
        }

        return quiz;
    }
}
