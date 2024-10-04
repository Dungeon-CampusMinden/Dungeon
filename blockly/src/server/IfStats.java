package server;

/**
 * This class is used to store all important values of an if instruction. This class contains the
 * result of the if condition. The result of the if condition will be inverted and set as the else
 * flag if there is an else statement.
 */
public class IfStats {
  public boolean if_flag;
  public boolean else_flag;

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
