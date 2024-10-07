package server;

import java.util.ArrayList;

/**
 * This class is used to store all important values of a repeat instruction. This class contains the
 * body of the repeat loop, the current counter and the target counter.
 */
public class RepeatStats {
  private final int targetCounter;

  private int counter;

  /**
   * Body of the repeat loop. Contains all actions that need to be performed when the repeat loop is
   * repeating itself.
   */
  public ArrayList<String> repeatBody;

  /**
   * This boolean indicates if the repeat loop is repeating itself. Will be set to true when the
   * scope of the repeat loop ends but the current counter did not reach the target counter yet.
   */
  public boolean isRepeating = false;

  /**
   * Create a new repeat loop with the given target counter.
   *
   * @param targetCounter The targetCounter controls how often the repeat loop will be repeated.
   */
  public RepeatStats(int targetCounter) {
    this.targetCounter = targetCounter - 1;
    counter = 0;
    repeatBody = new ArrayList<>();
  }

  /**
   * Evaluate if the repeat loop is finished.
   *
   * @return Returns true if the counter reached the target counter. Otherwise, false.
   */
  public boolean evalRepeatComplete() {
    return counter >= targetCounter;
  }

  /** Increase the counter of the repeat loop. */
  public void increaseCounter() {
    counter++;
  }

  @Override
  public String toString() {
    return "Counter: " + counter + ", Target Counter: " + targetCounter;
  }
}
