package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.interaction.keypad.KeypadFactory;
import feature.systems.PositionSync;
import java.util.List;
import rooms.systemRecovery.entities.EntityFactory;

/**
 * Riddle 2: initialize sockets, populate modules, remove the GPU and read the array length.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class ModuleStorageRiddle {
  private final DungeonLevel level;

  private final Entity[] moduleSockets = new Entity[5];
  private final Entity[] moduleChips = new Entity[5];
  private final Point[] moduleSocketPoints = new Point[5];
  private Entity moduleDisplay;
  private String moduleDisplayText = "Array length: Noch nicht bestimmt";

  /** Creates the riddle for the owning level. */
  public ModuleStorageRiddle(DungeonLevel level) {
    this.level = level;
  }

  /** Creates the room objects and the exit keypad. */
  public void setup() {
    setupRoomThreeKeypad();
    for (int index = 0; index < 5; index++) {
      moduleSocketPoints[index] = level.getPoint("s" + index);
      moduleSockets[index] = EntityFactory.moduleSocket(moduleSocketPoints[index]);
      Game.add(moduleSockets[index]);
    }

    moduleDisplay =
        EntityFactory.moduleDisplay(level.getPoint("display_room2"), () -> moduleDisplayText);
    Game.add(moduleDisplay);
  }

  /** Activates all module sockets after the module array is initialized. */
  public void activateModuleSockets() {
    for (Entity socket : moduleSockets) {
      EntityFactory.activateModuleSocket(socket);
    }
  }

  /** Spawns the five module chips on their corresponding sockets. */
  public void spawnModuleChips() {
    String[] modules = {"CPU", "RAM", "GPU", "SSD", "NETWORK"};
    for (int index = 0; index < modules.length; index++) {
      EntityFactory.occupyModuleSocket(moduleSockets[index], modules[index]);
      moduleChips[index] = EntityFactory.moduleChip(moduleSocketPoints[index], modules[index]);
      Game.add(moduleChips[index]);
    }
  }

  /** Removes the defective GPU chip from the third socket. */
  public void removeGpuChip() {
    if (moduleChips[2] != null) {
      Game.remove(moduleChips[2]);
      moduleChips[2] = null;
      EntityFactory.clearModuleSocket(moduleSockets[2]);
    }
  }

  /** Moves the module chips to the inventory scanner without moving their sockets. */
  public void portModuleChipsToScanner() {
    for (int index = 0; index < moduleChips.length; index++) {
      Entity moduleChip = moduleChips[index];
      if (moduleChip == null) {
        continue;
      }
      Point target = level.getPoint("scanner" + index);
      moduleChip
          .fetch(PositionComponent.class)
          .ifPresent(
              position -> {
                position.position(target);
                PositionSync.syncPosition(moduleChip);
              });
    }
  }

  /** Updates the room display with the module array length. */
  public void showModuleArrayLength() {
    moduleDisplayText = "Array length: 5";
    EntityFactory.updateDisplayText(moduleDisplay, moduleDisplayText);
  }

  private void setupRoomThreeKeypad() {
    Game.add(
        KeypadFactory.createKeypad(
            level.getPoint("room3_keypad"),
            List.of(5),
            () -> ((DoorTile) Game.tileAt(level.getPoint("door_inventarscanner")).get()).open(),
            true));
  }
}
