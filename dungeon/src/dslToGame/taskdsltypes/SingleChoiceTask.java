package dslToGame.taskdsltypes;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import task.Task;
import task.quizquestion.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

//@DSLType
public class SingleChoiceTask {
    /*
    @DSLTypeMember String description;
    @DSLTypeMember ArrayList<Quiz.Content> answers;
    @DSLTypeMember int correctAnswerIndex;
    @DSLTypeMember BiFunction<SingleChoiceTask, List<Quiz.Content>, Float> scoreFunction;
    */

    @DSLTypeAdapter(name = "single_choice_task")
    public static Task buildQuizFromSingleChoiceTask(
        @DSLTypeMember(name="description") String description,
        @DSLTypeMember(name="answers") List<Quiz.Content> answers,
        @DSLTypeMember(name="correct_answer_index") int correctAnswerIndex,
        @DSLTypeMember(name="score_function") BiFunction<SingleChoiceTask, List<Quiz.Content>, Float> scoreFunction
        ) {
        Quiz quiz = new Quiz(Quiz.Type.SINGLE_CHOICE, description);

        for (Quiz.Content answer : answers) {
            quiz.addAnswer(answer);
        }

        return quiz;
    }
}

