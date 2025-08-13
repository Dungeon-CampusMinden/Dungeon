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
  private static final int DELAY_MILLIS = 1000;
  private LeverComponent p, p2, l1, l2;
  private ExitTile exit;
  private DoorTile door1, door2;

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
    setupCrateRiddle();
    setupExitLevers();

    door1 = (DoorTile) Game.tileAT(customPoints.get(12));
    door2 = (DoorTile) Game.tileAT(customPoints.get(13));
    door1.close();
    door2.close();
    exit = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    exit.close();
  }

  private void spawnCatapults() {
    Game.add(
        MiscFactory.catapult(
            customPoints().get(0).toCenteredPoint(), customPoints().get(1).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(1).toCenteredPoint()));
    Game.add(
        MiscFactory.catapult(
            customPoints().get(4).toCenteredPoint(), customPoints().get(5).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(5).toCenteredPoint()));

    Game.add(
        MiscFactory.catapult(
            customPoints().get(2).toCenteredPoint(), customPoints().get(3).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(3).toCenteredPoint()));

    Game.add(
        MiscFactory.catapult(
            customPoints().get(8).toCenteredPoint(), customPoints().get(9).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(9).toCenteredPoint()));
  }

  private void setupCrateRiddle() {
    Entity plate = LeverFactory.pressurePlate(customPoints.get(7).toCenteredPoint());
    p = plate.fetch(LeverComponent.class).get();
    Game.add(plate);
    Entity plate2 = LeverFactory.pressurePlate(customPoints.get(14).toCenteredPoint());
    p2 = plate2.fetch(LeverComponent.class).get();
    Game.add(plate2);
    Entity crate = MiscFactory.crate(customPoints.get(6).toCenteredPoint());
    crate.add(new CatapultableComponent(entity -> {}, entity -> {}));
    Game.add(crate);
  }

  private void setupExitLevers() {
    Entity lever1 =
        LeverFactory.createTimedLever(customPoints.get(10).toCenteredPoint(), DELAY_MILLIS);
    Entity lever2 =
        LeverFactory.createTimedLever(customPoints.get(11).toCenteredPoint(), DELAY_MILLIS);
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
