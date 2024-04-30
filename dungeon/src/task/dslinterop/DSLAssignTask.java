package task.dslinterop;

import static task.tasktype.AssignTask.EMPTY_ELEMENT;

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
import task.tasktype.AssignTask;
import task.tasktype.Element;

/** Typeadapter for creation of {@link AssignTask} instances via dsl. */
public class DSLAssignTask {
  /** The empty element name. */
  public static final String EMPTY_ELEMENT_NAME = AssignTask.EMPTY_ELEMENT_NAME;

  /**
   * WTF? .
   *
   * @param name foo
   * @param description foo
   * @param points foo
   * @param pointsToPass foo
   * @param solution foo
   * @param explanation foo
   * @param gradingFunction foo
   * @param scenarioBuilder foo
   * @return foo
   */
  @DSLTypeAdapter(name = "assign_task")
  public static AssignTask buildAssignTask(
      @DSLTypeNameMember String name,
      @DSLTypeMember(name = "description") String description,
      @DSLTypeMember(name = "points") float points,
      @DSLTypeMember(name = "points_to_pass") float pointsToPass,
      @DSLTypeMember(name = "solution") Set<List<Element<String>>> solution,
      @DSLTypeMember(name = "explanation") String explanation,
      @DSLTypeMember(name = "grading_function")
          BiFunction<Task, Set<TaskContent>, Float> gradingFunction,
      @DSLTypeMember(name = "scenario_builder")
          Function<AssignTask, Set<Set<Entity>>> scenarioBuilder) {
    AssignTask task = new AssignTask();
    task.taskText(description);
    task.taskName(name);
    task.explanation(explanation);

    if (scenarioBuilder != null) {
      task.scenarioBuilderFunction(scenarioBuilder);
    }

    if (points > 0.0f && pointsToPass > 0.0f) {
      task.points(points, pointsToPass);
    }

    // set scoring function either to parameter or default value
    task.scoringFunction(
        Objects.requireNonNullElseGet(gradingFunction, GradingFunctions::assignGradingEasy));

    // scan for duplicates
    HashMap<String, Element<String>> elementMap = new HashMap<>();
    HashMap<Element<String>, Element<String>> substitutionMap = new HashMap<>();
    for (var elementList : solution) {
      for (var element : elementList) {
        String content = element.content();
        if (elementMap.containsKey(content)) {
          // get element from element map
          var substituationElement = elementMap.get(content);
          substitutionMap.put(element, substituationElement);
        } else if (content.equals(EMPTY_ELEMENT_NAME)) {
          substitutionMap.put(element, EMPTY_ELEMENT);
        } else {
          elementMap.put(content, element);
        }
      }
    }

    // build solution map (ignore all elements after first and second)
    HashMap<Element, Set<Element>> solutionMap = new HashMap<>();

    // each definition element (the one on the right side) can only be assigned to
    // one term element
    HashMap<Element, Element> definitionElementToTermElement = new HashMap<>();
    for (var elementList : solution) {
      if (elementList.size() < 2) {
        throw new RuntimeException("Element count in solution List to small!!");
      }
      var termElement = elementList.get(0);
      if (substitutionMap.containsKey(termElement)) {
        termElement = substitutionMap.get(termElement);
      }
      if (!solutionMap.containsKey(termElement)) {
        solutionMap.put(termElement, new HashSet<>());
      }
      var definitionSet = solutionMap.get(termElement);

      var definitionElement = elementList.get(1);
      if (substitutionMap.containsKey(definitionElement)) {
        definitionElement = substitutionMap.get(definitionElement);
      }

      if (definitionElementToTermElement.containsKey(definitionElement)) {
        var definitionElementsAssignedTermElement =
            definitionElementToTermElement.get(definitionElement);
        if (!definitionElementsAssignedTermElement.equals(termElement)) {
          throw new RuntimeException(
              "Definition element was assigned to two different term elements!");
        }
      }

      definitionSet.add(definitionElement);
      definitionElementToTermElement.put(definitionElement, termElement);
    }

    // add link to task
    for (var entrySet : solutionMap.entrySet()) {
      task.addContent(entrySet.getKey());
      for (var entry : entrySet.getValue()) {
        task.addContent(entry);
      }
    }

    task.solution(solutionMap);
    return task;
  }

  /**
   * {@link IDSLExtensionMethod} to get the stored {@link TaskContent} of a {@link AssignTask}
   * instance.
   */
  @DSLExtensionMethod(name = "get_solution", extendedType = AssignTask.class)
  public static class GetSolutionMethod
      implements IDSLExtensionMethod<AssignTask, Map<Element, Set<Element>>> {
    /** Get the stored {@link TaskContent} of a {@link AssignTask} instance. */
    public static GetSolutionMethod instance = new GetSolutionMethod();

    @Override
    public Map<Element, Set<Element>> call(AssignTask instance, List<Object> params) {
      return instance.solution();
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {};
      return Arrays.stream(arr).toList();
    }
  }

  /** {@link IDSLExtensionMethod} to set the scenario text in a single choice task instance. */
  @DSLExtensionMethod(name = "set_scenario_text", extendedType = AssignTask.class)
  public static class SetScenarioText implements IDSLExtensionMethod<AssignTask, Void> {
    /** Set the scenario text in a single choice task instance. */
    public static DSLAssignTask.SetScenarioText instance = new DSLAssignTask.SetScenarioText();

    @Override
    public Void call(AssignTask instance, List<Object> params) {
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

  /** {@link IDSLExtensionMethod} to set the grading function of a {@link AssignTask} instance. */
  @DSLExtensionMethod(name = "set_grading_function", extendedType = AssignTask.class)
  public static class AssignTaskSetGradingFunction
      implements IDSLExtensionMethod<AssignTask, Void> {
    /** Set the grading function of a {@link AssignTask} instance. */
    public static DSLAssignTask.AssignTaskSetGradingFunction instance =
        new DSLAssignTask.AssignTaskSetGradingFunction();

    @Override
    public Void call(AssignTask instance, List<Object> params) {
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

  /** {@link IDSLExtensionMethod} to set the grading function of a {@link AssignTask} instance. */
  @DSLExtensionMethod(name = "set_answer_picker_function", extendedType = AssignTask.class)
  public static class AssignTaskSetAnswerPickerFunction
      implements IDSLExtensionMethod<AssignTask, Void> {
    /** The {@link AssignTaskSetAnswerPickerFunction} instance. */
    public static DSLAssignTask.AssignTaskSetAnswerPickerFunction instance =
        new DSLAssignTask.AssignTaskSetAnswerPickerFunction();

    @Override
    public Void call(AssignTask instance, List<Object> params) {
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
