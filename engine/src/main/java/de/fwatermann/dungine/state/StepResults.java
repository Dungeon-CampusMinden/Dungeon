package de.fwatermann.dungine.state;

import java.util.HashMap;
import java.util.Map;

public class StepResults {

  private LoadStepper stepper;

  protected StepResults(LoadStepper stepper) {
    this.stepper = stepper;
  }

  private Map<Integer, Object> results = new HashMap<>();

  public <T> T result(int step) {
    Object result = this.results.getOrDefault(step, null);
    if(result == null) return null;
    return (T) result;
  }

  public <T> T result(String id) {
    int index = this.stepper.stepMap.getOrDefault(id, -1);
    if(index == -1) return null;
    return this.result(index);
  }

  protected void setResult(int step, Object result) {
    this.results.put(step, result);
  }

}
