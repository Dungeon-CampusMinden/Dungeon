package de.fwatermann.dungine.state;

import de.fwatermann.dungine.utils.Then;
import de.fwatermann.dungine.window.GameWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadStepper {

  private final GameWindow window;

  public LoadStepper(GameWindow window) {
    this.window = window;
  }

  private final StepResults results = new StepResults(this);
  protected final Map<String, Integer> stepMap = new HashMap<>();
  private final List<Step> steps = new ArrayList<>();
  private Step done;
  private int currentStep = 0;

  //
  // WITH RESULT CONTEXT + RETURN VALUE
  //

  public LoadStepper step(String id, boolean mainThread, StepFunction.IWithResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step(step, mainThread));
    return this;
  }

  public LoadStepper step(String id, StepFunction.IWithResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step(step, false));
    return this;
  }

  public LoadStepper step(boolean mainThread, StepFunction.IWithResults<?> step) {
    this.steps.add(new Step(step, mainThread));
    return this;
  }

  public LoadStepper step(StepFunction.IWithResults<?> step) {
    this.steps.add(new Step(step, false));
    return this;
  }

  //
  // NO RESULT CONTEXT + RETURN VALUE
  //

  public LoadStepper step(String id, boolean mainThread, StepFunction.IWithoutResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> step.run(), mainThread));
    return this;
  }

  public LoadStepper step(String id, StepFunction.IWithoutResults<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> step.run(), false));
    return this;
  }

  public LoadStepper step(boolean mainThread, StepFunction.IWithoutResults<?> step) {
    this.steps.add(new Step((r) -> step.run(), mainThread));
    return this;
  }

  public LoadStepper step(StepFunction.IWithoutResults<?> step) {
    this.steps.add(new Step((r) -> step.run(), false));
    return this;
  }

  //
  // WITH RESULT CONTEXT + NO RETURN VALUE
  //

  public LoadStepper step(String id, boolean mainThread, StepFunction.IVoidWithResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> {
      step.run(r);
      return null;
    }, mainThread));
    return this;
  }

  public LoadStepper step(String id, StepFunction.IVoidWithResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> {
      step.run(r);
      return null;
    }, false));
    return this;
  }

  public LoadStepper step(boolean mainThread, StepFunction.IVoidWithResults step) {
    this.steps.add(new Step((r) -> {
      step.run(r);
      return null;
    }, mainThread));
    return this;
  }

  public LoadStepper step(StepFunction.IVoidWithResults step) {
    this.steps.add(new Step((r) -> {
      step.run(r);
      return null;
    }, false));
    return this;
  }

  //
  // NO RESULT CONTEXT + NO RETURN VALUE
  //

  public LoadStepper step(String id, boolean mainThread, StepFunction.IVoidWithoutResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> {
      step.run();
      return null;
    }, mainThread));
    return this;
  }

  public LoadStepper step(String id, StepFunction.IVoidWithoutResults step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step((r) -> {
      step.run();
      return null;
    }, false));
    return this;
  }

  public LoadStepper step(boolean mainThread, StepFunction.IVoidWithoutResults step) {
    this.steps.add(new Step((r) -> {
      step.run();
      return null;
    }, mainThread));
    return this;
  }

  public LoadStepper step(StepFunction.IVoidWithoutResults step) {
    this.steps.add(new Step((r) -> {
      step.run();
      return null;
    }, false));
    return this;
  }

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

  public void start() {
    this.next();
  }

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

  private record Step(StepFunction.IWithResults<?> func, boolean mainThread) {}
}
