package reporting;

import task.Task;
import task.TaskContent;
import task.quizquestion.SingleChoice;

import java.util.Set;
import java.util.function.BiFunction;

/** Contanins different grading functions for the different task types. */
public class GradingFunctions {

    /**
     * A simple grading function for {@link SingleChoice} Task.
     *
     * <p>The function will check if the given answer is the correct answer, and if so, it will
     * return the full amount of points; otherwise, it will return 0 points.
     *
     * @return BiFunction that can be used for {@link Task#scoringFunction(BiFunction)}
     */
    public static BiFunction<Task, Set<TaskContent>, Float> singleChoiceGrading() {
        return new BiFunction<Task, Set<TaskContent>, Float>() {
            @Override
            public Float apply(Task task, Set<TaskContent> contents) {
                SingleChoice singleChoice = (SingleChoice) task;
                int index = singleChoice.correctAnswerIndices().get(0);
                TaskContent correctAnswer = singleChoice.contentByIndex(index);
                TaskContent givenAnswer =
                        contents.stream()
                                .findFirst()
                                .orElseThrow(
                                        () -> new IllegalArgumentException("No answer was given"));
                if (correctAnswer == givenAnswer) return singleChoice.points();
                else return 0f;
            }
        };
    }
}
