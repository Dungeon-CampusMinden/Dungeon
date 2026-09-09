package rooms.systemRecovery.riddles;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.shader.OutlineShader;
import feature.components.DecoComponent;
import feature.entities.LeverFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.keypad.KeypadFactory;
import feature.skills.SkillTools;
import feature.systems.EventScheduler;
import feature.utils.ICommand;
import java.util.List;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;

/**
 * Riddle 3: unlock the scanner and visualize counting the remaining modules.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class InventoryScannerRiddle {
  private final DungeonLevel level;

  private static final String SCANNER_SOUND = "retro_beep_01";
  private static final Vector2 SCANNER_OFFSET = Vector2.of(-1, 1);
  private boolean scannerPuzzleSolved = false;
  private boolean scannerRunning = false;
  private boolean scannerCompleted = false;
  private Entity scannerEntity;
  private Entity scannerDisplay;

  /** Creates the riddle for the owning level. */
  public InventoryScannerRiddle(DungeonLevel level) {
    this.level = level;
  }

  /** Creates the room objects and the exit keypad. */
  public void setup() {
    setupTransportStorageKeypad();
    scannerEntity =
        EntityFactory.moduleScanner(level.getPoint("scanner0").translate(SCANNER_OFFSET));
    Game.add(scannerEntity);

    scannerDisplay =
        EntityFactory.moduleDisplay(
            level.getPoint("scanner_display"), () -> "Scanner: Noch nicht aktiviert");
    Game.add(scannerDisplay);

    Game.add(
        LeverFactory.createLever(
            level.getPoint("scanner_lever"),
            new ICommand() {
              @Override
              public void execute() {
                if (!scannerPuzzleSolved || scannerRunning || scannerCompleted) {
                  return;
                }
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
    Point scannerPoint = level.getPoint("scanner" + index);
    scannerEntity
        .fetch(engine.components.PositionComponent.class)
        .ifPresent(position -> position.position(scannerPoint.translate(SCANNER_OFFSET)));
    Game.audio().playGlobal(SoundSpec.builder(SCANNER_SOUND));
    Game.entityAtPoint(scannerPoint)
        .forEach(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.shaders().add("moduleScanner", new OutlineShader(2, Color.CYAN));
                          EventScheduler.scheduleAction(
                              () -> {
                                draw.shaders().remove("moduleScanner");
                                if (index == 2 && "module_gpu".equals(entity.name())) {
                                  SkillTools.blink(entity, 0xFF0000FF, 600, 3);
                                  EventScheduler.scheduleAction(
                                      () -> draw.tintColor(0xFF0000FF), 600);
                                }
                              },
                              450);
                        }));
  }

  private void completeModuleScan() {
    scannerRunning = false;
    scannerCompleted = true;
    EntityFactory.updateDisplayText(scannerDisplay, "4");
  }

  private void setupTransportStorageKeypad() {
    Game.add(
        KeypadFactory.createKeypad(
            level.getPoint("keypad_transportlager"),
            List.of(4),
            () -> ((DoorTile) Game.tileAt(level.getPoint("door_transportlager")).get()).open(),
            true));
  }
}
