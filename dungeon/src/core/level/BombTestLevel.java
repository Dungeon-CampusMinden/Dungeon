package core.level;

import contrib.entities.MiscFactory;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.List;

/** Template, please copy for own class. */
public class BombTestLevel extends DungeonLevel {

  private final List<Coordinate> explodableWallCoordinates;

  /**
   * Constructor for the example level.
   *
   * @param layout the tile layout
   * @param designLabel the design label
   * @param customPoints list of important coordinates in the level
   */
  public BombTestLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "BombTestLevel");
    this.explodableWallCoordinates = customPoints.subList(0, Math.min(1, customPoints.size()));
  }

  /**
   * Called once when the level is loaded. Used to spawn entities and setup level-specific features.
   */
  @Override
  public void onFirstTick() {
    IPath wallTex = new SimpleIPath("objects/explodable_wall/explodable_wall.png");
    for (Coordinate c : explodableWallCoordinates) {
      Entity wall = MiscFactory.explodableWall(wallTex, c);
      Game.add(wall);
    }
  }

  /** Called every frame. Used to update dynamic level logic like doors opening. */
  @Override
  public void onTick() {}
}
