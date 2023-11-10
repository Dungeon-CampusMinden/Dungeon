package task.taskdsltypes;

import reporting.GradingFunctions;

import semanticanalysis.types.*;

import task.Quiz;
import task.Task;
import task.TaskContent;
import task.quizquestion.SingleChoice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Typeadapter for the creation of {@link SingleChoice} instances via dsl. */
public class SingleChoiceTask {

    @DSLTypeAdapter(name = "single_choice_task")
    public static SingleChoice buildQuizFromSingleChoiceTask(
            @DSLTypeNameMember String name,
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
            @DSLTypeMember(name = "points") float points,
            @DSLTypeMember(name = "points_to_pass") float pointsToPass,
            @DSLTypeMember(name = "correct_answer_index") int correctAnswerIndex,
            @DSLTypeMember(name = "explanation") String explanation,
            @DSLTypeMember(name = "grading_function")
                    BiFunction<Task, Set<TaskContent>, Float> gradingFunction) {
        SingleChoice sc = new SingleChoice(description);
        sc.taskName(name);
        sc.explanation(explanation);

        if (points > 0.0f && pointsToPass > 0.0f) {
            sc.points(points, pointsToPass);
        }

        for (Quiz.Content answer : answers) {
            sc.addAnswer(answer);
        }

        sc.addCorrectAnswerIndex(correctAnswerIndex);
        // default value
        sc.scoringFunction(
                Objects.requireNonNullElseGet(
                        gradingFunction, GradingFunctions::singleChoiceGrading));

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
        public List<Type> getParameterTypes() {
            var arr = new Type[] {};
            return Arrays.stream(arr).toList();
        }
    }

    /** {@link IDSLExtensionMethod} to set the scenario text in a single choice task instance */
    @DSLExtensionMethod(name = "set_scenario_text", extendedType = SingleChoice.class)
    public static class SetScenarioText implements IDSLExtensionMethod<SingleChoice, Void> {
        public static SetScenarioText instance = new SetScenarioText();

        @Override
        public Void call(SingleChoice instance, List<Object> params) {
            String valueToSet = (String) params.get(0);
            instance.scenarioText(valueToSet);
            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            var arr = new Type[] {String.class};
            return Arrays.stream(arr).toList();
        }
    }

    /**
     * {@link IDSLExtensionMethod} to set the grading function of a {@link SingleChoice} instance.
     */
    @DSLExtensionMethod(name = "set_grading_function", extendedType = SingleChoice.class)
    public static class SingleChoiceSetGradingFunction
            implements IDSLExtensionMethod<SingleChoice, Void> {
        public static SingleChoiceTask.SingleChoiceSetGradingFunction instance =
                new SingleChoiceTask.SingleChoiceSetGradingFunction();

        @Override
        public Void call(SingleChoice instance, List<Object> params) {
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

    /**
     * {@link IDSLExtensionMethod} to set the grading function of a {@link SingleChoice} instance.
     */
    @DSLExtensionMethod(name = "set_answer_picker_function", extendedType = SingleChoice.class)
    public static class SingleChoiceSetAnswerPickerFunction
            implements IDSLExtensionMethod<SingleChoice, Void> {
        public static SingleChoiceTask.SingleChoiceSetAnswerPickerFunction instance =
                new SingleChoiceTask.SingleChoiceSetAnswerPickerFunction();

        @Override
        public Void call(SingleChoice instance, List<Object> params) {
            var func = (Function<Task, Set<TaskContent>>) params.get(0);
            instance.answerPickingFunction(func);
            return null;
        }

        // region parameterized parameter type declaration

        // The TypeBuilder needs an implementation of ParameterizedType (with the actual type
        // information)
        // to create a FunctionType for the method parameter. As this method will accept a
        // BiFunction<Task, Set<TaskContent>, Float> as a parameter, we need to build this
        // ParameterizedType here by ourselves.
        private static final ParameterizedType funcType =
                new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] {Task.class, setType};
                    }

                    @Override
                    public Type getRawType() {
                        return Function.class;
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
            var typeArr = new Type[] {funcType};
            return Arrays.stream(typeArr).toList();
        }
    }
}
