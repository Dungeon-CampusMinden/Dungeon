package reporting;

import task.Task;
import task.TaskContent;
import task.quizquestion.MultipleChoice;
import task.quizquestion.SingleChoice;

import java.util.HashSet;
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
        return (task, answer) -> {
            SingleChoice singleChoice = (SingleChoice) task;
            int index = singleChoice.correctAnswerIndices().get(0);
            TaskContent correctAnswer = singleChoice.contentByIndex(index);
            TaskContent givenAnswer =
                    answer.stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No answer was given"));
            if (correctAnswer == givenAnswer) return singleChoice.points();
            else return 0f;
        };
    }

    /**
     * A simple grading function for the {@link MultipleChoice} Task.
     *
     * <p>For each correct answer, points will be added; for each wrong answer, points will be
     * removed.
     *
     * <p>The amount of points given/removed per answer is calculated by the number of points the
     * Task is worth divided by the count of correct answers.
     *
     * <p>The return value cannot be lower than 0.
     *
     * @return a BiFunction that can be used for {@link Task#scoringFunction(BiFunction)}
     */
    public static BiFunction<Task, Set<TaskContent>, Float> multipeChoiceGrading() {
        return (BiFunction<Task, Set<TaskContent>, Float>)
                (task, givenAnswers) -> {
                    MultipleChoice multipleChoice = (MultipleChoice) task;

                    Set<TaskContent> correctAnswers = new HashSet<>();
                    for (int index : multipleChoice.correctAnswerIndices())
                        correctAnswers.add(multipleChoice.contentByIndex(index));

                    float pointPerAnswer = multipleChoice.points() / correctAnswers.size();
                    float reachedPoints = 0f;
                    for (TaskContent answer : givenAnswers) {
                        if (correctAnswers.contains(answer)) reachedPoints += pointPerAnswer;
                        else reachedPoints -= pointPerAnswer;
                    }
                    return Math.max(0, reachedPoints);
                };
    }
}
