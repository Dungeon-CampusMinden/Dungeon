package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.hud.DialogUtils;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.tasks.TaskComponent;
import rooms.systemRecovery.entities.ModuleEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoverySounds;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;
import tracking.core.TrackingInteractionStatus;

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
  private static final String SCANNER_DOOR_CODE = "0" + MODULE_CAPACITY + "0" + DEFECTIVE_GPU_INDEX;

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
   * @return {@code true} after the module length was sent to the display
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
    SystemRecoveryPuzzleEvents.interaction(
        SystemRecoveryPuzzle.MODULE_STORAGE,
        "module-display",
        "inspect-length",
        TrackingInteractionStatus.COMPLETED,
        completed ? "display-ready" : "display-unready",
        player);
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
    Entity keypad =
        KeypadFactory.createKeypad(level.getPoint("room3_keypad"), SCANNER_DOOR_CODE, true);
    keypad
        .fetch(KeypadComponent.class)
        .ifPresent(
            component -> {
              TaskComponent<?> task = keypad.fetch(TaskComponent.class).orElseThrow();
              task.onCorrect(
                  player -> {
                    if (openDoorForStep(scannerDoor, SystemRecoveryLearningStep.ROOM2_DOOR_CODE)) {
                      callbacks.success("0502", player.id());
                      return;
                    }
                    SystemRecoveryPuzzleEvents.interaction(
                        SystemRecoveryPuzzle.MODULE_STORAGE,
                        "scanner-keypad",
                        "submit-pin",
                        TrackingInteractionStatus.BLOCKED,
                        "out-of-order",
                        player);
                    task.setSolved(false);
                    component.clear();
                  });
              task.onWrong(player -> callbacks.failure(component.display(), player.id()));
            });
    Game.add(keypad);
  }

  /**
   * Opens a keypad door, but only while its learning step is active.
   *
   * @param door the door to open
   * @param step the learning step the door belongs to
   * @return true if the door was opened and the step completed
   */
  private static boolean openDoorForStep(DoorTile door, SystemRecoveryLearningStep step) {
    if (SystemRecoveryProgressNet.activeStep().orElse(null) != step || door.isOpen()) {
      return false;
    }
    door.open();
    if (!door.isOpen()) return false;
    if (!SystemRecoveryProgressNet.complete(step)) {
      door.close();
      return false;
    }
    SystemRecoverySounds.doorOpened();
    return true;
  }
}
