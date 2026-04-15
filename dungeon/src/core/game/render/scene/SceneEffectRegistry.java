package core.game.render.scene;

import java.util.*;

/**
 * The SceneEffectRegistry class manages a collection of scene effects with configurable priorities
 * and insertion orders. Effects can be registered, removed, retrieved, and manipulated based on their
 * priority and other properties.
 *
 * <p>This registry supports operations such as enabling/disabling effects, modifying effect priorities, and
 * iterating over effects in a priority-respecting order.
 *
 * <p>It is designed to support toggleable effects via the ToggleableSceneEffect interface but can accommodate
 * other immutable or customized effect types as well.
 */
public final class SceneEffectRegistry {

  private long insertionCounter = 0L;

  private final Map<String, SceneEffect> effectMap = new HashMap<>();
  private final Map<String, Integer> priorityMap = new HashMap<>();
  private final Map<String, Long> insertionIndexMap = new HashMap<>();
  private final TreeMap<Integer, Set<Entry>> sortedByPriority = new TreeMap<>();

  /**
   * Adds a new scene effect with the given priority.
   *
   * @param identifier unique identifier
   * @param effect effect instance
   * @param priority lower values are applied earlier
   * @return true if added, false if the identifier already exists
   */
  public boolean add(String identifier, SceneEffect effect, int priority) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(effect, "effect");

    if (effectMap.containsKey(identifier)) {
      return false;
    }

    long insertionIndex = insertionCounter++;
    Entry entry = new Entry(effect, insertionIndex);

