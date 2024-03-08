package task.tasktype;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import task.Task;
import task.TaskContent;
import task.reporting.GradingFunctions;

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

  /** The empty element. */
  public static final Element<String> EMPTY_ELEMENT = new Element<>("");

  /** The name of the empty element. */
  public static final String EMPTY_ELEMENT_NAME = "$EMPTY_ELEMENT$";

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

  @Override
  public String correctAnswersAsString() {
    StringBuilder answers = new StringBuilder();
    solution
        .keySet()
        .forEach(
            new Consumer<Element>() {
              @Override
              public void accept(Element element) {
                if (!element.content().toString().isBlank())
                  answers.append(element.content().toString()).append(": ");
                else answers.append("Nicht zuzuordnen: ");
                solution
                    .get(element)
                    .forEach(e -> answers.append(e.content().toString()).append(", "));
                answers.deleteCharAt(answers.length() - 2); // remove last ","
                answers.append(System.lineSeparator());
              }
            });

    return answers.toString();
  }
}
