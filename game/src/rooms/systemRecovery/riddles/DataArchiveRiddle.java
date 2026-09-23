package rooms.systemRecovery.riddles;

import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import rooms.systemRecovery.entities.ArchiveEntityFactory;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Riddle 7: reconstruct the archive data model from bookshelves and physical data nodes.
 *
 * <p>The room deliberately separates syntax hints from values. The shelves explain which array
 * declarations are needed; the three data nodes provide the values through world interactions.
 * Array and value order is intentionally not part of this riddle's meaning.
 */
public final class DataArchiveRiddle {
  private static final int[] ENERGY_VALUES = {20, 50, 80};
  private static final String[] MODULE_VALUES = {"CPU", "GPU", "RAM"};
  private static final boolean[] ACTIVE_VALUES = {true, false, true};

  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;
  private boolean completed;

  /**
   * Creates the archive riddle for the owning level.
   *
   * @param level level that owns the archive entities
   */
  public DataArchiveRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /**
   * Creates the archive riddle with callbacks for physical interactions.
   *
   * @param level level that owns the archive entities
   * @param callbacks success, failure and completion callbacks
   */
  public DataArchiveRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
  }

  /** Spawns the three programming-hint shelves and the three physical archive data nodes. */
  public void setup() {
    spawnBookshelves();
    spawnDataNodes();
  }

  /**
   * Marks the terminal requirement as solved and opens the door to the next storage room.
   *
   * <p>The door is a shared world object, so the authoritative tile state is enough to synchronize
   * the result to all clients.
   */
  public void complete() {
    if (completed) return;
    completed = true;
    callbacks.success("arrays-created", -1);
    callbacks.solved();
    ((DoorTile) Game.tileAt(level.getPoint("door_speicher")).orElseThrow()).open();
  }

  /** Restores the accepted archive arrays and opened storage door without gameplay callbacks. */
  public void restoreCompletedState() {
    completed = true;
    ((DoorTile) Game.tileAt(level.getPoint("door_speicher")).orElseThrow()).open();
  }

  /**
   * @return whether the archive terminal requirement has been solved
   */
  public boolean completed() {
    return completed;
  }

  private void spawnBookshelves() {
    Game.add(
        ArchiveEntityFactory.archiveBookshelf(
            level.getPoint("archive_shelf_energie"),
            "archive_shelf_energie",
            SystemRecoveryText.key("world.archive.shelf-energy")));
    Game.add(
        ArchiveEntityFactory.archiveBookshelf(
            level.getPoint("archive_shelf_module"),
            "archive_shelf_module",
            SystemRecoveryText.key("world.archive.shelf-module")));
    Game.add(
        ArchiveEntityFactory.archiveBookshelf(
            level.getPoint("archive_shelf_aktiv"),
            "archive_shelf_aktiv",
            SystemRecoveryText.key("world.archive.shelf-active")));
  }

  private void spawnDataNodes() {
    for (int index = 0; index < ENERGY_VALUES.length; index++) {
      Point node = level.getPoint("archive_node" + index);
      String nodeName = "archive_node" + index;
      String dataText =
          SystemRecoveryText.key(
              "world.archive.node", index, ENERGY_VALUES[index], MODULE_VALUES[index]);
      Game.add(
          ArchiveEntityFactory.archiveDataDisplay(node, nodeName, dataText, ACTIVE_VALUES[index]));
    }
  }
}
