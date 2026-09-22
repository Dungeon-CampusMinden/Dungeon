package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.DecoComponent;
import feature.entities.LeverFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.systems.EventScheduler;
import feature.utils.ICommand;
import java.util.List;
import rooms.systemRecovery.entities.ScannerEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 3: unlock the scanner and visualize counting the remaining modules.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class InventoryScannerRiddle {
  private static final List<Integer> TRANSPORT_DOOR_CODE =
      List.of(0, ModuleStorageRiddle.MODULE_CAPACITY, 0, ModuleStorageRiddle.OCCUPIED_MODULE_COUNT);

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;

  private static final String SCANNER_SOUND = "retro_beep_01";
  private static final Vector2 SCANNER_OFFSET = Vector2.of(-1, 1);
  private boolean scannerPuzzleSolved = false;
  private boolean scannerRunning = false;
  private boolean scannerCompleted = false;
  private int currentScanIndex = -1;
  private boolean scannerFaultDetected = false;

  /**
   * Returns whether the visual module scan has finished.
   *
   * @return whether the scanner riddle is complete
   */
  public boolean completed() {
    return scannerCompleted;
  }

  /**
   * Returns the module currently being examined by the scanner.
   *
   * @return the zero-based module index, or {@code -1} while the scanner is idle
   */
  public int currentScanIndex() {
    return scannerRunning ? currentScanIndex : -1;
  }

  /**
   * @return whether the GPU fault was detected during the scan
   */
  public boolean scannerFaultDetected() {
    return scannerFaultDetected;
  }

  /**
   * @return whether the scanner is currently moving across the module row
   */
  public boolean running() {
    return scannerRunning;
  }

  private Entity scannerEntity;
  private Entity scannerDisplay;

  /**
   * Creates the riddle for the owning level.
   *
   * @param level level that owns the scanner entities
   */
  public InventoryScannerRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /**
   * Creates the riddle with callbacks for physical success and failure events.
   *
   * @param level level that owns the scanner entities
   * @param callbacks success, failure and completion callbacks
   */
  public InventoryScannerRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
  }

  /** Creates the room objects and the exit keypad. */
  public void setup() {
    setupTransportStorageKeypad();
    scannerEntity =
        ScannerEntityFactory.moduleScanner(
            level.getPoint("scanner0").translate(SCANNER_OFFSET), 1f);
    Game.add(scannerEntity);

    scannerDisplay =
        SystemRecoveryDisplayFactory.hintDisplay(
            level.getPoint("scanner_display"),
            () -> SystemRecoveryText.key("world.scanner.display-pending"),
            SystemRecoveryText.key("world.scanner.title"));
    Game.add(scannerDisplay);

    Game.add(
        LeverFactory.createLever(
            level.getPoint("scanner_lever"),
            new ICommand() {
              @Override
              public void execute() {
                if (!scannerPuzzleSolved || scannerRunning || scannerCompleted) {
                  callbacks.failure("lever", -1);
                  return;
                }
                callbacks.success("lever", -1);
                startModuleScan();
              }

              @Override
              public void undo() {}
            }));

    Entity scannerTerminal =
        DecoFactory.createDeco(level.getPoint("scanner_terminal"), Deco.DeskWithPC1);
    scannerTerminal.name("scanner_terminal");
    scannerTerminal.remove(DecoComponent.class);
    SystemRecoveryComputerFactory.attachComputerDialog(scannerTerminal);
    Game.add(scannerTerminal);
  }

  /** Enables the scanner lever after the inventory scanner code has been solved. */
  public void completeScannerPuzzle() {
    if (scannerPuzzleSolved) return;
    scannerPuzzleSolved = true;
  }

  /** Restores the completed scanner state without replaying its animation or callbacks. */
  public void restoreCompletedState() {
    scannerPuzzleSolved = true;
    scannerCompleted = true;
    scannerRunning = false;
    currentScanIndex = -1;
    scannerFaultDetected = true;
    if (scannerDisplay != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(
          scannerDisplay,
          SystemRecoveryText.key(
              "world.scanner.display-complete",
              ModuleStorageRiddle.MODULE_CAPACITY,
              ModuleStorageRiddle.OCCUPIED_MODULE_COUNT));
    }
  }

  private void startModuleScan() {
    scannerRunning = true;
    for (int index = 0; index < ModuleStorageRiddle.MODULE_CAPACITY; index++) {
      int scannerIndex = index;
      EventScheduler.scheduleAction(() -> highlightScannerModule(scannerIndex), index * 600L);
    }
    EventScheduler.scheduleAction(this::completeModuleScan, 5 * 600L);
  }

  private void highlightScannerModule(int index) {
    currentScanIndex = index;
    if (index == 2) scannerFaultDetected = true;
    Point scannerPoint = level.getPoint("scanner" + index);
    scannerEntity
        .fetch(engine.components.PositionComponent.class)
        .ifPresent(position -> position.position(scannerPoint.translate(SCANNER_OFFSET)));
    Game.audio().playGlobal(SoundSpec.builder(SCANNER_SOUND));
  }

  private void completeModuleScan() {
    if (scannerCompleted) return;
    scannerRunning = false;
    currentScanIndex = -1;
    scannerCompleted = true;
    callbacks.solved();
    SystemRecoveryDisplayFactory.updateDisplayText(
        scannerDisplay,
        SystemRecoveryText.key(
            "world.scanner.display-complete",
            ModuleStorageRiddle.MODULE_CAPACITY,
            ModuleStorageRiddle.OCCUPIED_MODULE_COUNT));
    SystemRecoveryLevel.announceStoryToAllPlayers(SystemRecoveryStoryDialogs.SCANNER_COMPLETE);
  }

  private void setupTransportStorageKeypad() {
    DoorTile transportDoor =
        (DoorTile) Game.tileAt(level.getPoint("door_transportlager")).orElseThrow();
    boolean[] openedForExpectedStep = {false};
    Entity keypad =
        KeypadFactory.createKeypad(
            level.getPoint("keypad_transportlager"),
            TRANSPORT_DOOR_CODE,
            () -> {
              if (SystemRecoveryProgressNet.activeStep().orElse(null)
                      != SystemRecoveryLearningStep.ROOM3_DOOR_CODE
                  || transportDoor.isOpen()) {
                return;
              }
              transportDoor.open();
              if (!transportDoor.isOpen()) return;
              if (SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ROOM3_DOOR_CODE)) {
                openedForExpectedStep[0] = true;
              } else {
                transportDoor.close();
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
                      callbacks.success("0504", player.id());
                      return;
                    }
                    component.isUnlocked(false);
                    component.enteredDigits().clear();
                    callbacks.failure("0504-out-of-order", player.id());
                  });
              component.onWrongCode(
                  player -> callbacks.failure(component.enteredString(), player.id()));
            });
    Game.add(keypad);
  }
}
