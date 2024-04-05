package main;

public class Helpers {
  /**
   * Get the method name for a depth in call stack. <br>
   * Utility function
   *
   * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
   * @return method name
   */
  public static String getMethodName(final int depth) {
    final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
    int idx = depth + 1;
    String name = ste[idx].getMethodName();
    return name;
  }

  public static String getMethodName() {
    // invoking method
    String name = getMethodName(2);
    return name;
  }
}
