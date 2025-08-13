package coopDungeon.level;

import contrib.entities.MiscFactory;
import core.Game;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

/**
 * The second level of the Coop Dungeon.
 *
 * <p>The players must use catapults to jump over pits and work together to push a crate onto a
 * pressure plate to open the way to the exit.
 */
public class Level02 extends DungeonLevel {

  /**
   * Creates a new Level02.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public Level02(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 2");
  }

  @Override
  protected void onFirstTick() {
    spawnCatapults();
  }

  private void spawnCatapults() {
    Game.add(
        MiscFactory.catapult(
            customPoints().get(0).toCenteredPoint(), customPoints().get(1).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(1).toCenteredPoint()));
    Game.add(
        MiscFactory.catapult(
            customPoints().get(2).toCenteredPoint(), customPoints().get(3).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(3).toCenteredPoint()));
    Game.add(
        MiscFactory.catapult(
            customPoints().get(3).toCenteredPoint(), customPoints().get(4).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(4).toCenteredPoint()));
  }

  @Override
  protected void onTick() {}
}
