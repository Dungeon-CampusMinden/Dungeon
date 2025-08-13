package coopDungeon.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.systems.EventScheduler;
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
 * The first level of the coop Dungeon.
 *
 * <p>In this level, the player must use pressure plates to open the door for their partner.
 *
 * <p>To open the final door, they have to time their interaction and trigger two switches at the
 * same time.
 */
public class Level01 extends DungeonLevel {

  public static final int DELAY_MILLIS = 1000;
  private DoorTile door;
  private ExitTile exit;
  private LeverComponent l1, l2, l3, l4;
  private EventScheduler.ScheduledAction a1, a2 = null;

  public Level01(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 1");
  }

  @Override
  protected void onFirstTick() {
    setupLever();
    door = (DoorTile) Game.randomTile(LevelElement.DOOR).get();
    door.close();
    exit = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    exit.close();
  }

  private void setupLever() {
    Entity l = LeverFactory.pressurePlate(customPoints.get(0).toCenteredPoint());
    Game.add(l);
    l1 = l.fetch(LeverComponent.class).get();

    l = LeverFactory.pressurePlate(customPoints.get(1).toCenteredPoint());
    Game.add(l);
    l2 = l.fetch(LeverComponent.class).get();

    l = LeverFactory.createTimedLever(customPoints.get(2).toCenteredPoint(), DELAY_MILLIS);
    l3 = l.fetch(LeverComponent.class).get();
    Game.add(l);
    l = LeverFactory.createTimedLever(customPoints.get(3).toCenteredPoint(), DELAY_MILLIS);
    Game.add(l);
    l4 = l.fetch(LeverComponent.class).get();
  }

  @Override
  protected void onTick() {
    if (BooleanOperations.or(l1, l2)) door.open();
    else door.close();
    if (BooleanOperations.and(l3, l4)) exit.open();
  }
}
