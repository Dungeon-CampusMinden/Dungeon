package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.sound.SoundSpec;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.systems.EventScheduler;
import java.util.Map;
import rooms.systemRecovery.entities.ScannerEntityFactory;
import rooms.systemRecovery.entities.StorageEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.StorageCellColors;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 8: materialize and inspect the three-by-four storage matrix.
 *
 * <p>The twelve matrix cells are deliberately owned here. Riddle 9 has a separate three-by-two
 * matrix and must never reuse these coordinates.
 */
public final class TwoDimensionalStorageRiddle {
  /** Number of rows in the storage matrix. */
  public static final int ROW_COUNT = 3;

  /** Number of columns in the storage matrix. */
  public static final int COLUMN_COUNT = 4;

  private static final Map<String, Integer> FILLED_VALUES = Map.of("0_2", 1, "1_3", 2, "2_1", 3);
  private static final long CHIP_ARM_STEP_MS = 100L;
  private static final int CHIP_ARM_STEPS = 12;

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;
  private final Entity[][] cells = new Entity[ROW_COUNT][COLUMN_COUNT];
  private final Entity[] storageItems = new Entity[FILLED_VALUES.size()];
  private int stage;
  private Entity chipGrabArm;
  private boolean chipDelivered;
  private Entity storageDisplay;
  private String storageDisplayText;

