package level;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

import java.util.List;

/**
 * A level that uses blockly. This class is abstract and should be extended by specific levels.
 *
 * <p>
 * This class extends the {@link DevDungeonLevel} class and provides a framework for creating, it overrides the DevDungeon
 * specific `onTick` method to provide a more general implementation.
 *
 * @see DevDungeonLevel
 */
public abstract class BlocklyLevel extends DevDungeonLevel {

  /**
   * Create a new blockly level.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   * @param name The name of the level.
   */
  public BlocklyLevel(
    LevelElement[][] layout,
    DesignLabel designLabel,
    List<Coordinate> customPoints,
    String name) {
    super(layout, designLabel, customPoints, name, "");
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      onFirstTick();
    } else {
      onTick();
    }
  }
}
