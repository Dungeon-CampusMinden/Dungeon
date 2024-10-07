package server;

import java.util.ArrayList;

/**
 * This class is used to store all important values of a while loop. This class contains the
 * condition, the condition result, a boolean representing if the loop is currently repeating and an
 * array list containing the instructions of the while body.
 */
public class WhileStats {
  /**
   * Condition string of the while-loop. Will be used to eval the while-loop when the scope of the
   * while-loop was about to be closed.
   */
  public String condition;

  /** Current condition result of the while-loop. */
  public boolean conditionResult;

  /**
   * Boolean indicating if the while-loop is repeating itself. Will be set to true if the scope of
   * the while-loop was about to be closed, but the condition evaluates to true.
   */
  public boolean isRepeating;

  /**
   * Body of the while-loop. Contains all actions that need to be performed when repeating the
   * while-loop.
   */
  public ArrayList<String> whileBody;

  /**
   * Create a new while-loop.
   *
   * @param condition Condition of the while-loop.
   * @param conditionResult Current condition result.
   */
  public WhileStats(String condition, boolean conditionResult) {
    this.condition = condition;
    isRepeating = false;
    whileBody = new ArrayList<>();
    this.conditionResult = conditionResult;
  }

  @Override
  public String toString() {
    return "Condition: "
        + condition
        + ", is Repeating: "
        + isRepeating
        + ", Condition Result: "
        + conditionResult;
  }
}
