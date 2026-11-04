package feature.interaction.combinationlock;

import engine.Entity;
import engine.utils.logging.DungeonLogger;
import java.util.Map;

/**
 * Encodes the wheels of a combination lock in generic entity metadata, so a client can render the
 * {@link CombinationLockUI} of a lock owned by the server.
 */
public final class CombinationLockSync {

  /** Metadata key of the digits the wheels show. */
  public static final String DIGITS_KEY = "combinationLock.digits";

  /** Metadata key of the selected wheel. */
  public static final String SELECTED_KEY = "combinationLock.selected";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CombinationLockSync.class);

  private CombinationLockSync() {}

  /**
   * Encodes the current wheels, if the entity has a combination lock.
   *
   * @param entity entity to inspect
   * @return an immutable metadata map, or an empty map for entities without a lock
   */
  public static Map<String, String> metadata(Entity entity) {
    return entity
        .fetch(CombinationLockComponent.class)
        .map(
            component ->
                Map.of(
                    DIGITS_KEY,
                    component.digits(),
                    SELECTED_KEY,
                    Integer.toString(component.selected())))
        .orElseGet(Map::of);
  }

  /**
   * Applies synchronized state to an entity, creating or replacing its lock component if the number
   * of wheels differs.
   *
   * <p>Metadata without lock state leaves the entity unchanged.
   *
   * @param entity client-side entity receiving the metadata
   * @param metadata synchronized entity metadata
   */
  public static void apply(Entity entity, Map<String, String> metadata) {
    String digits = metadata.get(DIGITS_KEY);
    if (digits == null) return;
    int selected;
    try {
      selected = Integer.parseInt(metadata.getOrDefault(SELECTED_KEY, ""));
    } catch (NumberFormatException e) {
      LOGGER.warn("Ignoring invalid combination lock wheel for entity {}", entity.id());
      return;
    }
    if (!digits.matches("[0-9]+") || selected < 0 || selected >= digits.length()) {
      LOGGER.warn("Ignoring invalid combination lock state for entity {}", entity.id());
      return;
    }

    CombinationLockComponent component =
        entity
            .fetch(CombinationLockComponent.class)
            .filter(current -> current.wheels() == digits.length())
            .orElseGet(
                () -> {
                  CombinationLockComponent created = new CombinationLockComponent(digits.length());
                  entity.add(created);
                  return created;
                });
    component.digits(digits);
    component.selected(selected);
  }
}
