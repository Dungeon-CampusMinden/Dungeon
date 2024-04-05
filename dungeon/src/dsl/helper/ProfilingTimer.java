package dsl.helper;

import java.util.HashMap;

public class ProfilingTimer implements AutoCloseable {
  public enum Unit {
    nano,
    micro,
    milli
  }

  private final String message;
  private final long startTime;
  private final HashMap<String, Long> times;
  private final Unit unit;

  public ProfilingTimer(String message, HashMap<String, Long> times) {
    this.message = message;
    this.times = times;
    this.unit = Unit.nano;
    this.startTime = System.nanoTime();
  }

  public ProfilingTimer(String message, HashMap<String, Long> times, Unit unit) {
    this.message = message;
    this.times = times;
    this.unit = unit;
    this.startTime = System.nanoTime();
  }

  @Override
  public void close() {
    long elapsedTime = System.nanoTime() - startTime;
    long divided =
        switch (this.unit) {
          case nano -> elapsedTime;
          case micro -> elapsedTime / 1000;
          case milli -> elapsedTime / 1000000;
        };

    this.times.put(message, divided);
  }
}
