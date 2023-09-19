package task.taskdsltypes;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.MultipleChoice;

import java.util.List;
import java.util.Set;

public class MultipleChoiceTask {
    @DSLTypeAdapter(name = "multiple_choice_task")
    public static Task buildQuizFromMultipleChoiceTask(
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
            @DSLTypeMember(name = "correct_answer_index") List<Integer> correctAnswerIndices // ,
            // TODO: siehe https://github.com/Programmiermethoden/Dungeon/issues/916
            //  @DSLTypeMember(name="score_function") BiFunction<Task, Set<Quiz.Content>, Float>
            //  scoreFunction
            ) {
        Quiz quiz = new MultipleChoice(description);

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
        Quiz quiz = (Quiz) t;
        List<Integer> correctAnswerIndices = quiz.correctAnswerIndices();

        int totalCorrectAnswers = correctAnswerIndices.size();
        int givenCorrectAnswers = 0;
        var contents = t.contentStream().toList();
        for (int answerIndex : correctAnswerIndices) {
            if (contents.size() <= answerIndex) {
                continue;
            }
            TaskContent correctAnswer = contents.get(answerIndex);
            if (answers.contains(correctAnswer)) {
                givenCorrectAnswers++;
            }
        }
        return (float) givenCorrectAnswers / (float) totalCorrectAnswers;
    }
}
