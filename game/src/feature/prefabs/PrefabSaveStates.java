package feature.prefabs;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.logging.DungeonLogger;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import tools.jackson.databind.JsonNode;

/**
 * Runtime state of prefab instances that a save of the game keeps, e.g. whether a lever is on.
 *
 * <p>Only state that players created directly is kept, see {@link Prefab#captureState}. What links
 * did to other prefabs, such as opening a door, is rebuilt from the {@link GameFlags game flags} of
 * the save instead.
 */
public final class PrefabSaveStates {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PrefabSaveStates.class);
  // Saved states of the instances of a level that is loaded from a save and not yet respawned.
  private static final Map<ILevel, Map<String, JsonNode>> RESTORING = new WeakHashMap<>();

  private PrefabSaveStates() {}

  /**
   * Captures the state of every spawned server-side instance of a level that has one.
   *
   * @param level level to save
   * @return saved states by instance name
   */
  public static Map<String, JsonNode> capture(ILevel level) {
    Map<String, JsonNode> states = new LinkedHashMap<>();
    PrefabSpawner.forEachSpawned(
        level,
        PrefabSide.SERVER,
        (prefab, instance, entities) -> {
          try {
            prefab
                .captureState(
                    new PrefabCreationContext(level, PrefabSide.SERVER), instance, entities)
                .ifPresent(state -> states.put(instance.name(), state));
          } catch (RuntimeException exception) {
            // One broken prefab must not cost the players the rest of their progress.
            LOGGER.error("Could not save the state of prefab '" + instance.name() + "'", exception);
          }
        });
    return states;
  }

  /**
   * Returns the entities whose state the prefabs of a level save themselves, so a save does not
   * capture them a second time, e.g. as items lying in the level.
   *
   * @param level level to save
   * @return the claimed entities
   */
  public static Set<Entity> claimedEntities(ILevel level) {
    Set<Entity> claimed = Collections.newSetFromMap(new IdentityHashMap<>());
    PrefabSpawner.forEachSpawned(
        level,
        PrefabSide.SERVER,
        (prefab, instance, entities) ->
            claimed.addAll(
                prefab.savedEntities(
                    new PrefabCreationContext(level, PrefabSide.SERVER), instance, entities)));
    return claimed;
  }

  /**
   * Hands saved states to the instances of a level that is loaded from a save. Each instance
   * receives its state when it spawns for the first time, see {@link Prefab#restoredState}.
   *
   * @param level level loaded from the save
   * @param states saved states by instance name
   */
  public static void restore(ILevel level, Map<String, JsonNode> states) {
    RESTORING.put(level, new LinkedHashMap<>(states));
  }

  /**
   * Drops the saved states that no instance used, so later spawns start from authored data.
   *
   * <p>Called once the level and everything its handler restores have spawned.
   *
   * @param level level loaded from the save
   */
  public static void finishRestore(ILevel level) {
    Map<String, JsonNode> unused = RESTORING.remove(level);
    if (unused != null && !unused.isEmpty()) {
      LOGGER.warn("Saved prefab states without a spawned instance: " + unused.keySet());
    }
  }

  static Optional<JsonNode> restoring(ILevel level, String name) {
    Map<String, JsonNode> states = RESTORING.get(level);
    return states == null ? Optional.empty() : Optional.ofNullable(states.get(name));
  }

  static void consume(ILevel level, String name) {
    Map<String, JsonNode> states = RESTORING.get(level);
    if (states != null) states.remove(name);
  }
}
