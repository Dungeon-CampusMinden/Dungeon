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

  public LoadStepper step(IStepFunction<?> step) {
    this.steps.add(new Step(step, false));
    return this;
  }

  public LoadStepper step(boolean mainThread, IStepFunction<?> step) {
    this.steps.add(new Step(step, mainThread));
    return this;
  }

  public LoadStepper step(String id, boolean mainThread, IStepFunction<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step(step, mainThread));
    return this;
  }

  public LoadStepper step(String id, IStepFunction<?> step) {
    this.stepMap.put(id, this.steps.size());
    this.steps.add(new Step(step, false));
    return this;
  }

  public LoadStepper done(boolean mainThread, IStepFunction<Void> step) {
    this.done = new Step(step, mainThread);
    return this;
  }

  public void start() {
    this.next();
  }

  private void next() {
    if (this.steps.isEmpty() || this.currentStep >= this.steps.size()) { // Done
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
                "LoadStepperAsync")
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
              "LoadStepperAsync")
          .start();
    }
  }

  private record Step(IStepFunction<?> func, boolean mainThread) {}
}
