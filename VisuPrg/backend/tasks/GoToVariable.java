package tasks;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.game.ECSManagment;
import core.level.Tile;
import core.level.utils.LevelUtils;
import systems.VisualProgrammingSystem;

public class GoToVariable extends VisuTask {
  private final String variableName;
  private GraphPath<Tile> currentPath;
  private Entity chest;

  public GoToVariable(
      String message, VisualProgrammingSystem visualProgrammingSystem, String variableName) {
    super(message, visualProgrammingSystem);
    this.variableName = variableName;
    chest = visualProgrammingSystem.getVariableChest(variableName);
  }

  @Override
  public void execute() {
    Entity hero = ECSManagment.hero().get();
    currentPath = LevelUtils.calculatePathFromHero(chest);
    if (currentPath != null && !AIUtils.pathFinished(hero, currentPath)) {
      if (AIUtils.pathLeft(hero, currentPath)) {
        currentPath = LevelUtils.calculatePathFromHero(chest);
      }
      AIUtils.move(ECSManagment.hero().get(), currentPath);
    }

    if (currentPath != null && AIUtils.pathFinished(hero, currentPath)) {
      visualProgrammingSystem.setTaskDone();
    }
  }
}
