package core.level.loader;

import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Tuple;
import java.util.List;

/**
 * A basic implementation of the {@link TileLevel} to be used as a manager for the {@link
 * DungeonLoader#addLevel(Tuple[])} operation.
 *
 * <p>This manager will do nothing.
 */
public final class BasicDungeonLevel extends TileLevel {
  /**
   * Constructs a new DevDungeonLevel with the given layout, design label, and custom points.
   *
   * @param layout The layout of the level, represented as a 2D array of LevelElements.
   * @param designLabel The design label of the level.
   * @param customPoints A list of custom points to be added to the level.
   */
  public BasicDungeonLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Default Dungeon", "Default description");
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      onFirstTick();
    } else {
      onTick();
    }
  }

  @Override
  protected void onFirstTick() {}

  @Override
  protected void onTick() {}
}
