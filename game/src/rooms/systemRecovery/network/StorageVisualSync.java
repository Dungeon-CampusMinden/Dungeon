package rooms.systemRecovery.network;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import java.util.Map;

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
      case "active" -> 0x4D7EA8FF;
      case "target" -> 0xF0D248FF;
      case "filled" ->
          switch (value) {
            case 1 -> 0x42C8E6FF;
            case 2 -> 0xF0B84AFF;
            case 3 -> 0xD66CFFFF;
            default -> 0x4D7EA8FF;
          };
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
