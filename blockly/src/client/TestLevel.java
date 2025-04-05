package client;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import level.BlocklyLevel;

import java.util.List;

public class TestLevel extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout       2D array containing the tile layout.
   * @param designLabel  The design label for the level.
   * @param customPoints The custom points of the level.
   * @param name         The name of the level.
   */
  public TestLevel(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints, String name) {
    super(layout, designLabel, customPoints, "TestLevel");
    blockBlock("move", "goToExit");
  }

  /**
   * Called when the level is first ticked.
   *
   * @see #onTick()
   * @see ITickable
   */
  @Override
  protected void onFirstTick() {

  }

  /**
   * Called when the level is ticked.
   *
   * @see #onFirstTick()
   * @see ITickable
   */
  @Override
  protected void onTick() {

  }
}
