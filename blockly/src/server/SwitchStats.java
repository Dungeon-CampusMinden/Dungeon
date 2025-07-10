package server;

/** This class stores all important values of a switch scope. */
public class SwitchStats {
  /**
   * The evaluated expression behind "entscheide ueber".
   *
   * <p>This value is the result of getActualValueFromExpression(...) for the switch statement.
   */
  public final int switchValue;

  /**
   * Whether a case has already matched (so that no further cases are executed after the first
   * match).
   *
   * <p>Once a case is matched and executed, this flag prevents subsequent cases from running.
   */
  public boolean caseMatched;

  /**
   * Whether actions in the current case/default branch are allowed to execute.
   *
   * <p>If true, code within the matching case or default branch will run; otherwise it will be
   * skipped.
   */
  public boolean executing;

  /**
   * Creates a new SwitchStats instance with the evaluated switchValue.
   *
   * @param switchValue the result of getActualValueFromExpression(...)
   */
  public SwitchStats(int switchValue) {
    this.switchValue = switchValue;
    this.caseMatched = false;
    this.executing = false;
  }

  /**
   * Checks if actions are allowed to execute in the current switch block.
   *
   * @return true if executing == true (i.e., we are in the matching case/default branch)
   */
  public boolean executeAction() {
    return executing;
  }
}
