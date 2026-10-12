package feature.prefabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Session-only prefab changes of one level, layered over its authored prefab instances.
 *
 * <p>Instances spawned or updated at runtime are stored as overrides, removed instances by name.
 * None of this is written to the level file. The state belongs to one loaded level object, so it is
 * discarded when the level is loaded again.
 *
 * @see PrefabRuntime
 */
public final class PrefabRuntimeState {

  private final Map<String, PrefabInstance> overrides = new LinkedHashMap<>();
  private final Set<String> removed = new HashSet<>();
  private final Map<String, Map<PrefabEvent<?>, List<Consumer<?>>>> listeners = new HashMap<>();

  /**
   * Snapshot of the runtime state of one instance name, used to roll back failed changes.
   *
   * @param override runtime data of the instance, or null if it has none
   * @param removed whether the instance was removed at runtime
   */
  record Entry(PrefabInstance override, boolean removed) {}

  /**
   * Applies the runtime changes to the authored instances of a level.
   *
   * <p>Authored instances keep their order, overridden ones are replaced in place, and instances
   * that only exist at runtime are appended in the order they were spawned.
   *
   * @param authored authored level instances
   * @return the instances active in the current game session
   */
  public List<PrefabInstance> resolve(List<PrefabInstance> authored) {
    if (overrides.isEmpty() && removed.isEmpty()) return Collections.unmodifiableList(authored);
    List<PrefabInstance> result = new ArrayList<>(authored.size() + overrides.size());
    Set<String> seen = new HashSet<>();
    for (PrefabInstance instance : authored) {
      seen.add(instance.name());
      if (removed.contains(instance.name())) continue;
      result.add(overrides.getOrDefault(instance.name(), instance));
    }
    for (PrefabInstance override : overrides.values()) {
      if (!seen.contains(override.name())) result.add(override);
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns whether any runtime change is present.
   *
   * @return true if instances were spawned, updated or removed at runtime
   */
  public boolean isModified() {
    return !overrides.isEmpty() || !removed.isEmpty();
  }

  void put(PrefabInstance instance) {
    removed.remove(instance.name());
    overrides.put(instance.name(), instance);
  }

  void remove(String name) {
    overrides.remove(name);
    removed.add(name);
    listeners.remove(name);
  }

  /**
   * Drops the runtime changes of the given instance names so their authored state applies again.
   *
   * <p>Used by the level editor after it changed authored instances, so edits are always visible.
   *
   * @param names instance names to reset to their authored state
   */
  public void discard(Iterable<String> names) {
    for (String name : names) {
      overrides.remove(name);
      removed.remove(name);
    }
  }

  Entry entry(String name) {
    return new Entry(overrides.get(name), removed.contains(name));
  }

  void restore(String name, Entry entry) {
    if (entry.override() == null) overrides.remove(name);
    else overrides.put(name, entry.override());
    if (entry.removed()) removed.add(name);
    else removed.remove(name);
  }

  <T> void addListener(String name, PrefabEvent<T> event, Consumer<? super T> listener) {
    Objects.requireNonNull(event, "event");
    Objects.requireNonNull(listener, "listener");
    listeners
        .computeIfAbsent(name, ignored -> new HashMap<>())
        .computeIfAbsent(event, ignored -> new ArrayList<>())
        .add(listener);
  }

  @SuppressWarnings("unchecked")
  <T> List<Consumer<? super T>> listeners(String name, PrefabEvent<T> event) {
    Map<PrefabEvent<?>, List<Consumer<?>>> byEvent = listeners.get(name);
    if (byEvent == null) return List.of();
    List<Consumer<?>> registered = byEvent.get(event);
    if (registered == null) return List.of();
    List<Consumer<? super T>> result = new ArrayList<>(registered.size());
    for (Consumer<?> listener : registered) result.add((Consumer<? super T>) listener);
    return result;
  }
}