  /**
   * Creates the storage-matrix controller for the owning level.
   *
   * @param level owning System Recovery level
   */
  public TwoDimensionalStorageRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /**
   * Creates the storage riddle with callbacks for physical interactions.
   *
   * @param level owning System Recovery level
   * @param callbacks hooks for successful, failed and completed interactions
   */
  public TwoDimensionalStorageRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
    this.storageDisplayText = SystemRecoveryText.key("world.matrix.display-array");
  }

  /** Spawns the shared terminal and all twelve storage cells in their 3x4 layout. */
  public void setup() {
    spawnTerminal();
    storageDisplay =
        SystemRecoveryDisplayFactory.hintDisplay(
            level.getPoint("display_2d"),
            () -> storageDisplayText,
            SystemRecoveryText.key("world.matrix.title"));
    storageDisplay.name("storage_display");
    Game.add(storageDisplay);
    for (int row = 0; row < ROW_COUNT; row++) {
      for (int column = 0; column < COLUMN_COUNT; column++) {
        Entity cell = StorageEntityFactory.matrixCell(cellPoint(row, column), row, column);
        cells[row][column] = cell;
        Game.add(cell);
      }
    }
  }

  private void spawnTerminal() {
    Entity terminal = DecoFactory.createDeco(level.getPoint("storage_terminal"), Deco.DeskWithPC1);
    terminal.name("storage_terminal");
    terminal.remove(DecoComponent.class);
    SystemRecoveryComputerFactory.attachComputerDialog(terminal);
    Game.add(terminal);
  }

  /** Activates the complete matrix after the array declaration was accepted. */
  public void activateMatrix() {
    if (stage >= 1) return;
    stage = 1;
    storageDisplayText = SystemRecoveryText.key("world.matrix.display-values");
    updateStorageDisplay();
    for (int row = 0; row < ROW_COUNT; row++) {
      for (int column = 0; column < COLUMN_COUNT; column++) {
        boolean target = FILLED_VALUES.containsKey(row + "_" + column);
        cells[row][column].name(
            "storage_matrix_cell_" + row + "_" + column + (target ? "_target" : "_active"));
        tintCell(
            row,
            column,
            target
                ? StorageCellColors.forValue(FILLED_VALUES.get(row + "_" + column))
                : StorageCellColors.active());
      }
    }
  }

  /** Marks the three required coordinates as filled after their assignments were accepted. */
  public void fillMatrix() {
    if (stage >= 2) return;
    stage = 2;
    for (Map.Entry<String, Integer> entry : FILLED_VALUES.entrySet()) {
      String[] coordinate = entry.getKey().split("_");
      markFilled(Integer.parseInt(coordinate[0]), Integer.parseInt(coordinate[1]));
    }
    spawnStorageItems();
  }

  /** Completes the matrix riddle and sends an empty locator chip to the chip point. */
  public void complete() {
    if (stage >= 3) return;
    stage = 3;
    callbacks.success("matrix-filled", -1);
    callbacks.solved();
    storageDisplayText = SystemRecoveryText.key("world.matrix.display-complete");
    updateStorageDisplay();
    deliverChipWithGrabArm();
  }

  /**
   * Returns the synchronized visual state for a matrix cell.
   *
   * @param row matrix row
   * @param column matrix column
   * @return empty, active, target or filled
   */
  public String cellState(int row, int column) {
    if (stage >= 2 && FILLED_VALUES.containsKey(row + "_" + column)) return "filled";
    if (stage == 1 && FILLED_VALUES.containsKey(row + "_" + column)) return "target";
    return stage >= 1 ? "active" : "empty";
  }

  /**
   * Returns the value shown by a filled matrix cell, or zero for an empty cell.
   *
   * @param row matrix row
   * @param column matrix column
   * @return stored value, or zero when the cell is empty
   */
  public int cellValue(int row, int column) {
    return FILLED_VALUES.getOrDefault(row + "_" + column, 0);
  }

  /**
   * @return the current storage-riddle stage, where 3 means the chip delivery has started
   */
  public int stage() {
    return stage;
  }

  /**
   * Returns whether the matrix has released its locator chip.
   *
   * @return {@code true} after the assignments have released the locator chip
   */
  public boolean completed() {
    return stage >= 3;
  }

  /** Restores the released-chip state without replaying the grab-arm animation. */
  public void restoreCompletedState() {
    if (stage < 1) activateMatrix();
    if (stage < 2) fillMatrix();
    stage = 3;
    storageDisplayText = SystemRecoveryText.key("world.matrix.display-complete");
    updateStorageDisplay();
  }

  private Point cellPoint(int row, int column) {
    return RiddleSupport.point(
        level, "storage_cell_" + row + "_" + column, "storage_" + row + "_" + column);
  }

  private void markFilled(int row, int column) {
    Entity cell = cells[row][column];
    cell.name("storage_matrix_cell_" + row + "_" + column + "_filled");
    tintCell(row, column, StorageCellColors.forValue(cellValue(row, column)));
  }

  private void tintCell(int row, int column, int tint) {
    Game.tileAt(cellPoint(row, column)).ifPresent(tile -> tile.tintColor(tint));
  }

  private void updateStorageDisplay() {
    if (storageDisplay != null) {
      SystemRecoveryDisplayFactory.updateDisplayText(storageDisplay, storageDisplayText);
    }
  }

  private void spawnStorageItems() {
    if (storageItems[0] != null) return;
    int itemIndex = 0;
    for (Map.Entry<String, Integer> entry : FILLED_VALUES.entrySet()) {
      String[] coordinate = entry.getKey().split("_");
      Entity item =
          StorageEntityFactory.valueItem(
              cellPoint(Integer.parseInt(coordinate[0]), Integer.parseInt(coordinate[1])),
              entry.getValue());
      storageItems[itemIndex++] = item;
      Game.add(item);
    }
  }

  private void deliverChipWithGrabArm() {
    if (chipDelivered || chipGrabArm != null) return;
    chipGrabArm = ScannerEntityFactory.chipGrabArm(cellPoint(1, 3));
    Game.add(chipGrabArm);
    Game.audio().playGlobal(SoundSpec.builder("retro_beep_01"));
    moveChipGrabArm(0);
  }

  private void moveChipGrabArm(int step) {
    if (chipGrabArm == null) return;
    Point start = cellPoint(1, 3);
    Point destination = level.getPoint("chip");
    Point position =
        start.translate(start.vectorTo(destination).scale((float) step / CHIP_ARM_STEPS));
    RiddleSupport.moveSortEntity(chipGrabArm, position);
    if (step < CHIP_ARM_STEPS) {
      EventScheduler.scheduleAction(() -> moveChipGrabArm(step + 1), CHIP_ARM_STEP_MS);
      return;
    }

    Game.remove(chipGrabArm);
    chipGrabArm = null;
    if (!chipDelivered) {
      chipDelivered = true;
      Game.add(WorldItemBuilder.buildWorldItem(new SearchProgramChipItem(), destination));
    }
  }
}
