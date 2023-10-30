package task;

import reporting.GradingFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Assignment Task.
 *
 * <p>Stores a Map as a solution with Elements as keys (for example, an index) and a Set of Elements
 * as values that should be assigned to the keys.
 */
public class AssignTask extends Task {
    private static final BiFunction<Task, Set<TaskContent>, Float> DEFAULT_SCORING_FUNCTION =
            GradingFunctions.assignGradingEasy();
    private Map<Element, Set<Element>> solution;

    /** Create an Assignment Task with the given solution map. */
    public AssignTask() {
        super();
        scoringFunction(DEFAULT_SCORING_FUNCTION);
    }

    /**
     * Add the solution to this task.
     *
     * @param solution A Map where the keys are the containers and the values are the elements that
     *     must be matched to the containers.
     */
    public void solution(Map<Element, Set<Element>> solution) {
        this.solution = solution;
    }

    /**
     * Get a copy of the solution map.
     *
     * @return A copy of the solution map.
     */
    public Map<Element, Set<Element>> solution() {
        return new HashMap<>(solution);
    }
}
