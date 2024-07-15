package task.dslinterop;

import core.Entity;
import dsl.annotation.DSLExtensionMethod;
import dsl.annotation.DSLTypeAdapter;
import dsl.annotation.DSLTypeMember;
import dsl.annotation.DSLTypeNameMember;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import task.Task;
import task.TaskContent;
import task.reporting.GradingFunctions;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.MultipleChoice;

/** Typeadapter for creation of {@link MultipleChoice} instances via dsl. */
public class DSLMultipleChoice {
  /**
   * WTF? .
   *
   * @param name foo
   * @param description foo
   * @param answers foo
   * @param points foo
   * @param pointsToPass foo
   * @param correctAnswerIndices foo
   * @param explanation foo
   * @param gradingFunction foo
   * @param scenarioBuilder foo
   * @return foo
   */
  @DSLTypeAdapter(name = "multiple_choice_task")
  public static MultipleChoice buildQuizFromMultipleChoiceTask(
      @DSLTypeNameMember String name,
      @DSLTypeMember(name = "description") String description,
      @DSLTypeMember(name = "answers") List<Quiz.Content> answers,
      @DSLTypeMember(name = "points") float points,
      @DSLTypeMember(name = "points_to_pass") float pointsToPass,
      @DSLTypeMember(name = "correct_answer_indices") List<Integer> correctAnswerIndices,
      @DSLTypeMember(name = "explanation") String explanation,
      @DSLTypeMember(name = "grading_function")
          BiFunction<Task, Set<TaskContent>, Float> gradingFunction,
      @DSLTypeMember(name = "scenario_builder")
          Function<MultipleChoice, Set<Set<Entity>>> scenarioBuilder
      //  scoreFunction
      ) {
    MultipleChoice mc = new MultipleChoice(description);
    mc.taskName(name);
    mc.explanation(explanation);

    if (scenarioBuilder != null) {
      mc.scenarioBuilderFunction(scenarioBuilder);
    }

    if (points > 0.0f && pointsToPass > 0.0f) {
      mc.points(points, pointsToPass);
    }

    for (Quiz.Content answer : answers) {
      mc.addAnswer(answer);
    }

    for (var index : correctAnswerIndices) {
      mc.addCorrectAnswerIndex(index);
    }

    // default value
    mc.scoringFunction(
        Objects.requireNonNullElseGet(gradingFunction, GradingFunctions::multipeChoiceGrading));

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
   * instance.
   */
  @DSLExtensionMethod(name = "get_content", extendedType = MultipleChoice.class)
  public static class GetContentMethod
      implements IDSLExtensionMethod<MultipleChoice, List<TaskContent>> {
    /** Get the stored {@link TaskContent} of a {@link MultipleChoice} instance. */
    public static DSLMultipleChoice.GetContentMethod instance =
        new DSLMultipleChoice.GetContentMethod();

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

  /** {@link IDSLExtensionMethod} to set the scenario text in a single choice task instance. */
  @DSLExtensionMethod(name = "set_scenario_text", extendedType = MultipleChoice.class)
  public static class SetScenarioText implements IDSLExtensionMethod<MultipleChoice, Void> {
    /** Set the scenario text in a single choice task instance. */
    public static DSLMultipleChoice.SetScenarioText instance =
        new DSLMultipleChoice.SetScenarioText();

    @Override
    public Void call(MultipleChoice instance, List<Object> params) {
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
   * {@link IDSLExtensionMethod} to set the grading function of a {@link MultipleChoice} instance.
   */
  @DSLExtensionMethod(name = "set_grading_function", extendedType = MultipleChoice.class)
  public static class MultipleChoiceSetGradingFunction
      implements IDSLExtensionMethod<MultipleChoice, Void> {
    /** Set the grading function of a {@link MultipleChoice} instance. */
    public static DSLMultipleChoice.MultipleChoiceSetGradingFunction instance =
        new DSLMultipleChoice.MultipleChoiceSetGradingFunction();

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

  /**
   * {@link IDSLExtensionMethod} to set the grading function of a {@link MultipleChoice} instance.
   */
  @DSLExtensionMethod(name = "set_answer_picker_function", extendedType = MultipleChoice.class)
  public static class MultipleChoiceSetAnswerPickerFunction
      implements IDSLExtensionMethod<MultipleChoice, Void> {
    /** The {@link MultipleChoiceSetAnswerPickerFunction} instance. */
    public static DSLMultipleChoice.MultipleChoiceSetAnswerPickerFunction instance =
        new DSLMultipleChoice.MultipleChoiceSetAnswerPickerFunction();

    @Override
    public Void call(MultipleChoice instance, List<Object> params) {
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
