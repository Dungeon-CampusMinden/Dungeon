package server;

import java.util.ArrayList;

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
    return "Condition: " + condition + ", is Repeating: " + isRepeating +
      ", Condition Result: " + conditionResult;
  }
}
