package coopDungeon.level;

import contrib.components.CatapultableComponent;
import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.utils.components.lever.BooleanOperations;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import java.util.Set;

/**
 * The second level of the Coop Dungeon.
 *
 * <p>The players must use catapults to jump over pits and work together to push a crate onto a
 * pressure plate to open the way to the exit.
 */
public class Level02 extends DungeonLevel {

  private LeverComponent p, l1, l2;
  private ExitTile exit;
  private Set<Tile> doorSet;

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

    doorSet = Game.allTiles(LevelElement.DOOR);
    doorSet.forEach(tile -> ((DoorTile) tile).close());
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
    Entity crate = MiscFactory.crate(customPoints.get(6).toCenteredPoint());
    crate.add(new CatapultableComponent(entity -> {}, entity -> {}));
    Game.add(crate);
    Entity plate = LeverFactory.pressurePlate(customPoints.get(7).toCenteredPoint());
    p = plate.fetch(LeverComponent.class).get();
    Game.add(plate);
  }

  private void setupExitLevers() {
    Entity lever1 = LeverFactory.createLever(customPoints.get(10).toCenteredPoint());
    Entity lever2 = LeverFactory.createLever(customPoints.get(11).toCenteredPoint());
    Game.add(lever1);
    Game.add(lever2);
    l1 = lever1.fetch(LeverComponent.class).get();
    l2 = lever2.fetch(LeverComponent.class).get();
  }

  @Override
  protected void onTick() {
    if (BooleanOperations.and(l1, l2)) exit.open();
    if (p.isOn()) doorSet.forEach(tile -> ((DoorTile) tile).open());
    else doorSet.forEach(tile -> ((DoorTile) tile).close());
  }
}
