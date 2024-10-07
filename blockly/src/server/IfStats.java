package server;

/**
 * This class is used to store all important values of an if instruction. This class contains the
 * result of the if condition. The result of the if condition will be inverted and set as the else
 * flag if there is an else statement.
 */
public class IfStats {
  /**
   * Will be set after evaluating the condition of the if-statement. If either the if-flag or
   * else-flag is set, actions may be performed. Otherwise, no action may be performed.
   */
  public boolean if_flag;

  /** Will be set if we parse a "sonst" action. Will be set to the negation of the if-flag. */
  public boolean else_flag;

  /**
   * Create a new if-statement with the result of the condition.
   *
   * @param if_flag The result of the condition. Will be set as the if_flag. The else flag is
   *     initially always false.
   */
  public IfStats(boolean if_flag) {
    this.if_flag = if_flag;
    this.else_flag = false;
  }

  /**
   * Check if actions in the current if statement should be executed.
   *
   * @return Returns true if either if_flag or else_flag is true.
   */
  public boolean executeAction() {
    return if_flag || else_flag;
  }
}
