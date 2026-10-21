package feature.prefabs.types;

import engine.level.elements.ILevel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Runtime state of the spawned instances of one prefab type that is not part of any entity, e.g.
 * pending timers.
 *
 * <p>Prefabs store the state when an instance spawns and remove it in {@link
 * feature.prefabs.Prefab#onDespawn}, so actions of a respawned instance never see stale state.
 *
 * @param <S> state type
 */
final class SpawnState<S> {

  private final Map<ILevel, Map<String, S>> states = new WeakHashMap<>();

  /**
   * Stores the state of a newly spawned instance.
   *
   * @param level owning level
   * @param name instance name
   * @param state state of the spawn
   * @return the stored state
   */
  S start(ILevel level, String name, S state) {
    states.computeIfAbsent(level, ignored -> new HashMap<>()).put(name, state);
    return state;
  }

  /**
   * Returns the state of a spawned instance.
   *
   * @param level owning level
   * @param name instance name
   * @return the state, or empty if the instance is not spawned
   */
  Optional<S> get(ILevel level, String name) {
    Map<String, S> byName = states.get(level);
    return byName == null ? Optional.empty() : Optional.ofNullable(byName.get(name));
  }

  /**
   * Removes the state of a despawned instance.
   *
   * @param level owning level
   * @param name instance name
   * @return the removed state, if there was one
   */
  Optional<S> end(ILevel level, String name) {
    Map<String, S> byName = states.get(level);
    if (byName == null) return Optional.empty();
    S state = byName.remove(name);
    if (byName.isEmpty()) states.remove(level);
    return Optional.ofNullable(state);
  }
}
