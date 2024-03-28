package testingUtils;

/** A simple counter. */
public final class SimpleCounter {
  private int count = 0;

  /** A method to increment the count variable. */
  public void inc() {
    count++;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public int getCount() {
    return count;
  }
}
