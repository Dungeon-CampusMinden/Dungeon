package tasks;

import systems.VisualProgrammingSystem;

public class VisuTask {
  private final String message;
  protected final VisualProgrammingSystem visualProgrammingSystem;

  public VisuTask(String message, VisualProgrammingSystem visualProgrammingSystem) {
    this.message = message;
    this.visualProgrammingSystem = visualProgrammingSystem;
  }

  public void execute() {}
}
