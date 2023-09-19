package task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Assignment Task.
 *
 * <p>Stores a Map as a solution with Elements as keys (for example, an index) and a Set of Elements
 * as values that should be assigned to the keys.
 */
public class AssignTask extends Task {
    private final Map<Element, Set<Element>> solution;

    /**
     * Create an Assignment Task with the given solution map.
     *
     * @param solution The map containing Elements as keys and Sets of Elements as values.
     */
    public AssignTask(Map<Element, Set<Element>> solution) {
        super();
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
