package coopDungeon.level;

import contrib.components.CatapultableComponent;
import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.utils.components.lever.BooleanOperations;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;

/**
 * The second level of the Coop Dungeon.
 *
 * <p>The players must use catapults to jump over pits and work together to push a crate onto a
 * pressure plate to open the way to the exit.
 */
public class Level02 extends DungeonLevel {
  private static final int DELAY_MILLIS = 1000;
  private static final float CRATE_MASS = 1f;
  private LeverComponent p, p2, l1, l2;
  private ExitTile exit;
  private DoorTile door1, door2;

  /**
   * Creates a new Level02.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public Level02(LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Coop 2");
  }

  @Override
  protected void onFirstTick() {
    spawnCatapults();
    setupCrateRiddle();
    setupExitLevers();

    door1 = (DoorTile) Game.tileAt(getPoint(12)).orElse(null);
    door2 = (DoorTile) Game.tileAt(getPoint(13)).orElse(null);
    door1.close();
    door2.close();
    exit = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    exit.close();
  }

  private void spawnCatapults() {
    Game.add(MiscFactory.catapult(getPoint(0), getPoint(1), 10f));
    Game.add(MiscFactory.marker(getPoint(1)));
    Game.add(MiscFactory.catapult(getPoint(4), getPoint(5), 10f));
    Game.add(MiscFactory.marker(getPoint(5)));

    Game.add(MiscFactory.catapult(getPoint(2), getPoint(3), 10f));
    Game.add(MiscFactory.marker(getPoint(3)));

    Game.add(MiscFactory.catapult(getPoint(8), getPoint(9), 10f));
    Game.add(MiscFactory.marker(getPoint(9)));
  }

  private void setupCrateRiddle() {
    Entity plate = LeverFactory.pressurePlate(getPoint(7), CRATE_MASS);
    p = plate.fetch(LeverComponent.class).get();
    Game.add(plate);
    Entity plate2 = LeverFactory.pressurePlate(getPoint(14));
    p2 = plate2.fetch(LeverComponent.class).get();
    Game.add(plate2);
    Entity crate = MiscFactory.crate(getPoint(6), CRATE_MASS);
    crate.add(new CatapultableComponent(entity -> {}, entity -> {}));
    Game.add(crate);
  }

  private void setupExitLevers() {
    Entity lever1 = LeverFactory.createTimedLever(getPoint(10), DELAY_MILLIS);
    Entity lever2 = LeverFactory.createTimedLever(getPoint(11), DELAY_MILLIS);
    Game.add(lever1);
    Game.add(lever2);
    l1 = lever1.fetch(LeverComponent.class).get();
    l2 = lever2.fetch(LeverComponent.class).get();
  }

  @Override
  protected void onTick() {
    if (BooleanOperations.and(l1, l2)) exit.open();
    if (p.isOn()) door1.open();
    else door1.close();
    if (p2.isOn()) door2.open();
    else door2.close();
  }
}
