package rooms.systemRecovery.riddles;

import static rooms.systemRecovery.riddles.RiddleSupport.portraitPathFor;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.skills.SkillTools;
import feature.systems.EventScheduler;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;

/**
 * Riddle 4: create conveyor packages and collect them in array order.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class TransportStorageRiddle {
  private final DungeonLevel level;

  private static final String SCANNER_SOUND = "retro_beep_01";
  private static final Vector2 TRANSPORT_SCANNER_OFFSET = Vector2.of(-1, 1);
  private final Entity[] transportPackages = new Entity[5];
  private Entity transportScanner;
  private boolean transportPackagesSpawned = false;
  private boolean transportRunning = false;
  private boolean transportCompleted = false;
  private static final int SCANNER_TRAVEL_STEPS = 16;
  private static final long SCANNER_TRAVEL_STEP_MS = 180L;
  private static final long SCANNER_COLLECTION_WAIT_MS = 900L;
  private static final long SCANNER_PACKAGE_INTERVAL_MS =
      SCANNER_TRAVEL_STEPS * SCANNER_TRAVEL_STEP_MS + SCANNER_COLLECTION_WAIT_MS + 300L;

  /** Creates the riddle for the owning level. */
  public TransportStorageRiddle(DungeonLevel level) {
    this.level = level;
  }

  /** Spawns the shared terminal, collection scanner and tile-based conveyor. */
  public void setup() {
    Entity terminal = DecoFactory.createDeco(level.getPoint("lager_terminal"), Deco.DeskWithPC1);
    terminal.name("lager_terminal");
    terminal.remove(DecoComponent.class);
    SystemRecoveryComputerFactory.attachComputerDialog(terminal);
    Game.add(terminal);

    transportScanner =
        EntityFactory.transportScanner(
            level.getPoint("band_start").translate(TRANSPORT_SCANNER_OFFSET));
    Game.add(transportScanner);
    Point start = level.getPoint("band_start");
    Point end = transportEndPoint();
    int segmentCount = Math.round(Math.abs(end.x() - start.x()));
    float direction = Math.signum(end.x() - start.x());
    for (int index = 0; index <= segmentCount; index++) {
      Game.add(EntityFactory.conveyorSegment(start.translate(direction * index, -0.25f)));
    }
  }

  private Point transportEndPoint() {
    try {
      return level.getPoint("band_ende");
    } catch (RuntimeException ignored) {
      return level.getPoint("baned_end");
    }
  }

  /** Spawns the packages for transport riddle four exactly once. */
  public void spawnTransportPackages() {
    if (transportPackagesSpawned) return;
    int[] weights = {15, 40, 20, 60, 30};
    for (int index = 0; index < weights.length; index++) {
      transportPackages[index] =
          EntityFactory.transportPackage(level.getPoint("band" + index), weights[index]);
      Game.add(transportPackages[index]);
    }
    transportPackagesSpawned = true;
  }

  /** Starts the authoritative conveyor animation for transport riddle four. */
  public void startTransportSequence() {
    if (!transportPackagesSpawned || transportRunning || transportScanner == null) return;
    transportRunning = true;
    for (int index = 0; index < transportPackages.length; index++) {
      final int packageIndex = index;
      Entity packageEntity = transportPackages[packageIndex];
      Point packageTarget =
          level.getPoint("band" + packageIndex).translate(TRANSPORT_SCANNER_OFFSET);
      long startDelay = packageIndex * SCANNER_PACKAGE_INTERVAL_MS;
      EventScheduler.scheduleAction(
          () ->
              moveScannerToPackage(
                  packageEntity,
                  packageTarget,
                  SCANNER_TRAVEL_STEPS,
                  packageIndex == transportPackages.length - 1),
          startDelay);
    }
    EventScheduler.scheduleAction(
        () -> transportRunning = false,
        transportPackages.length * SCANNER_PACKAGE_INTERVAL_MS + SCANNER_COLLECTION_WAIT_MS);
  }

  private void moveScannerToPackage(
      Entity packageEntity, Point target, int steps, boolean lastPackage) {
    if (packageEntity == null || transportScanner == null) return;
    transportScanner
        .fetch(engine.components.PositionComponent.class)
        .ifPresent(
            position -> {
              Point current = position.position();
              position.position(current.translate(current.vectorTo(target).scale(1f / steps)));
            });
    if (steps > 1) {
      EventScheduler.scheduleAction(
          () -> moveScannerToPackage(packageEntity, target, steps - 1, lastPackage),
          SCANNER_TRAVEL_STEP_MS);
    } else {
      Game.audio().playGlobal(SoundSpec.builder(SCANNER_SOUND));
      SkillTools.blink(packageEntity, 0x00FFFFFF, SCANNER_COLLECTION_WAIT_MS, 3);
      EventScheduler.scheduleAction(
          () -> {
            Game.remove(packageEntity);
            if (lastPackage) {
              completeTransportSequence();
            }
          },
          SCANNER_COLLECTION_WAIT_MS);
    }
  }

  private void completeTransportSequence() {
    if (transportCompleted) return;
    transportCompleted = true;
    ((DoorTile) Game.tileAt(level.getPoint("door_datenspeicher")).get()).open();
    Game.player()
        .ifPresent(
            player ->
                DialogFactory.showDialogDialog(
                    "Alle Pakete sind eingesammelt. Der Zugang zum Datenarchiv ist jetzt geöffnet.",
                    portraitPathFor(player),
                    () -> {},
                    player.id()));
  }

  /** Returns the conveyor scanner shared with riddle 6. */
  Entity scanner() {
    return transportScanner;
  }

  /** Replaces the scanner reference when riddle 6 needs a wider scanner. */
  void replaceScanner(Entity scanner) {
    transportScanner = scanner;
  }
}
