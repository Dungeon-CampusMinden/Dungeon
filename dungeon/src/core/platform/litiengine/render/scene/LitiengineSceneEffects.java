package core.platform.litiengine.render.scene;

import java.util.*;

/**
 * Ordered collection of LITIENGINE scene-pass effects.
 *
 * <p>Effects are sorted first by priority and then by insertion order.
 */
public final class LitiengineSceneEffects {

  private long insertionCounter = 0L;

  private final Map<String, LitiengineSceneEffect> effectMap = new HashMap<>();
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
  public boolean add(String identifier, LitiengineSceneEffect effect, int priority) {
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
  public boolean add(String identifier, LitiengineSceneEffect effect) {
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

    LitiengineSceneEffect removedEffect = effectMap.remove(identifier);
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
  public Optional<LitiengineSceneEffect> get(String identifier) {
    return Optional.ofNullable(effectMap.get(identifier));
  }

  /**
   * Changes the priority of an existing effect.
   *
   * @param identifier unique identifier
   * @param newPriority new priority
   * @return true if updated, false if effect does not exist
   */
  public boolean changePriority(String identifier, int newPriority) {
    LitiengineSceneEffect effect = effectMap.get(identifier);
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
    for (LitiengineSceneEffect effect : effectMap.values()) {
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
    for (LitiengineSceneEffect effect : effectMap.values()) {
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
  public Iterable<LitiengineSceneEffect> getSorted(boolean onlyEnabled) {
    return () ->
      new Iterator<>() {
        private final Iterator<Set<Entry>> priorityIterator = sortedByPriority.values().iterator();
        private Iterator<Entry> currentSetIterator = Collections.emptyIterator();
        private LitiengineSceneEffect nextEffect = null;

        @Override
        public boolean hasNext() {
          if (nextEffect != null) {
            return true;
          }

          while (true) {
            if (currentSetIterator.hasNext()) {
              LitiengineSceneEffect candidate = currentSetIterator.next().effect();
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
        public LitiengineSceneEffect next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }

          LitiengineSceneEffect result = nextEffect;
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
  public Iterable<LitiengineSceneEffect> getEnabledSorted() {
    return getSorted(true);
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

  private record Entry(LitiengineSceneEffect effect, long insertionIndex)
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

  /**
   * Optional helper contract for mutable scene effects that can be enabled/disabled globally.
   */
  public interface ToggleableSceneEffect extends LitiengineSceneEffect {
    void enabled(boolean enabled);
  }
}
