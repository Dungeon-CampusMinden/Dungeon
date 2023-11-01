package task.taskdsltypes;

import semanticanalysis.types.DSLExtensionMethod;
import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import semanticanalysis.types.IDSLExtensionMethod;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.SingleChoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/** Typeadapter for the creation of {@link SingleChoice} instances via dsl. */
public class SingleChoiceTask {

    @DSLTypeAdapter(name = "single_choice_task")
    public static SingleChoice buildQuizFromSingleChoiceTask(
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
            @DSLTypeMember(name = "correct_answer_index") int correctAnswerIndex,
            @DSLTypeMember(name = "grading_function")
                    BiFunction<Task, Set<TaskContent>, Float> gradingFunction) {
        SingleChoice sc = new SingleChoice(description);

        for (Quiz.Content answer : answers) {
            sc.addAnswer(answer);
        }

        sc.addCorrectAnswerIndex(correctAnswerIndex);
        // sc.scoringFunction(SingleChoiceTask::score);
        sc.scoringFunction(gradingFunction);

        return sc;
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

    /**
     * {@link IDSLExtensionMethod} to get the stored {@link TaskContent} of a {@link SingleChoice}
     * instance
     */
    @DSLExtensionMethod(name = "get_content", extendedType = SingleChoice.class)
    public static class GetContentMethod
            implements IDSLExtensionMethod<SingleChoice, List<TaskContent>> {
        public static GetContentMethod instance = new GetContentMethod();

        @Override
        public List<TaskContent> call(SingleChoice instance, List<Object> params) {
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
