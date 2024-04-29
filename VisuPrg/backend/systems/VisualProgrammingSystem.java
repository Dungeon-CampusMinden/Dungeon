package systems;

import components.ValueComponent;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import tasks.VisuTask;

public class VisualProgrammingSystem extends System {

  private final BlockingQueue<VisuTask> tasks = new LinkedBlockingQueue<>();
  private VisuTask currentTask;
  private Map<String, Entity> variableChests = new HashMap<>();

  public VisualProgrammingSystem() {
    super(ValueComponent.class, PlayerComponent.class);
  }

  @Override
  public void execute() {
    if (!tasks.isEmpty() && currentTask == null) {
      currentTask = tasks.element();
    } else if (currentTask != null) {
      currentTask.execute();
    }
  }

  public void setTaskDone() {
    if (tasks.remove(currentTask)) {
      currentTask = null;
    }
  }

  public void addVariable(Entity variableChest) {
    variableChests.put(variableChest.name(), variableChest);
  }

  public void addTask(ArrayList<VisuTask> taskList) {
    for (VisuTask task : taskList) {
      tasks.add(task);
    }
  }

  public Entity getVariableChest(String variableName) {
    return variableChests.get(variableName);
  }

  public int getVariableValue(String variableName) {
    Entity chest = variableChests.get(variableName);
    ValueComponent vc =
        chest
            .fetch(ValueComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, ValueComponent.class));
    return vc.getValue();
  }
}
