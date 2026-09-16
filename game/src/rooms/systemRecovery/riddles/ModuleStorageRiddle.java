package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.hud.DialogUtils;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import java.util.List;
import rooms.systemRecovery.entities.ModuleEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 2: initialize sockets, populate modules, remove the GPU and read the array length.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class ModuleStorageRiddle {
  private static final String[] MODULE_NAMES = {"CPU", "RAM", "GPU", "SSD", "NETWORK"};

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;

  private final Entity[] moduleSockets = new Entity[5];
  private final Entity[] moduleChips = new Entity[5];
  private final Point[] moduleSocketPoints = new Point[5];
  private Entity moduleDisplay;
  private String moduleDisplayText;
  private boolean completed;
  private boolean lengthInspected;
  private boolean moduleChipsPorted;

  /**
   * Creates the riddle for the owning level.
   *
   * @param level level that owns the module entities
   */
  public ModuleStorageRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /**
   * Creates the riddle with callbacks for physical success and failure events.
   *
   * @param level level that owns the module entities
   * @param callbacks success, failure and completion callbacks
   */
  public ModuleStorageRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
    this.moduleDisplayText = SystemRecoveryText.key("world.module.display-pending");
  }

  /** Creates the room objects and the exit keypad. */
  public void setup() {
    setupRoomThreeKeypad();
    for (int index = 0; index < 5; index++) {
      moduleSocketPoints[index] = level.getPoint("s" + index);
      moduleSockets[index] = ModuleEntityFactory.moduleSocket(moduleSocketPoints[index]);
      Game.add(moduleSockets[index]);
    }

    moduleDisplay =
        SystemRecoveryDisplayFactory.moduleDisplay(
            level.getPoint("display_room2"), () -> moduleDisplayText, this::inspectModuleDisplay);
    Game.add(moduleDisplay);
  }

  /** Activates all module sockets after the module array is initialized. */
  public void activateModuleSockets() {
    for (Entity socket : moduleSockets) {
      ModuleEntityFactory.activateModuleSocket(socket);
    }
  }

  /** Shows the available modules and their required array indices after the battery is inserted. */
  public void showModuleAssignments() {
    moduleDisplayText = SystemRecoveryText.key("world.module.display-values");
    if (moduleDisplay != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(moduleDisplay, moduleDisplayText);
    }
  }

  /** Spawns the five module chips on their corresponding sockets. */
  public void spawnModuleChips() {
    if (moduleChips[0] != null) return;

    for (int index = 0; index < MODULE_NAMES.length; index++) {
      ModuleEntityFactory.occupyModuleSocket(moduleSockets[index], MODULE_NAMES[index]);
      moduleChips[index] =
          ModuleEntityFactory.moduleChip(moduleSocketPoints[index], MODULE_NAMES[index]);
      Game.add(moduleChips[index]);
    }
  }

  /** Removes the defective GPU chip from the third socket. */
  public void removeGpuChip() {
    if (moduleChips[2] != null) {
      Game.remove(moduleChips[2]);
      moduleChips[2] = null;
      ModuleEntityFactory.clearModuleSocket(moduleSockets[2]);
    }
  }

  /**
   * Transfers the module chips to the inventory scanner.
   *
   * <p>The old entities are removed before fresh entities are spawned at the scanner points. This
   * prevents stale room-two entities and collider state from surviving the network teleport.
   */
  public void portModuleChipsToScanner() {
    if (moduleChipsPorted) return;
    moduleChipsPorted = true;

    for (int index = 0; index < moduleChips.length; index++) {
      Entity moduleChip = moduleChips[index];
      if (moduleChip == null) {
        continue;
      }
      Point target = RiddleSupport.point(level, "scanner" + index, "scanner_" + index);
      Game.remove(moduleChip);
      ModuleEntityFactory.clearModuleSocket(moduleSockets[index]);
      moduleChips[index] = ModuleEntityFactory.moduleChip(target, MODULE_NAMES[index]);
      Game.add(moduleChips[index]);
    }
  }

  /** Updates the room display with the module array length. */
  public void showModuleArrayLength() {
    if (completed) return;
    moduleDisplayText = SystemRecoveryText.key("world.module.display-length", 5);
    completed = true;
    callbacks.solved();
    if (moduleDisplay != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(moduleDisplay, moduleDisplayText);
    }
  }

  /**
   * Returns whether the module-array puzzle has reached its final terminal step.
   *
   * @return {@code true} after {@code module.length;} was accepted
   */
  public boolean completed() {
    return completed;
  }

  /**
   * Returns whether a player has examined the display after the array length was shown.
   *
   * @return {@code true} after the display interaction has been completed
   */
  public boolean lengthInspected() {
    return lengthInspected;
  }

  private void inspectModuleDisplay(Entity display, Entity player) {
    if (completed) callbacks.success("inspect-length", player.id());
    else callbacks.failure("inspect-length", player.id());
    DialogUtils.showTextPopup(
        display.fetch(DisplayTextComponent.class).orElseThrow().text(),
        SystemRecoveryText.key("world.module.display-title"),
        player.id());
    if (completed && !lengthInspected) {
      lengthInspected = true;
      SystemRecoveryLevel.announceStoryForPlayer(
          SystemRecoveryStoryDialogs.OPEN_SCANNER_DOOR, player.id());
    }
  }

  private void setupRoomThreeKeypad() {
    DoorTile scannerDoor =
        (DoorTile) Game.tileAt(level.getPoint("door_inventarscanner")).orElseThrow();
    boolean[] openedForExpectedStep = {false};
    Entity keypad =
        KeypadFactory.createKeypad(
            level.getPoint("room3_keypad"),
            List.of(5),
            () -> {
              if (SystemRecoveryProgressNet.activeStep().orElse(null)
                      != SystemRecoveryLearningStep.ROOM2_DOOR_CODE
                  || scannerDoor.isOpen()) {
                return;
              }
              scannerDoor.open();
              if (!scannerDoor.isOpen()) return;
              if (SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ROOM2_DOOR_CODE)) {
                openedForExpectedStep[0] = true;
              } else {
                scannerDoor.close();
              }
            },
            true);
    keypad
        .fetch(KeypadComponent.class)
        .ifPresent(
            component -> {
              component.onCorrectCode(
                  player -> {
                    if (openedForExpectedStep[0]) {
                      callbacks.success("5", player.id());
                      return;
                    }
                    component.isUnlocked(false);
                    component.enteredDigits().clear();
                    callbacks.failure("5-out-of-order", player.id());
                  });
              component.onWrongCode(
                  player -> callbacks.failure(component.enteredString(), player.id()));
            });
    Game.add(keypad);
  }
}
