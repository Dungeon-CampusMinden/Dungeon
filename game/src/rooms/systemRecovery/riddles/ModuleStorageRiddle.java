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
  // Door codes encode two zero-padded two-digit fields: capacity, then the puzzle result.
  static final int MODULE_CAPACITY = MODULE_NAMES.length;
  static final int DEFECTIVE_GPU_INDEX = 2;
  static final int OCCUPIED_MODULE_COUNT = MODULE_CAPACITY - 1;
  private static final List<Integer> SCANNER_DOOR_CODE =
      List.of(0, MODULE_CAPACITY, 0, DEFECTIVE_GPU_INDEX);

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;

  private final Entity[] moduleSockets = new Entity[MODULE_CAPACITY];
  private final Entity[] moduleChips = new Entity[MODULE_CAPACITY];
  private final Point[] moduleSocketPoints = new Point[MODULE_CAPACITY];
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
    for (int index = 0; index < MODULE_CAPACITY; index++) {
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
    if (moduleChips[DEFECTIVE_GPU_INDEX] != null) {
      Game.remove(moduleChips[DEFECTIVE_GPU_INDEX]);
      moduleChips[DEFECTIVE_GPU_INDEX] = null;
      ModuleEntityFactory.clearModuleSocket(moduleSockets[DEFECTIVE_GPU_INDEX]);
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
    moduleDisplayText =
        SystemRecoveryText.key("world.module.display-length", MODULE_CAPACITY, DEFECTIVE_GPU_INDEX);
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

  /** Restores the module puzzle's completed physical state without callbacks or dialogs. */
  public void restoreCompletedState() {
    activateModuleSockets();
    showModuleAssignments();
    spawnModuleChips();
    removeGpuChip();
    moduleChipsPorted = false;
    portModuleChipsToScanner();
    moduleDisplayText =
        SystemRecoveryText.key("world.module.display-length", MODULE_CAPACITY, DEFECTIVE_GPU_INDEX);
    completed = true;
    lengthInspected = true;
    if (moduleDisplay != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(moduleDisplay, moduleDisplayText);
    }
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
            SCANNER_DOOR_CODE,
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
                      callbacks.success("0502", player.id());
                      return;
                    }
                    component.isUnlocked(false);
                    component.enteredDigits().clear();
                    callbacks.failure("0502-out-of-order", player.id());
                  });
              component.onWrongCode(
                  player -> callbacks.failure(component.enteredString(), player.id()));
            });
    Game.add(keypad);
  }
}
