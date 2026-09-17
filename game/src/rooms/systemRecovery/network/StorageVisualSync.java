package rooms.systemRecovery.network;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import java.util.Map;
import rooms.systemRecovery.util.StorageCellColors;

/** Reconstructs the authoritative tint of one two-dimensional storage cell. */
final class StorageVisualSync {

  /**
   * Applies the synchronized active, target or filled state of a storage cell.
   *
   * @param entity storage cell entity
   * @param metadata synchronized cell metadata
   */
  void apply(Entity entity, Map<String, String> metadata) {
    String state = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_STATE);
    if (state == null
        || entity.name() == null
        || !entity.name().startsWith("storage_matrix_cell_")) {
      return;
    }

    int value =
        parseInteger(
            metadata.getOrDefault(
                SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_VALUE, "0"));
    int tint = tintFor(state, value);
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            position -> Game.tileAt(position.position()).ifPresent(tile -> tile.tintColor(tint)));
  }

  /** Storage cells have no local cache; this method is present for the common sync lifecycle. */
  void reset() {}

  private static int tintFor(String state, int value) {
    return switch (state) {
      case "active" -> StorageCellColors.active();
      case "target", "filled" -> StorageCellColors.forValue(value);
      default -> -1;
    };
  }

  private static int parseInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }
}
