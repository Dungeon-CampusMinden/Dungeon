package modules.computer;

/** Represents the player's progress on the computer. */
public enum ComputerProgress {

  /** Before the light is turned on. */
  OFF(0),
  /** After the light is turned on, but before the player has logged in. */
  ON(1),
  /** After the player has logged in. */
  LOGGED_IN(2),
  ;

  private final int progress;

  ComputerProgress(int progress) {
    this.progress = progress;
  }

  /**
   * Returns the progress value associated with this ComputerProgress state.
   *
   * @return the progress value, where higher values indicate further progress in the computer
   *     interaction sequence
   */
  public int progress() {
    return progress;
  }

  /**
   * Shorthand for checking the progress against another state.
   *
   * @param other the ComputerProgress state to compare against
   * @return true if this ComputerProgress state has reached or exceeded the progress of the other
   *     state, false otherwise
   */
  public boolean hasReached(ComputerProgress other) {
    return this.progress >= other.progress;
  }
}
