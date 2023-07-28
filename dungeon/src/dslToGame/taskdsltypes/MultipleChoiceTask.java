package dslToGame.taskdsltypes;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import task.Task;
import task.TaskContent;
import task.quizquestion.Quiz;

import java.util.List;
import java.util.Set;

public class MultipleChoiceTask {
    @DSLTypeAdapter(name = "multiple_choice_task")
    public static Task buildQuizFromMultipleChoiceTask(
        @DSLTypeMember(name="description") String description,
        @DSLTypeMember(name="answers") List<Quiz.Content> answers,
        @DSLTypeMember(name="correct_answer_index") List<Integer> correctAnswerIndices//,
        // TODO: in order to use scoring functions as intended at the current implementation status,
        //  we need to somehow convert the specific function-type (from the DSL-definition, which expects a
        //  SingleChoiceTask-type) to a generic function-type (which accepts the Task and Set<TaskContent>)
        //@DSLTypeMember(name="score_function") BiFunction<Task, Set<Quiz.Content>, Float> scoreFunction
    ) {
        Quiz quiz = new Quiz(Quiz.Type.MULTIPLE_CHOICE, description);

        for (Quiz.Content answer : answers) {
            quiz.addAnswer(answer);
        }

        for (var index : correctAnswerIndices) {
            quiz.addCorrectAnswerIndex(index);
        }

        quiz.scoringFunction(MultipleChoiceTask::score);

        return quiz;
    }

    static Float score(Task t, Set<TaskContent> answers) {
        Quiz quiz = (Quiz)t;
        List<Integer> correctAnswerIndices = quiz.correctAnswerIndices();

        int totalCorrectAnswers = correctAnswerIndices.size();
        int givenCorrectAnswers = 0;
        for (int answerIndex : correctAnswerIndices) {
            TaskContent correctAnswer = t.contentStream().toList().get(answerIndex);
            if (answers.contains(correctAnswer)) {
                givenCorrectAnswers++;
            }
        }
        return (float)givenCorrectAnswers/(float)totalCorrectAnswers;
    }
}
