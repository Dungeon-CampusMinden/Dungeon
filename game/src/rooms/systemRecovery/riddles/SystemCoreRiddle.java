package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import rooms.systemRecovery.entities.SortingEntityFactory;
import rooms.systemRecovery.entities.SystemCoreEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 10: restore the three central data-processing routines.
 *
 * <p>The terminal interpreter validates the source code. This controller owns only the physical
 * presentation of the three tasks and the shared display. Keeping this state here means the
 * interpreter does not need to know about computers, containers, map cells or doors.
 */
public final class SystemCoreRiddle {
  private static final int[] SORT_VALUES = {42, 17, 8, 31, 23};
  private static final String[] MODULE_VALUES = {"CPU", "GPU", "RAM", null, null};
  private static final int COMPLETE_TINT = 0x66FF66FF;

  private final DungeonLevel level;
  private final SearchRobotRiddle searchRobot;
  private final Entity[] sortEntries = new Entity[SORT_VALUES.length];
  private final Entity[] moduleEntries = new Entity[MODULE_VALUES.length];
  private final Set<Entity> mapEntries = new HashSet<>();
  private SearchRobotMatrix map;
  private Entity display;
  private int stage;
  private boolean exitOpen;
  private boolean mapSearchStarted;

  /**
   * Creates the central-computer riddle for the owning level.
   *
   * @param level level that owns the system-core entities
   */
  public SystemCoreRiddle(DungeonLevel level) {
    this.level = level;
    this.searchRobot = null;
  }

  /**
   * Creates the central-computer riddle with the shared search robot.
   *
   * @param level level that owns the system-core entities
   * @param searchRobot dedicated controller for the second robot used by the final matrix scan
   */
  public SystemCoreRiddle(DungeonLevel level, SearchRobotRiddle searchRobot) {
    this.level = level;
    this.searchRobot = searchRobot;
  }

  /** Spawns the dedicated central terminal, the shared display and all three task areas. */
  public void setup() {
    spawnTerminal();
    spawnDisplay();
    spawnSortArea();
    spawnModuleArea();
    spawnMapArea();
    if (searchRobot != null) {
      searchRobot.setupSystemCoreRobot(map.pointAt(0));
    }
  }

  /** Marks the Bubble Sort area as complete and advances the shared display. */
  public void completeSort() {
    if (stage >= 1) return;
    stage = 1;
    tint(sortEntries);
    updateDisplay();
  }

  /** Marks the module-count area as complete and advances the shared display. */
  public void completeModuleCount() {
    if (stage < 1 || stage >= 2) return;
    stage = 2;
    tint(moduleEntries);
    updateDisplay();
  }

  /** Marks the map-search area as complete and updates the shared display. */
  public void completeMapSearch() {
    if (stage < 2 || stage >= 3) return;
    stage = 3;
    tint(mapEntries);
    updateDisplay();
  }

  /**
   * Starts the shared search robot on the System Core matrix.
   *
   * <p>The terminal code only authorizes this run. The actual learning step is completed by the
   * robot callback after it has reached every cell, so the final input mask cannot appear early.
   *
   * @param onComplete callback invoked after the robot has scanned all cells
   * @return whether a new scan was started
   */
  public boolean startMapSearch(Runnable onComplete) {
    if (stage < 2 || stage >= 3 || mapSearchStarted || searchRobot == null || map == null) {
      return false;
    }
    if (!searchRobot.startSystemCoreScan(map, onComplete)) return false;
    mapSearchStarted = true;
    updateDisplay();
    return true;
  }

  /** Completes the meta-riddle and opens the route beyond the system core. */
  public void complete() {
    if (stage < 3 || stage >= 4) return;
    stage = 4;
    updateDisplay();
  }

  /** Updates the display after ECHO has opened the route to the elevator. */
  public void markExitOpen() {
    if (stage < 4 || exitOpen) return;
    exitOpen = true;
    updateDisplay();
  }

  /**
   * @return whether all three routines and the final combination have been accepted
   */
  public boolean completed() {
    return stage >= 4;
  }

