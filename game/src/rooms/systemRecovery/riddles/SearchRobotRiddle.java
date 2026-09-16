package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.entities.WorldItemBuilder;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import feature.systems.EventScheduler;
import java.util.Arrays;
import java.util.List;
import rooms.systemRecovery.entities.ScannerEntityFactory;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 9: program the search chip and let the robot inspect a matrix defined by two corner
 * points.
 *
 * <p>The scan is authoritative. Clients receive the robot position and the current cell index via
 * the normal entity snapshot plus System Recovery metadata.
 */
public final class SearchRobotRiddle {
  private static final long SCAN_INTERVAL_MS = 750L;

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;
  private SearchRobotMatrix matrix;
  private Entity robot;
  private Entity searchTarget;
  private int scanIndex = -1;
  private boolean running;
  private boolean completed;

  /**
   * Creates the search-robot controller for the owning level.
   *
   * @param level level that owns the search-robot entities
   */
  public SearchRobotRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /**
   * Creates the search-robot riddle with callbacks for chip insertion attempts.
   *
   * @param level level that owns the search-robot entities
   * @param callbacks success and failure callbacks
   */
  public SearchRobotRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
  }

  /** Spawns the robot, controller and one target item at the matrix end point. */
  public void setup() {
    matrix =
        SearchRobotMatrix.between(
            RiddleSupport.point(level, "roboter_start"), RiddleSupport.point(level, "roboter_end"));
    searchTarget = ScannerEntityFactory.searchTargetItem(matrix.pointAt(matrix.size() - 1));
    Game.add(searchTarget);

    robot = ScannerEntityFactory.searchRobot(RiddleSupport.point(level, "suchroboter"));
    Game.add(robot);

    Entity controller =
        ScannerEntityFactory.searchRobotController(
            RiddleSupport.point(level, "suchroboter_controller", "suchroboter_controlls"),
            this::onControllerInteract);
    Game.add(controller);
  }

  /**
   * @return whether a scan is currently running
   */
  public boolean running() {
    return running;
  }

  /**
   * @return whether the search robot has delivered the system-core module
   */
  public boolean completed() {
    return completed;
  }

  /**
   * @return current scan cell in row-major order, or -1 while idle
   */
  public int currentCellIndex() {
    return running ? scanIndex : -1;
  }

  private void onControllerInteract(Entity ignored, Entity player) {
    if (running) {
      callbacks.failure("insert", player.id());
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.search.controller-running"),
          SystemRecoveryText.key("world.search.title"),
          player.id());
      return;
    }

    SearchProgramChipItem chip = programmedChip(player);
    if (chip == null) {
      callbacks.failure("missing-program", player.id());
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.search.controller-missing"),
          SystemRecoveryText.key("world.search.title"),
          player.id());
      return;
    }

    DialogFactory.showMultipleChoiceDialog(
        SystemRecoveryText.key("world.search.controller"),
        SystemRecoveryText.key("world.search.title"),
        List.of(
            ChoiceOption.of(SystemRecoveryText.key("computer.insert-search"), "insert"),
            ChoiceOption.of(SystemRecoveryText.key("computer.without-chip"), "cancel")),
        false,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String choice))
              || !"insert".equals(choice)) {
            callbacks.failure("cancel", player.id());
            return;
          }
          if (running) {
            callbacks.failure("insert", player.id());
            return;
          }
          player
              .fetch(InventoryComponent.class)
              .flatMap(inventory -> inventory.remove(chip))
              .ifPresentOrElse(
                  removed -> {
                    callbacks.success("insert", player.id());
                    startScan();
                  },
                  () -> {
                    callbacks.failure("insert", player.id());
                    DialogUtils.showTextPopup(
                        SystemRecoveryText.key("world.search.controller-missing"),
                        SystemRecoveryText.key("world.search.title"),
                        player.id());
                  });
        },
        () -> {},
        player.id());
  }

  private SearchProgramChipItem programmedChip(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .flatMap(
            inventory ->
                Arrays.stream(inventory.items())
                    .filter(SearchProgramChipItem.class::isInstance)
                    .map(SearchProgramChipItem.class::cast)
                    .filter(SearchProgramChipItem::programmed)
                    .findFirst())
        .orElse(null);
  }

  private void startScan() {
    running = true;
    completed = false;
    scanIndex = 0;
    scanNextCell();
  }

  private void scanNextCell() {
    if (!running) return;
    if (scanIndex >= matrix.size()) {
      finishScan();
      return;
    }

    Point target = matrix.pointAt(scanIndex);
    RiddleSupport.moveSortEntity(robot, target);
    Game.audio().playGlobal(SoundSpec.builder("retro_beep_01"));
    EventScheduler.scheduleAction(
        () -> {
          scanIndex++;
          scanNextCell();
        },
        SCAN_INTERVAL_MS);
  }

  private void finishScan() {
    if (completed) return;
    running = false;
    scanIndex = -1;
    collectSearchTarget();
    RiddleSupport.moveSortEntity(
        robot, RiddleSupport.point(level, "roboter_item_destination", "roboter_item_destionation"));
    Point destination =
        RiddleSupport.point(level, "roboter_item_destination", "roboter_item_destionation");
    boolean moduleAlreadyDelivered =
        Game.entityAtPoint(destination)
            .anyMatch(entity -> entity.name().contains("Systemkern-Modul"));
    if (!moduleAlreadyDelivered) {
      Game.add(WorldItemBuilder.buildWorldItem(new SystemCoreAccessChipItem(), destination));
    }
    completed =
        Game.entityAtPoint(destination)
            .anyMatch(entity -> entity.name().contains("Systemkern-Modul"));
    if (completed) callbacks.solved();
  }

  private void collectSearchTarget() {
    if (searchTarget == null) return;
    Game.remove(searchTarget);
    searchTarget = null;
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
  }
}
