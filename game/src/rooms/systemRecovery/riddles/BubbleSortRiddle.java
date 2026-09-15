package rooms.systemRecovery.riddles;

import static rooms.systemRecovery.riddles.RiddleSupport.moveSortEntity;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.items.ItemKey;
import feature.systems.EventScheduler;
import java.util.List;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 6: consume a programmed USB stick, sort conveyor packages and award the archive key.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class BubbleSortRiddle {
  private final DungeonLevel level;
  private final TransportStorageRiddle transport;
  private final RiddleCallbacks callbacks;
  private static final String SCANNER_SOUND = "retro_beep_01";
  private boolean sortMachineRunning;
  private boolean completed;

  /** Returns whether the conveyor sort has finished successfully. */
  public boolean completed() {
    return completed;
  }

  private final int[] sortBeltValues = {15, 40, 20, 60, 30};
  private final Entity[] sortBeltPackages = new Entity[sortBeltValues.length];
  private Point[] sortBeltPoints;
  private int sortBeltOuterIndex;
  private int sortBeltInnerIndex;
  private int sortMachinePlayerId;

  /** Creates the riddle for the owning level and its shared conveyor dependency. */
  public BubbleSortRiddle(DungeonLevel level, TransportStorageRiddle transport) {
    this(level, transport, RiddleCallbacks.noop());
  }

  /** Creates the machine with callbacks for insertion success and failure. */
  public BubbleSortRiddle(
      DungeonLevel level, TransportStorageRiddle transport, RiddleCallbacks callbacks) {
    this.level = level;
    this.transport = transport;
    this.callbacks = callbacks;
  }

  /** Spawns the machine that accepts a programmed sort chip. */
  public void setup() {
    Game.add(
        EntityFactory.bubbleSortMachine(
            level.getPoint("sort_machine"), this::onBubbleSortMachineInteract));
  }

  /** Returns whether a conveyor sorting run is in progress. */
  public boolean running() {
    return sortMachineRunning;
  }

  private void onBubbleSortMachineInteract(Entity machine, Entity player) {
    if (sortMachineRunning) {
      callbacks.failure("insert", player.id());
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.sort.machine-running"),
          SystemRecoveryText.key("world.sort.machine-title"),
          player.id());
      return;
    }
    SortProgramStickItem programmedStick =
        player
            .fetch(InventoryComponent.class)
            .flatMap(
                inventory ->
                    java.util.Arrays.stream(inventory.items())
                        .filter(SortProgramStickItem.class::isInstance)
                        .map(SortProgramStickItem.class::cast)
                        .filter(SortProgramStickItem::programmed)
                        .findFirst())
            .orElse(null);
    if (programmedStick == null) {
      callbacks.failure("missing-program", player.id());
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.sort.missing-program"),
          SystemRecoveryText.key("world.sort.machine-title"),
          player.id());
      return;
    }
    DialogFactory.showMultipleChoiceDialog(
        SystemRecoveryText.key("world.sort.insert-prompt"),
        SystemRecoveryText.key("world.sort.insert-title"),
        List.of(
            ChoiceOption.of(SystemRecoveryText.key("world.sort.insert"), "insert"),
            ChoiceOption.of(SystemRecoveryText.key("world.sort.cancel"), "cancel")),
        false,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String choice))
              || !"insert".equals(choice)) {
            callbacks.failure("cancel", player.id());
            return;
          }
          // Another player may have started the machine while this dialog was open.
          if (sortMachineRunning) {
            callbacks.failure("insert", player.id());
            DialogUtils.showTextPopup(
                SystemRecoveryText.key("world.sort.machine-running"),
                SystemRecoveryText.key("world.sort.machine-title"),
                player.id());
            return;
          }
          boolean removed =
              player
                  .fetch(InventoryComponent.class)
                  .flatMap(inventory -> inventory.remove(programmedStick))
                  .isPresent();
          if (!removed) {
            callbacks.failure("insert", player.id());
            DialogUtils.showTextPopup(
                SystemRecoveryText.key("world.sort.program-not-in-inventory"),
                SystemRecoveryText.key("world.sort.insert-title"),
                player.id());
            return;
          }
          sortMachinePlayerId = player.id();
          callbacks.success("insert", player.id());
          startTransportBubbleSort();
        },
        () -> {},
        player.id());
  }

  /** Starts the bubble-sort machine on the transport packages, not on the puzzle Cryo-Boxes. */
  private void startTransportBubbleSort() {
    System.arraycopy(new int[] {15, 40, 20, 60, 30}, 0, sortBeltValues, 0, sortBeltValues.length);
    sortBeltPoints = new Point[sortBeltValues.length];
    for (int index = 0; index < sortBeltValues.length; index++) {
      sortBeltPoints[index] = level.getPoint("band" + index);
      if (sortBeltPackages[index] != null) {
        Game.remove(sortBeltPackages[index]);
      }
      sortBeltPackages[index] =
          EntityFactory.transportPackage(sortBeltPoints[index], sortBeltValues[index]);
      Game.add(sortBeltPackages[index]);
    }
    sortBeltOuterIndex = 0;
    sortBeltInnerIndex = 0;
    sortMachineRunning = true;
    if (transport.scanner() != null) {
      Game.remove(transport.scanner());
    }
    transport.replaceScanner(
        EntityFactory.transportScanner(sortBeltPoints[0].translate(0, 1), 1.4f));
    transport.scanner().name("sort_belt_scanner");
    Game.add(transport.scanner());
    runTransportBubbleSortStep();
  }

  private void runTransportBubbleSortStep() {
    if (!sortMachineRunning) return;
    if (sortBeltOuterIndex >= sortBeltValues.length - 1) {
      sortMachineRunning = false;
      completed = true;
      callbacks.solved();
      awardArchiveKey();
      SystemRecoveryLevel.announceStoryToAllPlayers(SystemRecoveryStoryDialogs.ARCHIVE_INTRO);
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.sort.complete"),
          SystemRecoveryText.key("world.sort.machine-title"),
          sortMachinePlayerId);
      return;
    }

    Entity leftEntity = sortBeltPackages[sortBeltInnerIndex];
    Entity rightEntity = sortBeltPackages[sortBeltInnerIndex + 1];
    Point leftPoint = sortBeltPoints[sortBeltInnerIndex];
    Point rightPoint = sortBeltPoints[sortBeltInnerIndex + 1];
    moveSortEntity(transport.scanner(), leftPoint.translate(0, 1));
    Game.audio().playGlobal(SoundSpec.builder(SCANNER_SOUND));

    EventScheduler.scheduleAction(
        () -> {
          if (sortBeltValues[sortBeltInnerIndex] > sortBeltValues[sortBeltInnerIndex + 1]) {
            int value = sortBeltValues[sortBeltInnerIndex];
            sortBeltValues[sortBeltInnerIndex] = sortBeltValues[sortBeltInnerIndex + 1];
            sortBeltValues[sortBeltInnerIndex + 1] = value;
            sortBeltPackages[sortBeltInnerIndex] = rightEntity;
            sortBeltPackages[sortBeltInnerIndex + 1] = leftEntity;
            moveSortEntity(rightEntity, leftPoint);
            moveSortEntity(leftEntity, rightPoint);
          }
          sortBeltInnerIndex++;
          if (sortBeltInnerIndex >= sortBeltValues.length - 1 - sortBeltOuterIndex) {
            sortBeltOuterIndex++;
            sortBeltInnerIndex = 0;
          }
          EventScheduler.scheduleAction(this::runTransportBubbleSortStep, 700L);
        },
        700L);
  }

  /** Adds the archive key to the player who started the bubble-sort machine. */
  private void awardArchiveKey() {
    Game.findEntityById(sortMachinePlayerId)
        .flatMap(player -> player.fetch(InventoryComponent.class))
        .ifPresent(inventory -> inventory.add(new ItemKey()));
  }

  /**
   * Returns the IDs of the two packages and the scanner currently active on the conveyor.
   *
   * @return left package, right package and scanner IDs; {@code -1} when idle
   */
  public int[] currentBeltSortEntityIds() {
    if (!sortMachineRunning
        || sortBeltPackages[sortBeltInnerIndex] == null
        || sortBeltPackages[sortBeltInnerIndex + 1] == null
        || transport.scanner() == null) {
      return new int[] {-1, -1, -1};
    }
    return new int[] {
      sortBeltPackages[sortBeltInnerIndex].id(),
      sortBeltPackages[sortBeltInnerIndex + 1].id(),
      transport.scanner().id()
    };
  }

  /** Returns all active conveyor package IDs paired with their authoritative weights. */
  public String currentBeltPackageMetadata() {
    StringBuilder metadata = new StringBuilder();
    for (int index = 0; index < sortBeltPackages.length; index++) {
      Entity packageEntity = sortBeltPackages[index];
      if (packageEntity == null) continue;
      if (metadata.length() > 0) metadata.append(',');
      metadata.append(packageEntity.id()).append(':').append(sortBeltValues[index]);
    }
    return metadata.toString();
  }
}
