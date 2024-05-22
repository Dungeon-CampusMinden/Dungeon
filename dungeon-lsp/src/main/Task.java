package main;

import static main.Main.LOGGER;

import java.util.concurrent.Callable;

public class Task<V> {

  public enum Priority {
    HIGH,
    MEDIUM,
    LOW
  }

  Callable<V> internalCallable;
  Priority priority;
  String name;

  public Task(Callable<V> runnable, Priority priority, String name) {
    this.internalCallable = runnable;
    this.priority = priority;
    this.name = name;
  }

  public V call() throws Throwable {
    LOGGER.info("EXECUTING TASK: " + name);
    return internalCallable.call();
  }

  public Priority getPriority() {
    return priority;
  }

  @Override
  public String toString() {
    return "TASK: " + this.name;
  }
}
