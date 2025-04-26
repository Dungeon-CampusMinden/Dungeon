package level;

import contrib.hud.DialogUtils;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.game.ECSManagment;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.CameraSystem;
import core.utils.Point;

import java.util.List;

public class AiMazeLevel extends BlocklyLevel {
  private static boolean showText = true;

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
    super(layout, designLabel, customPoints, "AiMaze");
  }

  @Override
  protected void onFirstTick() {
    float x = Game.currentLevel().layout()[0].length/2.0f;
    float y = Game.currentLevel().layout().length/2.0f;
    Entity cameraFocusPoint = new Entity();
    cameraFocusPoint.add(new PositionComponent(x + 0.5f, y + 0.25f));
    cameraFocusPoint.add(new CameraComponent());
    Debugger.ZOOM_CAMERA(0.20f);
    Game.add(cameraFocusPoint);
  }

  @Override
  protected void onTick() {}
}
