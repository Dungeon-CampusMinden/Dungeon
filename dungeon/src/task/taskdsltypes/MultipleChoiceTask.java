package task.taskdsltypes;

import semanticanalysis.types.DSLExtensionMethod;
import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import semanticanalysis.types.IDSLExtensionMethod;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.MultipleChoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/** Typeadapter for creation of {@link MultipleChoice} instances via dsl. */
public class MultipleChoiceTask {
    @DSLTypeAdapter(name = "multiple_choice_task")
    public static MultipleChoice buildQuizFromMultipleChoiceTask(
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
            @DSLTypeMember(name = "correct_answer_index") List<Integer> correctAnswerIndices,
            @DSLTypeMember(name="grading_function") BiFunction<Task, Set<TaskContent>, Float> gradingFunction
            //  scoreFunction
            ) {
        MultipleChoice mc = new MultipleChoice(description);

        for (Quiz.Content answer : answers) {
            mc.addAnswer(answer);
        }

        for (var index : correctAnswerIndices) {
            mc.addCorrectAnswerIndex(index);
        }
        mc.scoringFunction(gradingFunction);

        return mc;
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

    /**
     * {@link IDSLExtensionMethod} to get the stored {@link TaskContent} of a {@link MultipleChoice}
     * instance
     */
    @DSLExtensionMethod(name = "get_content", extendedType = MultipleChoice.class)
    public static class GetContentMethod
            implements IDSLExtensionMethod<MultipleChoice, List<TaskContent>> {
        public static MultipleChoiceTask.GetContentMethod instance =
                new MultipleChoiceTask.GetContentMethod();

        @Override
        public List<TaskContent> call(MultipleChoice instance, List<Object> params) {
            // This has to return an ArrayList. Calling the `.toList()`-Method on the result of
            // the `map()`-call bellow will create an
            // `java.util.ImmutableCollections$ListN`-instance,
            // for which the TypeBuilder cannot create a corresponding dsl-type.
            List<TaskContent> returnList = new ArrayList<>();
            instance.contentStream().forEach(returnList::add);
            return returnList;
        }

        @Override
        public List<Class<?>> getParameterTypes() {
            var arr = new Class<?>[] {};
            return Arrays.stream(arr).toList();
        }
    }
}
