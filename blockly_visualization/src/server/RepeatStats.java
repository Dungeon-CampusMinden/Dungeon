package server;

import java.util.ArrayList;

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

  public boolean evalRepeatComplete() {
    return counter >= targetCounter;
  }

  public void increaseCounter() {
    counter++;
  }

  @Override
  public String toString() {
    return "Counter: " + counter + ", Target Counter: " + targetCounter;
  }
}
