package task.taskdsltypes;

import reporting.GradingFunctions;

import semanticanalysis.types.DSLExtensionMethod;
import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import semanticanalysis.types.IDSLExtensionMethod;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.MultipleChoice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

/** Typeadapter for creation of {@link MultipleChoice} instances via dsl. */
public class MultipleChoiceTask {
    @DSLTypeAdapter(name = "multiple_choice_task")
    public static MultipleChoice buildQuizFromMultipleChoiceTask(
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
            @DSLTypeMember(name = "correct_answer_index") List<Integer> correctAnswerIndices,
            @DSLTypeMember(name = "grading_function")
                    BiFunction<Task, Set<TaskContent>, Float> gradingFunction
            //  scoreFunction
            ) {
        MultipleChoice mc = new MultipleChoice(description);

        for (Quiz.Content answer : answers) {
            mc.addAnswer(answer);
        }

        for (var index : correctAnswerIndices) {
            mc.addCorrectAnswerIndex(index);
        }

        // default value
        mc.scoringFunction(
                Objects.requireNonNullElseGet(
                        gradingFunction, GradingFunctions::multipeChoiceGrading));

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
        public List<Type> getParameterTypes() {
            var arr = new Type[] {};
            return Arrays.stream(arr).toList();
        }
    }

    /**
     * {@link IDSLExtensionMethod} to set the grading function of a {@link MultipleChoice} instance.
     */
    @DSLExtensionMethod(name = "set_grading_function", extendedType = MultipleChoice.class)
    public static class MultipleChoiceSetGradingFunction
            implements IDSLExtensionMethod<MultipleChoice, Void> {
        public static MultipleChoiceTask.MultipleChoiceSetGradingFunction instance =
                new MultipleChoiceTask.MultipleChoiceSetGradingFunction();

        @Override
        public Void call(MultipleChoice instance, List<Object> params) {
            var func = (BiFunction<Task, Set<TaskContent>, Float>) params.get(0);
            instance.scoringFunction(func);
            return null;
        }

        // region parameterized parameter type declaration

        // The TypeBuilder needs an implementation of ParameterizedType (with the actual type
        // information)
        // to create a FunctionType for the method parameter. As this method will accept a
        // BiFunction<Task, Set<TaskContent>, Float> as a parameter, we need to build this
        // ParameterizedType here by ourselves.
        private static final ParameterizedType biFuncType =
                new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] {Task.class, setType, Float.class};
                    }

                    @Override
                    public Type getRawType() {
                        return BiFunction.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };

        private static final ParameterizedType setType =
                new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] {TaskContent.class};
                    }

                    @Override
                    public Type getRawType() {
                        return Set.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };

        // endregion

        @Override
        public List<Type> getParameterTypes() {
            var typeArr = new Type[] {biFuncType};
            return Arrays.stream(typeArr).toList();
        }
    }
}
