package rooms.systemRecovery.riddles;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.LevelUtils;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.ai.AIUtils;
import feature.components.CollideComponent;
import feature.components.InventoryComponent;
import feature.entities.WorldItemBuilder;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongSupplier;
import rooms.systemRecovery.entities.ScannerEntityFactory;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 9 and the final System Core scan: program the search chip for the first matrix, then run a
 * dedicated second robot through the core's 3x5 matrix after the central program is accepted.
 *
 * <p>The scan is authoritative. Clients receive the robot position and the current cell index via
 * the normal entity snapshot plus System Recovery metadata.
 */
public final class SearchRobotRiddle {
  /** Mutually exclusive movement phases of one search robot. */
  private enum Phase {
    IDLE,
    SCANNING,
    WAITING_AT_CELL,
    DELIVERING,
    COMPLETED
  }

  private static final long SCAN_INTERVAL_MS = 750L;
  private static final int DELIVERY_SEARCH_RADIUS = 4;
  private static final float WAYPOINT_TOLERANCE = 0.05f;

  /** Cyan highlight shown while the robot examines one matrix cell. */
  public static final int SCAN_TINT = 0x33D9FFFF;

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;
  private final LongSupplier clock;
  private SearchRobotMatrix matrix;
  private Entity robot;
  private Entity searchTarget;
  private Entity deliveredAccessChip;
  private int scanIndex = -1;
  private GraphPath<Tile> currentPath;
  private int pathCursor;
  private int pathTargetIndex = -1;
  private Point deliveryDestination;
  private Point itemSpawnPoint;
  private long scanResumeAt;
  private Point highlightedCell;
  private int highlightedCellOriginalTint = -1;
  private Phase phase = Phase.IDLE;
  private boolean systemCoreScan;
  private boolean systemCoreScanCompleted;
  private boolean systemCoreRobotWasSolid;
  private Runnable systemCoreScanCompletion = () -> {};

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
    this(level, callbacks, TimeUtils::millis);
  }

  SearchRobotRiddle(DungeonLevel level, RiddleCallbacks callbacks, LongSupplier clock) {
    this.level = level;
    this.callbacks = callbacks;
    this.clock = clock;
  }

  /** Spawns the robot, controller and one target item at the matrix end point. */
  public void setup() {
    matrix =
        SearchRobotMatrix.between(
            RiddleSupport.point(level, "roboter_start"), RiddleSupport.point(level, "roboter_end"));
    searchTarget = ScannerEntityFactory.searchTargetItem(matrix.pointAt(matrix.size() - 1));
    Game.add(searchTarget);

    robot =
        ScannerEntityFactory.searchRobot(
            RiddleSupport.point(level, "suchroboter"), this::followCurrentPath);
    Game.add(robot);

    Entity controller =
        ScannerEntityFactory.searchRobotController(
            RiddleSupport.point(level, "suchroboter_controller", "suchroboter_controlls"),
            this::onControllerInteract);
    Game.add(controller);
  }

  /**
   * Spawns a second, controller-less robot for the System Core matrix.
   *
   * <p>The core robot is deliberately a separate entity and controller instance. It is already
   * placed at the matrix entrance when the level is built, so starting the scan never teleports the
   * Riddle 9 robot or mixes their synchronized highlight state.
   *
   * @param startPoint first cell of the System Core matrix
   */
  public void setupSystemCoreRobot(Point startPoint) {
    if (robot != null) return;
    robot = ScannerEntityFactory.searchRobot(startPoint, this::followCurrentPath);
    // The robot still has a collision box, but it must not be pushed around by the solid scan
    // cells while it is waiting for the core terminal routine to start.
    robot.fetch(CollideComponent.class).ifPresent(collide -> collide.isSolid(false));
    Game.add(robot);
  }

  /**
   * Returns whether this controller owns the given synchronized robot entity.
   *
   * @param candidate entity to check
   * @return whether this controller owns the entity
   */
  public boolean controls(Entity candidate) {
    return robot == candidate;
  }

  /**
   * @return whether a scan is currently running
   */
  public boolean running() {
    return phase == Phase.SCANNING || phase == Phase.WAITING_AT_CELL || phase == Phase.DELIVERING;
  }

  /**
   * @return whether the search robot has delivered the system-core module
   */
  public boolean completed() {
    return phase == Phase.COMPLETED;
  }

  /** Restores a finished search before the system-core access step. */
  public void restoreCompletedState() {
    if (searchTarget != null) {
      Game.remove(searchTarget);
      searchTarget = null;
    }
    phase = Phase.COMPLETED;
  }

  /**
   * @return current scan cell in row-major order, or -1 while idle
   */
  public int currentCellIndex() {
    return running() ? scanIndex : -1;
  }

  /**
   * Returns the matrix cell currently being examined, or {@code null} while the robot is moving to
   * the next cell or delivering the recovered module.
   *
   * @return active scan cell for synchronized visual feedback
   */
  public Point currentCellPoint() {
    return running() && phase != Phase.DELIVERING ? highlightedCell : null;
  }

  private void onControllerInteract(Entity ignored, Entity player) {
    if (running() || completed()) {
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
          if (running() || completed()) {
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

  void startScan() {
    if (running() || completed()) return;
    systemCoreScan = false;
    systemCoreScanCompletion = () -> {};
    phase = Phase.SCANNING;
    scanIndex = 0;
    scanResumeAt = 0L;
    currentPath = null;
    pathCursor = 0;
    pathTargetIndex = -1;
    SystemRecoveryLevel.announceStoryToAllPlayers(SystemRecoveryStoryDialogs.SEARCH_ROBOT_START);
  }

  /**
   * Starts a second, automatic scan for the final System Core riddle.
   *
   * <p>The core scan deliberately has no controller or item delivery step. Its dedicated robot is
   * spawned at the matrix entrance and movement between matrix cells still uses the AISystem path.
   *
   * @param coreMatrix inclusive row-major matrix to scan in the System Core
   * @param onComplete callback invoked once after every cell was reached
   * @return whether the scan was started
   */
  public boolean startSystemCoreScan(SearchRobotMatrix coreMatrix, Runnable onComplete) {
    if (running() || systemCoreScanCompleted || coreMatrix == null || robot == null) return false;
    systemCoreScan = true;
    systemCoreScanCompletion = onComplete == null ? () -> {} : onComplete;
    matrix = coreMatrix;
    robot
        .fetch(VelocityComponent.class)
        .ifPresent(
            velocity -> {
              velocity.currentVelocity(Vector2.ZERO);
              velocity.clearForces();
            });
    robot
        .fetch(CollideComponent.class)
        .ifPresent(
            collide -> {
              systemCoreRobotWasSolid = collide.isSolid();
              collide.isSolid(false);
            });
    phase = Phase.SCANNING;
    scanIndex = 0;
    scanResumeAt = 0L;
    currentPath = null;
    pathCursor = 0;
    pathTargetIndex = -1;
    clearHighlightedCell();
    return true;
  }

  /**
   * Lets the AISystem follow an A* path to the active matrix cell or delivery point.
   *
   * @param entity robot entity being moved
   */
  private void followCurrentPath(Entity entity) {
    if (!running()) return;

    if (phase == Phase.WAITING_AT_CELL) {
      if (clock.getAsLong() < scanResumeAt) return;
      phase = Phase.SCANNING;
      clearHighlightedCell();
      scanIndex++;
      currentPath = null;
    }

    if (phase != Phase.DELIVERING && scanIndex >= matrix.size()) {
      if (systemCoreScan) {
        completeSystemCoreScan();
      } else {
        beginDelivery();
      }
      return;
    }

    boolean delivering = phase == Phase.DELIVERING;
    Point target = delivering ? deliveryDestination : matrix.pointAt(scanIndex);
    int targetIndex = delivering ? -1 : scanIndex;
    PositionComponent position = entity.fetch(PositionComponent.class).orElse(null);
    if (position == null || target == null) return;

    if (currentPath == null
        || pathTargetIndex != targetIndex
        || AIUtils.pathLeft(entity, currentPath)) {
      currentPath = LevelUtils.calculatePath(position.position(), target);
      pathCursor = 0;
      pathTargetIndex = targetIndex;
    }

    VelocityComponent velocity = entity.fetch(VelocityComponent.class).orElse(null);
    if (velocity == null) return;
    if (currentPath.getCount() == 0) {
      velocity.currentVelocity(Vector2.ZERO);
      velocity.clearForces();
      if (Point.inRange(position.position(), target, WAYPOINT_TOLERANCE)) {
        if (delivering) {
          completeDelivery();
        } else {
          highlightCurrentCell();
          waitAtScannedCell();
        }
      }
      return;
    }

    // Path nodes and arrival checks both use PositionComponent's tile origin. AIUtils.followPath
    // uses the collider center instead, which can stop a right/up move one tile too early.
    while (pathCursor < currentPath.getCount()
        && Point.inRange(
            position.position(), currentPath.get(pathCursor).position(), WAYPOINT_TOLERANCE)) {
      pathCursor++;
    }
    if (pathCursor == currentPath.getCount()) {
      velocity.currentVelocity(Vector2.ZERO);
      velocity.clearForces();
      if (delivering) {
        completeDelivery();
      } else {
        highlightCurrentCell();
        waitAtScannedCell();
      }
      return;
    }

    Vector2 toWaypoint = position.position().vectorTo(currentPath.get(pathCursor).position());
    double speed = Math.min(velocity.maxSpeed(), toWaypoint.length() * Game.frameRate());
    velocity.currentVelocity(toWaypoint.normalize().scale(speed));
    velocity.clearForces();
  }

  /** Pauses briefly after the robot enters a cell, then advances the scan program. */
  private void waitAtScannedCell() {
    if (phase == Phase.WAITING_AT_CELL) return;
    phase = Phase.WAITING_AT_CELL;
    scanResumeAt = clock.getAsLong() + SCAN_INTERVAL_MS;
    Game.audio().playGlobal(SoundSpec.builder("retro_beep_01"));
  }

  /** Switches the same AI movement from matrix scanning to the delivery route. */
  private void beginDelivery() {
    if (phase == Phase.DELIVERING) return;
    collectSearchTarget();
    phase = Phase.DELIVERING;
    scanIndex = -1;
    currentPath = null;
    pathCursor = 0;
    pathTargetIndex = -1;
    clearHighlightedCell();
    itemSpawnPoint =
        RiddleSupport.point(level, "roboter_item_destination", "roboter_item_destionation");
    deliveryDestination = findReachableDeliveryPoint(itemSpawnPoint);
  }

  /**
   * Resolves the delivery marker to a tile the robot can actually reach.
   *
   * <p>The item itself is spawned at the requested marker. A level designer may place that marker
   * on a visual transition point such as a hole, which is a valid item position but not a valid
   * movement tile. In that case the robot stops on the nearest reachable tile around the marker,
   * while the item still appears exactly at the configured hand-off point.
   *
   * @param requestedPoint configured item hand-off point
   * @return reachable tile nearest to the requested point
   */
  private Point findReachableDeliveryPoint(Point requestedPoint) {
    Point currentPoint =
        robot
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .orElse(requestedPoint);
    if (hasPath(currentPoint, requestedPoint)) return requestedPoint;

    int originX = Math.round(requestedPoint.x());
    int originY = Math.round(requestedPoint.y());
    for (int radius = 1; radius <= DELIVERY_SEARCH_RADIUS; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
          if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
          Point candidate = new Point(originX + dx, originY + dy);
          if (hasPath(currentPoint, candidate)) return candidate;
        }
      }
    }

    // The robot is already on a reachable tile, so the item can still be delivered instead of
    // leaving the riddle in a permanently running state if the marker is completely surrounded.
    return currentPoint;
  }

  private boolean hasPath(Point from, Point to) {
    return Game.tileAt(to).filter(Tile::isAccessible).isPresent()
        && LevelUtils.calculatePath(from, to).getCount() > 0;
  }

  /** Creates the access module only after the robot has physically reached its destination. */
  private void completeDelivery() {
    if (completed()) return;
    phase = Phase.IDLE;
    currentPath = null;
    clearHighlightedCell();
    Point destination = itemSpawnPoint == null ? deliveryDestination : itemSpawnPoint;
    if (deliveredAccessChip == null) {
      deliveredAccessChip =
          WorldItemBuilder.buildWorldItem(new SystemCoreAccessChipItem(), destination);
      Game.add(deliveredAccessChip);
    }
    // Do not inspect the localized entity name here. It is a translation key on the server and
    // therefore cannot be used as a stable gameplay identifier.
    phase = deliveredAccessChip != null ? Phase.COMPLETED : Phase.IDLE;
    if (completed()) {
      SystemRecoveryLevel.announceStoryToAllPlayers(
          SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE);
      callbacks.solved();
      SystemRecoveryLevel.announceSystemCoreAccessModuleDelivered();
    }
  }

  /** Completes the final matrix scan without consuming or spawning another item. */
  private void completeSystemCoreScan() {
    if (!running() || !systemCoreScan) return;
    phase = Phase.IDLE;
    systemCoreScan = false;
    systemCoreScanCompleted = true;
    scanIndex = -1;
    currentPath = null;
    pathCursor = 0;
    pathTargetIndex = -1;
    clearHighlightedCell();
    robot
        .fetch(CollideComponent.class)
        .ifPresent(collide -> collide.isSolid(systemCoreRobotWasSolid));
    Runnable completion = systemCoreScanCompletion;
    systemCoreScanCompletion = () -> {};
    completion.run();
  }

  private void collectSearchTarget() {
    if (searchTarget == null) return;
    Game.remove(searchTarget);
    searchTarget = null;
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
  }

  private void highlightCurrentCell() {
    Point cell =
        running()
                && phase != Phase.DELIVERING
                && matrix != null
                && scanIndex >= 0
                && scanIndex < matrix.size()
            ? matrix.pointAt(scanIndex)
            : null;
    if (cell == null) {
      clearHighlightedCell();
      return;
    }
    if (cell.equals(highlightedCell)) return;
    clearHighlightedCell();
    Game.tileAt(cell)
        .ifPresent(
            tile -> {
              highlightedCell = cell;
              highlightedCellOriginalTint = tile.tintColor();
              tile.tintColor(SCAN_TINT);
            });
  }

  private void clearHighlightedCell() {
    if (highlightedCell == null) return;
    Point cell = highlightedCell;
    int originalTint = highlightedCellOriginalTint;
    highlightedCell = null;
    highlightedCellOriginalTint = -1;
    Game.tileAt(cell).ifPresent(tile -> tile.tintColor(originalTint));
  }
}
