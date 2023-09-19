package task.taskdsltypes;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.SingleChoice;

import java.util.List;
import java.util.Set;

public class SingleChoiceTask {

    @DSLTypeAdapter(name = "single_choice_task")
    public static Task buildQuizFromSingleChoiceTask(
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
            @DSLTypeMember(name = "correct_answer_index") int correctAnswerIndex // ,
            // TODO: siehe https://github.com/Programmiermethoden/Dungeon/issues/916
            //  @DSLTypeMember(name="score_function") BiFunction<Task, Set<Quiz.Content>, Float>
            //  scoreFunction
            ) {
        Quiz quiz = new SingleChoice(description);

        for (Quiz.Content answer : answers) {
            quiz.addAnswer(answer);
        }

        quiz.addCorrectAnswerIndex(correctAnswerIndex);

        quiz.scoringFunction(SingleChoiceTask::score);

        return quiz;
    }

    static Float score(Task t, Set<TaskContent> answers) {
        Quiz quiz = (Quiz) t;
        int correctAnswerIndex = quiz.correctAnswerIndices().get(0);
        if (answers.size() != 1) {
            return 0.0f;
        }
        var contents = t.contentStream().toList();
        if (contents.size() <= correctAnswerIndex) {
            return 0.0f;
        }
        TaskContent givenAnswer = answers.stream().toList().get(0);
        TaskContent correctAnswer = contents.get(correctAnswerIndex);
        if (givenAnswer.equals(correctAnswer)) {
            return 1.0f;
        } else {
            return 0.0f;
        }
    }
}
