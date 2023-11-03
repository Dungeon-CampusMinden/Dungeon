package task.taskdsltypes;

import reporting.GradingFunctions;
import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import task.AssignTask;
import task.Element;
import task.Task;
import task.TaskContent;

import java.util.*;
import java.util.function.BiFunction;

public class AssignTaskDSLType {
    public static Element<String> EMPTY_ELEMENT = new Element<>("");
    public static String EMPTY_ELEMENT_NAME = "$EMPTY_ELEMENT$";

    @DSLTypeAdapter(name = "assign_task")
    public static AssignTask buildAssignTask(
            @DSLTypeMember(name = "description") String description,
            @DSLTypeMember(name = "solution") Set<List<Element<String>>> solution,
            @DSLTypeMember(name = "grading_function") BiFunction<Task, Set<TaskContent>, Float> gradingFunction) {

        // TODO: description?!
        AssignTask task = new AssignTask();

        // set scoring function either to parameter or default value
        task.scoringFunction(Objects.requireNonNullElseGet(gradingFunction, GradingFunctions::assignGradingEasy));

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
     * {@link IDSLExtensionMethod} to get the stored {@link TaskContent} of a {@link SingleChoice}
     * instance
     */
    /*
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
            var arr = new Class<?>[]{};
            return Arrays.stream(arr).toList();
        }
    }
     */
}
