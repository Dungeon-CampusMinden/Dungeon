package aiAdvanced.level;

import contrib.level.DevDungeonLevel;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.CameraSystem;
import java.util.List;

/**
 * This class is used in the AiMaze level and adjusts the camera view to properly display the
 * labyrinth.
 */
public class AiMazeLevel extends DevDungeonLevel {
  /**
   * The zoom level of the overview camera. This is used to adjust the camera view to properly
   * display the labyrinth. (default: 0.55f)
   */
  public static float ZOOM_LEVEL = 0.55f;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AiMazeLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "AiMaze", "");
  }

  @Override
  protected void onFirstTick() {
    float x = Game.currentLevel().layout()[0].length / 2.0f;
    float y = Game.currentLevel().layout().length / 2.0f;
    Entity cameraFocusPoint = new Entity();
    cameraFocusPoint.add(new PositionComponent(x + 0.5f, y + 0.25f));
    cameraFocusPoint.add(new CameraComponent());
    CameraSystem.camera().zoom = ZOOM_LEVEL;
    Game.add(cameraFocusPoint);
  }

  @Override
  protected void onTick() {}
}
