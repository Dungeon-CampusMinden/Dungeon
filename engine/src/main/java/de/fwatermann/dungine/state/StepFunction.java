package de.fwatermann.dungine.state;

public class StepFunction {

  @FunctionalInterface
  public interface IWithResults<T> {
    T run(StepResults results);
  }

  @FunctionalInterface
  public interface IWithoutResults<T> {
    T run();
  }

  @FunctionalInterface
  public interface IVoidWithResults {
    void run(StepResults results);
  }

  @FunctionalInterface
  public interface IVoidWithoutResults {
    void run();
  }
}