    effectMap.put(identifier, effect);
    priorityMap.put(identifier, priority);
    insertionIndexMap.put(identifier, insertionIndex);
    sortedByPriority.computeIfAbsent(priority, ignored -> new TreeSet<>()).add(entry);
    return true;
  }

  /**
   * Adds a new scene effect with default priority 0.
   *
   * @param identifier unique identifier
   * @param effect effect instance
   * @return true if added, false if the identifier already exists
   */
  public boolean add(String identifier, SceneEffect effect) {
    return add(identifier, effect, 0);
  }

  /**
   * Removes an effect by identifier.
   *
   * @param identifier unique identifier
   * @return true if removed, false if not found
   */
  public boolean remove(String identifier) {
    if (!effectMap.containsKey(identifier)) {
      return false;
    }

    SceneEffect removedEffect = effectMap.remove(identifier);
    Integer priority = priorityMap.remove(identifier);
    Long insertionIndex = insertionIndexMap.remove(identifier);

    if (priority != null && insertionIndex != null) {
      removeFromSortedMap(new Entry(removedEffect, insertionIndex), priority);
    }

    return true;
  }

  /**
   * Returns an effect by identifier.
   *
   * @param identifier unique identifier
   * @return effect if present
   */
  public Optional<SceneEffect> get(String identifier) {
    return Optional.ofNullable(effectMap.get(identifier));
  }

  /**
   * Changes the priority of an existing effect.
   *
   * @param identifier unique identifier
   * @param newPriority new priority
   * @return true if updated, false if the effect does not exist
   */
  public boolean changePriority(String identifier, int newPriority) {
    SceneEffect effect = effectMap.get(identifier);
    Integer oldPriority = priorityMap.get(identifier);
    Long insertionIndex = insertionIndexMap.get(identifier);

    if (effect == null || oldPriority == null || insertionIndex == null) {
      return false;
    }

    if (oldPriority == newPriority) {
      return true;
    }

    Entry entry = new Entry(effect, insertionIndex);
    removeFromSortedMap(entry, oldPriority);
    priorityMap.put(identifier, newPriority);
    sortedByPriority.computeIfAbsent(newPriority, ignored -> new TreeSet<>()).add(entry);
    return true;
  }

  /** @return true if at least one effect is currently enabled */
  public boolean hasEnabledEffects() {
    for (SceneEffect effect : effectMap.values()) {
      if (effect.enabled()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Enables or disables all registered effects.
   *
   * <p>This method only toggles effects that implement the common mutable base contract introduced
   * by this backend. Immutable/custom effects are left unchanged.
   *
   * @param enabled desired enabled state
   */
  public void enableAll(boolean enabled) {
    for (SceneEffect effect : effectMap.values()) {
      if (effect instanceof ToggleableSceneEffect toggleable) {
        toggleable.enabled(enabled);
      }
    }
  }

  /** Enables all toggleable effects. */
  public void enableAll() {
    enableAll(true);
  }

  /** Disables all toggleable effects. */
  public void disableAll() {
    enableAll(false);
  }

  /**
   * Returns whether all toggleable scene effects are currently enabled.
   *
   * <p>If no toggleable effects are registered, this returns {@code false}. This behavior is more
   * useful for debug toggles than treating the empty case as "all enabled".
   *
   * @return true if every toggleable scene effect is enabled, false otherwise
   */
  public boolean allEnabled() {
    boolean hasToggleableEffects = false;

    for (SceneEffect effect : effectMap.values()) {
      if (effect instanceof ToggleableSceneEffect) {
        hasToggleableEffects = true;
        if (!effect.enabled()) {
          return false;
        }
      }
    }

    return hasToggleableEffects;
  }

  /**
   * Toggles all toggleable scene effects at once.
   *
   * <p>If at least one toggleable effect is currently disabled, all toggleable effects will be
   * enabled. Otherwise, all toggleable effects will be disabled.
   *
   * @return the new enabled state that was applied
   */
  public boolean toggleAll() {
    boolean newState = !allEnabled();
    enableAll(newState);
    return newState;
  }

  /** Removes all registered effects. */
  public void clear() {
    effectMap.clear();
    priorityMap.clear();
    insertionIndexMap.clear();
    sortedByPriority.clear();
  }

  /** @return true if no effects are registered */
  public boolean isEmpty() {
    return effectMap.isEmpty();
  }

  /**
   * Returns all effects sorted by priority and insertion order.
   *
   * @param onlyEnabled whether only enabled effects should be returned
   * @return iterable view over sorted effects
   */
  public Iterable<SceneEffect> getSorted(boolean onlyEnabled) {
    return () ->
      new Iterator<>() {
        private final Iterator<Set<Entry>> priorityIterator = sortedByPriority.values().iterator();
        private Iterator<Entry> currentSetIterator = Collections.emptyIterator();
        private SceneEffect nextEffect = null;

        @Override
        public boolean hasNext() {
          if (nextEffect != null) {
            return true;
          }

          while (true) {
            if (currentSetIterator.hasNext()) {
              SceneEffect candidate = currentSetIterator.next().effect();
              if (!onlyEnabled || candidate.enabled()) {
                nextEffect = candidate;
                return true;
              }
            } else if (priorityIterator.hasNext()) {
              currentSetIterator = priorityIterator.next().iterator();
            } else {
              return false;
            }
          }
        }

        @Override
        public SceneEffect next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }

          SceneEffect result = nextEffect;
          nextEffect = null;
          return result;
        }
      };
  }

  /**
   * Returns all enabled effects sorted by priority and insertion order.
   *
   * @return iterable over enabled effects
   */
  public Iterable<SceneEffect> getEnabledSorted() {
    return getSorted(true);
  }

  /**
   * Optional helper contract for mutable scene effects that can be enabled/disabled globally.
   */
  public interface ToggleableSceneEffect extends SceneEffect {
    void enabled(boolean enabled);
  }

  private void removeFromSortedMap(Entry entry, int priority) {
    Set<Entry> set = sortedByPriority.get(priority);
    if (set != null) {
      set.remove(entry);
      if (set.isEmpty()) {
        sortedByPriority.remove(priority);
      }
    }
  }

  private record Entry(SceneEffect effect, long insertionIndex)
    implements Comparable<Entry> {
    @Override
    public int compareTo(Entry other) {
      return Long.compare(this.insertionIndex, other.insertionIndex);
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Entry other && this.insertionIndex == other.insertionIndex;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(insertionIndex);
    }
  }
}
