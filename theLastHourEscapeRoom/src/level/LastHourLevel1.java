package level;

import contrib.components.DecoComponent;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.keypad.KeypadFactory;
import contrib.systems.EventScheduler;
import core.Game;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import mushRoom.Sounds;

import java.util.*;

/** The MushRoom. */
public class LastHourLevel1 extends DungeonLevel {

  private DoorTile storageDoor;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public LastHourLevel1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "last-hour-1");
  }

  @Override
  protected void onFirstTick() {
    Game.levelEntities(Set.of(DecoComponent.class))
        .forEach(
            e -> {
              e.remove(InteractionComponent.class);
            });

    storageDoor = (DoorTile) tileAt(getPoint("door-storage")).orElseThrow();
    storageDoor.close();

    DoorTile entryDoor = (DoorTile) tileAt(getPoint("door-entry")).orElseThrow();
    entryDoor.close();

    Game.add(KeypadFactory.createKeypad(getPoint("keypad-storage"), List.of(1, 2, 3, 4), () -> {
      storageDoor.open();
    }, true));

    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  @Override
  protected void onTick() {

  }

  /**
   * Plays ambient sounds at random intervals.
   */
  private void playAmbientSound() {
    //TODO: Copied from MushRoom, use different sounds.
    double r = Math.random();
    if (r < 0.20) {
//      Sounds.TREE_AMBIENT_CREAK.play();
    } else if (r < 0.40) {
//      Sounds.ANIMAL_AMBIENT.play();
    } else if (r < 0.60) {
//      Sounds.random(Sounds.WIND_AMBIENT_1, Sounds.WIND_AMBIENT_2, Sounds.WIND_AMBIENT_3);
    }

    EventScheduler.scheduleAction(this::playAmbientSound, (long) (Math.random() * 10000 + 10000));
  }
}
