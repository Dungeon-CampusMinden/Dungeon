package de.fwatermann.dungine.state;

import de.fwatermann.dungine.utils.Then;
import de.fwatermann.dungine.window.GameWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The `LoadStepper` class manages a sequence of steps to be executed, optionally on the main
 * thread. It allows for defining steps with or without results and with or without context.
 */
public class LoadStepper {

  private final GameWindow window;

  /**
   * Constructs a new `LoadStepper` with the specified game window.
   *
   * @param window the game window
   */
  public LoadStepper(GameWindow window) {
    this.window = window;
  }

  /** The map of step identifiers to step indices. */
  protected final Map<String, Integer> stepMap = new HashMap<>();

  private final StepResults results = new StepResults(this);
  private final List<Step> steps = new ArrayList<>();
  private Step done;
  private int currentStep = 0;

  /**
   * Adds a step with a result context and return value.
   *
   * @param id the identifier of the step
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, boolean mainThread, StepFunction.IWithResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step(step, mainThread));
    return this;
  }

  /**
   * Adds a step with a result context and return value.
   *
   * @param id the identifier of the step
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, StepFunction.IWithResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step(step, false));
    return this;
  }

  /**
   * Adds a step with a result context and return value.
   *
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(boolean mainThread, StepFunction.IWithResults<?> step) {
    this.steps.add(new Step(step, mainThread));
    return this;
  }

  /**
   * Adds a step with a result context and return value.
   *
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(StepFunction.IWithResults<?> step) {
    this.steps.add(new Step(step, false));
    return this;
  }

  /**
   * Adds a step without a result context and return value.
   *
   * @param id the identifier of the step
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, boolean mainThread, StepFunction.IWithoutResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> step.run(), mainThread));
    return this;
  }

  /**
   * Adds a step without a result context and return value.
   *
   * @param id the identifier of the step
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, StepFunction.IWithoutResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> step.run(), false));
    return this;
  }

  /**
   * Adds a step without a result context and return value.
   *
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(boolean mainThread, StepFunction.IWithoutResults<?> step) {
    this.steps.add(new Step((r) -> step.run(), mainThread));
    return this;
  }

  /**
   * Adds a step without a result context and return value.
   *
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(StepFunction.IWithoutResults<?> step) {
    this.steps.add(new Step((r) -> step.run(), false));
    return this;
  }

  /**
   * Adds a step with a result context and no return value.
   *
   * @param id the identifier of the step
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, boolean mainThread, StepFunction.IVoidWithResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(
        new Step(
            (r) -> {
              step.run(r);
              return null;
            },
            mainThread));
    return this;
  }

  /**
   * Adds a step with a result context and no return value.
   *
   * @param id the identifier of the step
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, StepFunction.IVoidWithResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(
        new Step(
            (r) -> {
              step.run(r);
              return null;
            },
            false));
    return this;
  }

  /**
   * Adds a step with a result context and no return value.
   *
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(boolean mainThread, StepFunction.IVoidWithResults step) {
    this.steps.add(
        new Step(
            (r) -> {
              step.run(r);
              return null;
            },
            mainThread));
    return this;
  }

  /**
   * Adds a step with a result context and no return value.
   *
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(StepFunction.IVoidWithResults step) {
    this.steps.add(
        new Step(
            (r) -> {
              step.run(r);
              return null;
            },
            false));
    return this;
  }

  /**
   * Adds a step without a result context and no return value.
   *
   * @param id the identifier of the step
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, boolean mainThread, StepFunction.IVoidWithoutResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(
        new Step(
            (r) -> {
              step.run();
              return null;
            },
            mainThread));
    return this;
  }

  /**
   * Adds a step without a result context and no return value.
   *
   * @param id the identifier of the step
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(String id, StepFunction.IVoidWithoutResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(
        new Step(
            (r) -> {
              step.run();
              return null;
            },
            false));
    return this;
  }

  /**
   * Adds a step without a result context and no return value.
   *
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(boolean mainThread, StepFunction.IVoidWithoutResults step) {
    this.steps.add(
        new Step(
            (r) -> {
              step.run();
              return null;
            },
            mainThread));
    return this;
  }

  /**
   * Adds a step without a result context and no return value.
   *
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper step(StepFunction.IVoidWithoutResults step) {
    this.steps.add(
        new Step(
            (r) -> {
              step.run();
              return null;
            },
            false));
    return this;
  }

  /**
   * Sets the final step to be executed after all other steps.
   *
   * @param mainThread whether the step should run on the main thread
   * @param step the step function
   * @return the updated `LoadStepper` instance
   */
  public LoadStepper done(boolean mainThread, StepFunction.IVoidWithResults step) {
    this.done =
        new Step(
            (r) -> {
              step.run(r);
              return null;
            },
            mainThread);
    return this;
  }

  /** Starts the execution of the steps. */
  public void start() {
    this.next();
  }

  /** Executes the next step in the sequence. */
  private void next() {
    if (this.steps.isEmpty() || this.currentStep >= this.steps.size()) { // Done
      if (this.done == null) return;
      if (this.done.mainThread) {
        this.window.runOnMainThread(
            () -> {
              this.done.func().run(this.results);
            });
      } else {
        new Thread(
                () -> {
                  this.done.func().run(this.results);
                },
                "Step-Done")
            .start();
      }
      return;
    }
    Step step = this.steps.get(this.currentStep++);
    if (step.mainThread) {
      this.window
          .runOnMainThread(
              () -> {
                Object result = step.func().run(this.results);
                this.results.setResult(this.currentStep - 1, result);
              })
          .then(this::next);
    } else {
      Then then =
          new Then(
              () -> {
                Object result = step.func().run(this.results);
                this.results.setResult(this.currentStep - 1, result);
              });
      then.then(this::next);
      new Thread(
              () -> {
                then.run.run();
                then.then().run();
              },
              "Step-" + (this.currentStep - 1))
          .start();
    }
  }

  /**
   * Returns the current step index.
   *
   * @return the current step index
   */
  public int currentStep() {
    return this.currentStep;
  }

  /**
   * Returns the total number of steps.
   *
   * @return the total number of steps
   */
  public int stepCount() {
    return this.steps.size();
  }

  /** The `Step` record represents a single step in the `LoadStepper`. */
  private record Step(StepFunction.IWithResults<?> func, boolean mainThread) {}
}