  /**
   * Returns the completed section count used by the synchronized visual state.
   *
   * @return {@code 0} before the first section, {@code 1} after sorting, {@code 2} after module
   *     counting, {@code 3} after the matrix search and {@code 4} after the meta-riddle
   */
  public int stage() {
    return stage;
  }

  /**
   * Checks the final result payload against the values produced by the three central areas.
   *
   * @param payload compact payload from the system-core input mask
   * @return whether all three displayed results are correct
   */
  public boolean acceptsMetaInput(String payload) {
    return SystemCoreMetaInput.parse(payload)
        .map(
            input -> input.matches(sortedEnergyValues(), activeModuleCount(), scannedModuleCount()))
        .orElse(false);
  }

  private void spawnTerminal() {
    Entity terminal = DecoFactory.createDeco(level.getPoint("core_terminal"), Deco.DeskWithPC1);
    terminal.name("core_terminal");
    terminal.remove(feature.components.DecoComponent.class);
    SystemRecoveryComputerFactory.attachComputerDialog(terminal);
    Game.add(terminal);
  }

  private void spawnDisplay() {
    display =
        SystemRecoveryDisplayFactory.hintDisplay(
            level.getPoint("core_display"),
            this::displayText,
            SystemRecoveryText.key("world.system-core.title"));
    display.name("core_display");
    Game.add(display);
    updateDisplay();
  }

  private void spawnSortArea() {
    for (int index = 0; index < SORT_VALUES.length; index++) {
      Entity entry =
          SortingEntityFactory.dataCrystal(level.getPoint("b" + index), SORT_VALUES[index]);
      entry.name("system_core_sort_" + index);
      sortEntries[index] = entry;
      Game.add(entry);
    }
  }

  private void spawnModuleArea() {
    for (int index = 0; index < MODULE_VALUES.length; index++) {
      Entity entry =
          SystemCoreEntityFactory.moduleEntry(
              level.getPoint("mod" + index), index, MODULE_VALUES[index]);
      moduleEntries[index] = entry;
      Game.add(entry);
    }
  }

  private void spawnMapArea() {
    map = SearchRobotMatrix.between(level.getPoint("map00"), level.getPoint("map24"));
    for (int row = 0; row < map.rows(); row++) {
      for (int column = 0; column < map.columns(); column++) {
        Entity cell = SystemCoreEntityFactory.mapCell(map.pointAt(row, column), row, column);
        mapEntries.add(cell);
        Game.add(cell);
      }
    }
  }

  private String displayText() {
    return switch (stage) {
      case 0 -> SystemRecoveryText.key("world.system-core.display-sort");
      case 1 -> SystemRecoveryText.key("world.system-core.display-count");
      case 2 -> SystemRecoveryText.key("world.system-core.display-search");
      case 3 -> metaDisplayText();
      default ->
          SystemRecoveryText.key(
              "world.system-core." + (exitOpen ? "display-complete" : "display-awaiting-exit"));
    };
  }

  private String metaDisplayText() {
    String sortedValues =
        sortedEnergyValues().stream().map(String::valueOf).collect(Collectors.joining(" "));
    return SystemRecoveryText.key(
        "world.system-core.display-meta", sortedValues, activeModuleCount(), scannedModuleCount());
  }

  private static List<Integer> sortedEnergyValues() {
    return Arrays.stream(SORT_VALUES).sorted().boxed().toList();
  }

  private static int activeModuleCount() {
    return (int) Arrays.stream(MODULE_VALUES).filter(value -> value != null).count();
  }

  private int scannedModuleCount() {
    return map == null ? 0 : map.size();
  }

  private void updateDisplay() {
    if (display != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(display, displayText());
    }
  }

  private void tint(Entity[] entities) {
    for (Entity entity : entities) {
      entity.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(COMPLETE_TINT));
    }
  }

  private void tint(Set<Entity> entities) {
    entities.forEach(
        entity ->
            entity.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(COMPLETE_TINT)));
  }
}
