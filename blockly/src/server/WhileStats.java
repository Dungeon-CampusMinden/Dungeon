package server;

import java.util.ArrayList;

/**
 * This class is used to store all important values of a while loop. This class contains the
 * condition, the condition result, a boolean representing if the loop is currently repeating and an
 * array list containing the instructions of the while body.
 */
public class WhileStats {
  public String condition;
  public boolean conditionResult;
  public boolean isRepeating;
  public ArrayList<String> whileBody;

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
