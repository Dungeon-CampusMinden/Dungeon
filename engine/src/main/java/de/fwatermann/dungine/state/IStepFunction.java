package de.fwatermann.dungine.state;

@FunctionalInterface
public interface IStepFunction<T> {

  T run(StepResults results);

}
