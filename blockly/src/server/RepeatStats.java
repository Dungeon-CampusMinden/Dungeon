package server;

import java.util.ArrayList;

/**
 *  This class is used to store all important values of a repeat instruction. This class contains the body of the repeat
 *  loop, the current counter and the target counter.
 */
public class RepeatStats {
  private final int targetCounter;

  private int counter;

  public ArrayList<String> repeatBody;

  public boolean isRepeating = false;

  public RepeatStats(int targetCounter) {
    this.targetCounter = targetCounter - 1;
    counter = 0;
    repeatBody = new ArrayList<>();
  }

  /**
   * Evaluate if the repeat loop is finished.
   * @return
   */
  public boolean evalRepeatComplete() {
    return counter >= targetCounter;
  }

  /**
   * Increase the counter of the repeat loop.
   */
  public void increaseCounter() {
    counter++;
  }

  @Override
  public String toString() {
    return "Counter: " + counter + ", Target Counter: " + targetCounter;
  }
}
