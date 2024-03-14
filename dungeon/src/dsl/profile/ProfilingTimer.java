package dsl.profile;

public class ProfilingTimer {
  private long startTime;

  public void start() {
    startTime = System.nanoTime();
  }

  public void stopAndPrint(String message) {
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println(message + ": " + elapsedTime + " nanoseconds");
  }
}
