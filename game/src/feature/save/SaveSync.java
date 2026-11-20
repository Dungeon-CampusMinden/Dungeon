package feature.save;

import engine.Entity;
import engine.components.PlayerComponent;
import feature.hud.AutoSaveIndicator;
import java.util.Map;

/**
 * Tells clients when the server wrote a save of the {@link GameSaves save system}, so they can
 * confirm it with the {@link AutoSaveIndicator}.
 */
public final class SaveSync {

  /** Snapshot metadata key of the save revision, set on player entities. */
  public static final String METADATA_SAVE_REVISION = "game-save-revision";

  // Save revision the client saw first in this session, saves up to it are not confirmed.
  private static int firstSeen = -1;

  private SaveSync() {}

  /** Forgets the revisions of the previous session, called when the client connects. */
  static void reset() {
    firstSeen = -1;
    AutoSaveIndicator.reset();
  }

  /**
   * Returns the save metadata of an entity on the server.
   *
   * @param entity entity of the snapshot
   * @return the save revision for players while the game saves itself, otherwise nothing
   */
  public static Map<String, String> metadata(Entity entity) {
    if (!GameSaves.enabled() || !entity.isPresent(PlayerComponent.class)) return Map.of();
    return Map.of(METADATA_SAVE_REVISION, Integer.toString(GameSaves.revision()));
  }

  /**
   * Confirms a new save on the client.
   *
   * @param metadata snapshot metadata of an entity
   */
  public static void apply(Map<String, String> metadata) {
    String revision = metadata.get(METADATA_SAVE_REVISION);
    if (revision == null) return;
    if (firstSeen < 0) {
      // Saves written before the client joined are not confirmed.
      try {
        firstSeen = Integer.parseInt(revision);
      } catch (NumberFormatException ignored) {
        // Invalid metadata is ignored like a missing one.
      }
      return;
    }
    AutoSaveIndicator.acceptRevision(revision);
  }
}
