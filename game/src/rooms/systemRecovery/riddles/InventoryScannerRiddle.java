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
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 3: unlock the scanner and visualize counting the remaining modules.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class InventoryScannerRiddle {
  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;

  private static final String SCANNER_SOUND = "retro_beep_01";
  private static final Vector2 SCANNER_OFFSET = Vector2.of(-1, 1);
  private boolean scannerPuzzleSolved = false;
  private boolean scannerRunning = false;
  private boolean scannerCompleted = false;
  private int currentScanIndex = -1;
  private boolean scannerFaultDetected = false;

  /** Returns whether the visual module scan has finished. */
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

  /** @return whether the GPU fault was detected during the scan */
  public boolean scannerFaultDetected() {
    return scannerFaultDetected;
  }

  /** @return whether the scanner is currently moving across the module row */
  public boolean running() {
    return scannerRunning;
  }

  private Entity scannerEntity;
  private Entity scannerDisplay;

  /** Creates the riddle for the owning level. */
  public InventoryScannerRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /** Creates the riddle with callbacks for physical success and failure events. */
  public InventoryScannerRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
  }

  /** Creates the room objects and the exit keypad. */
  public void setup() {
    setupTransportStorageKeypad();
    scannerEntity =
        EntityFactory.moduleScanner(level.getPoint("scanner0").translate(SCANNER_OFFSET));
    Game.add(scannerEntity);

    scannerDisplay =
        EntityFactory.hintDisplay(
            level.getPoint("scanner_display"),
            () -> SystemRecoveryText.text("world.scanner.display-pending"),
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

  private void startModuleScan() {
    scannerRunning = true;
    for (int index = 0; index < 5; index++) {
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
    EntityFactory.updateDisplayText(
        scannerDisplay, SystemRecoveryText.text("world.scanner.display-complete"));
  }

  private void setupTransportStorageKeypad() {
    Entity keypad =
        KeypadFactory.createKeypad(
            level.getPoint("keypad_transportlager"),
            List.of(4),
            () -> ((DoorTile) Game.tileAt(level.getPoint("door_transportlager")).get()).open(),
            true);
    keypad
        .fetch(KeypadComponent.class)
        .ifPresent(
            component -> {
              component.onCorrectCode(player -> callbacks.success("4", player.id()));
              component.onWrongCode(
                  player -> callbacks.failure(component.enteredString(), player.id()));
            });
    Game.add(keypad);
  }
}
