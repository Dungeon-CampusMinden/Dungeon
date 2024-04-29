package tasks;

import contrib.entities.EntityFactory;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import systems.VisualProgrammingSystem;

public class CreateVariable extends VisuTask {

  final String variableName;
  final int value;

  public CreateVariable(
      String message,
      VisualProgrammingSystem visualProgrammingSystem,
      String variableName,
      int value) {
    super(message, visualProgrammingSystem);
    this.variableName = variableName;
    this.value = value;
  }

  @Override
  public void execute() {
    // todo - set value if given (is this necessary?)
    Entity chest = null;
    try {
      // todo - dont make position random
      chest = EntityFactory.newChestDummy(PositionComponent.ILLEGAL_POSITION, variableName, value);
      Game.add(chest);
    } catch (Exception e) {
      new RuntimeException(e);
    }
    visualProgrammingSystem.addVariable(chest);
    visualProgrammingSystem.setTaskDone();
  }
}
