package core.level;

import contrib.entities.MiscFactory;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;

import java.util.List;

public class CookingPotTestLevel extends DungeonLevel {

  private static final int CP_INDEX_COOKING_POT = 0;

  /**
   * Constructor for the CookingPotTestLevel.
   *
   * @param layout the tile layout
   * @param designLabel the design label
   * @param customPoints list of important coordinates in the level
   */
  public CookingPotTestLevel (
    LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "CookingPotTestLevel");
  }

  /**
   * Called once when the level is loaded. Used to spawn entities and setup level-specific features.
   */
  @Override
  public void onFirstTick() {
    addCookingPot();
  }

  /** Called every frame. Used to update dynamic level logic like doors opening. */
  @Override
  public void onTick() {}

  private void addCookingPot() {
    Game.add(MiscFactory.cookingPot(getCustomPoint(CP_INDEX_COOKING_POT)));
  }

  /**
   * Retrieves the centered point of a custom coordinate index.
   *
   * @param index index into the customPoints list
   * @return centered world point for that index
   * @throws IndexOutOfBoundsException if index is invalid
   */
  private Point getCustomPoint(int index) {
    return customPoints.get(index).toCenteredPoint();
  }
}
