package dslToGame.taskdsltypes;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import task.Task;
import task.TaskContent;
import task.quizquestion.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class SingleChoiceTask {
    @DSLTypeAdapter(name = "single_choice_task")
    public static Task buildQuizFromSingleChoiceTask(
        @DSLTypeMember(name="description") String description,
        @DSLTypeMember(name="answers") List<Quiz.Content> answers,
        @DSLTypeMember(name="correct_answer_index") int correctAnswerIndex//,
        // TODO: in order to use scoring functions as intended at the current implementation status,
        //  we need to somehow convert the specific function-type (from the DSL-definition, which expects a
        //  SingleChoiceTask-type) to a generic function-type (which accepts the Task and Set<TaskContent>)
        //@DSLTypeMember(name="score_function") BiFunction<Task, Set<Quiz.Content>, Float> scoreFunction
        ) {
        Quiz quiz = new Quiz(Quiz.Type.SINGLE_CHOICE, description);

        for (Quiz.Content answer : answers) {
            quiz.addAnswer(answer);
        }

        quiz.addCorrectAnswerIndex(correctAnswerIndex);

        //quiz.scoringFunction(scoreFunction);

        return quiz;
    }
}

