package task.tasktype;

import java.util.ArrayList;
import java.util.List;
import task.Task;
import task.reporting.GradingFunctions;

/**
 * A Replacement Task.
 *
 * <p>Stores a collection of elements as a solution and a set of rules as replacement terms.
 *
 * <p>Note: Not yet implemented
 */
public class ReplacementTask extends Task {
  private final List<Element> solution;
  private final List<Rule> rules;

  /**
   * Create a new Replacement Task.
   *
   * @param rules Rules that define which and how elements can be replaced.
   */
  public ReplacementTask(List<Rule> rules) {
    super();
    this.solution = new ArrayList<>();
    this.rules = rules;
    scoringFunction(GradingFunctions.replacementGrading());
  }

  /**
   * WTF? .
   *
   * @param e foo
   */
  public void addSolution(Element e) {
    solution.add(e);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public List<Element> solution() {
    return new ArrayList<>(solution);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public List<Rule> rules() {
    return new ArrayList<>(rules);
  }

  @Override
  public String correctAnswersAsString() {
    StringBuilder answers = new StringBuilder();
    solution.forEach(s -> answers.append(s.content().toString()).append(System.lineSeparator()));
    answers.append("Ersetzungsregeln").append(System.lineSeparator());
    rules.forEach(r -> answers.append(r.toString()).append(System.lineSeparator()));
    return answers.toString();
  }

  /**
   * Rules for how elements can be replaced by each other.
   *
   * <p>This is basically an abstract implementation of {@link contrib.crafting.Recipe}.
   *
   * @param input foo
   * @param ordered foo
   * @param output foo
   */
  public record Rule(boolean ordered, Element[] input, Element[] output) {
    @Override
    public String toString() {
      // todo
      return "There is no String represnation for the rules yet.";
    }
  }
}
