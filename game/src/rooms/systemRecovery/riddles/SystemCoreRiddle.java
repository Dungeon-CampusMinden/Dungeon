package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import java.util.HashSet;
import java.util.Set;
import rooms.systemRecovery.entities.EntityFactory;
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
  private static final Set<String> BATTERY_CELLS = Set.of("0_2", "1_3", "2_1");
  private static final int COMPLETE_TINT = 0x66FF66FF;

  private final DungeonLevel level;
  private final Entity[] sortEntries = new Entity[SORT_VALUES.length];
  private final Entity[] moduleEntries = new Entity[MODULE_VALUES.length];
  private final Set<Entity> mapEntries = new HashSet<>();
  private SearchRobotMatrix map;
  private Entity display;
  private int stage;

  /** Creates the central-computer riddle for the owning level. */
  public SystemCoreRiddle(DungeonLevel level) {
    this.level = level;
  }

  /** Spawns the dedicated central terminal, the shared display and all three task areas. */
  public void setup() {
    spawnTerminal();
    spawnDisplay();
    spawnSortArea();
    spawnModuleArea();
    spawnMapArea();
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
   * Completes the central riddle and opens the route beyond the system core.
   *
   * <p>The terminal callback calls this only after all three terminal states have succeeded.
   */
  public void complete() {
    if (stage < 3) return;
    DoorTile door = (DoorTile) level.tileAt(level.getPoint("door_elevator")).orElseThrow();
    door.open();
  }

  /** @return whether all three central routines have been accepted */
  public boolean completed() {
    return stage >= 3;
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
        EntityFactory.hintDisplay(
            level.getPoint("core_display"),
            this::displayText,
            SystemRecoveryText.text("world.system-core.title"));
    display.name("core_display");
    Game.add(display);
    updateDisplay();
  }

  private void spawnSortArea() {
    for (int index = 0; index < SORT_VALUES.length; index++) {
      Entity entry =
          EntityFactory.sortingDataCrystal(level.getPoint("b" + index), SORT_VALUES[index]);
      entry.name("system_core_sort_" + index);
      sortEntries[index] = entry;
      Game.add(entry);
    }
  }

  private void spawnModuleArea() {
    for (int index = 0; index < MODULE_VALUES.length; index++) {
      Entity entry =
          EntityFactory.systemCoreModuleEntry(
              level.getPoint("mod" + index), index, MODULE_VALUES[index]);
      moduleEntries[index] = entry;
      Game.add(entry);
    }
  }

  private void spawnMapArea() {
    map = SearchRobotMatrix.between(level.getPoint("map00"), level.getPoint("map24"));
    for (int row = 0; row < map.rows(); row++) {
      for (int column = 0; column < map.columns(); column++) {
        boolean batterySignal = BATTERY_CELLS.contains(row + "_" + column);
        Entity cell =
            EntityFactory.systemCoreMapCell(
                map.pointAt(row, column), row, column, batterySignal);
        mapEntries.add(cell);
        Game.add(cell);
      }
    }
  }

  private String displayText() {
    return switch (stage) {
      case 0 -> SystemRecoveryText.text("world.system-core.display-sort");
      case 1 -> SystemRecoveryText.text("world.system-core.display-count");
      case 2 -> SystemRecoveryText.text("world.system-core.display-search");
      default -> SystemRecoveryText.text("world.system-core.display-complete");
    };
  }

  private void updateDisplay() {
    if (display != null) {
      EntityFactory.updateDisplayText(display, displayText());
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
            entity
                .fetch(DrawComponent.class)
                .ifPresent(draw -> draw.tintColor(COMPLETE_TINT)));
  }
}
