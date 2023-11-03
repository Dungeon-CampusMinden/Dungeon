package task.taskdsltypes;

import static task.AssignTask.EMPTY_ELEMENT;

import reporting.GradingFunctions;

import semanticanalysis.types.*;

import task.AssignTask;
import task.Element;
import task.Task;
import task.TaskContent;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

public class AssignTaskDSLType {
    public static final String EMPTY_ELEMENT_NAME = AssignTask.EMPTY_ELEMENT_NAME;

    @DSLTypeAdapter(name = "assign_task")
    public static AssignTask buildAssignTask(
            @DSLTypeNameMember String name,
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "solution") Set<List<Element<String>>> solution,
            @DSLTypeMember(name = "grading_function")
                    BiFunction<Task, Set<TaskContent>, Float> gradingFunction) {

        AssignTask task = new AssignTask();
        task.taskText(description);
        task.taskName(name);

        // set scoring function either to parameter or default value
        task.scoringFunction(
                Objects.requireNonNullElseGet(
                        gradingFunction, GradingFunctions::assignGradingEasy));

        // TODO: handle EMPTY_ELEMENT_NAME

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

        task.solution(solutionMap);
        return task;
    }

    /**
     * {@link IDSLExtensionMethod} to get the stored {@link TaskContent} of a {@link AssignTask}
     * instance
     */
    @DSLExtensionMethod(name = "get_solution", extendedType = AssignTask.class)
    public static class GetSolutionMethod
            implements IDSLExtensionMethod<AssignTask, Map<Element, Set<Element>>> {
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
}
