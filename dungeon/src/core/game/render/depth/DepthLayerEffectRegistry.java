package core.game.render.depth;

import java.util.*;

/**
 * A registry for managing depth layer effects with priority-based ordering.
 *
 * <p>This registry maintains a collection of depth layer effects that can be applied to rendered
 * entity layers. Effects are stored by identifier and sorted by priority, with secondary ordering
 * by insertion order for effects with the same priority.
 *
 * <p>The registry supports enabling/disabling
 * effects and provides iteration over effects in a priority order.
 */
public final class DepthLayerEffectRegistry {

  private long insertionCounter = 0L;

  private final Map<String, DepthLayerEffect> effectMap = new HashMap<>();
  private final Map<String, Integer> priorityMap = new HashMap<>();
  private final Map<String, Long> insertionIndexMap = new HashMap<>();
  private final TreeMap<Integer, Set<Entry>> sortedByPriority = new TreeMap<>();

  /**
   * Adds a depth layer effect to the registry with the specified identifier and priority.
   *
   * @param identifier a unique identifier for this effect (must not be null)
   * @param effect the depth layer effect to add (must not be null)
   * @param priority the priority level (higher values are processed later)
   * @return true if the effect was added, false if an effect with the same identifier already exists
   * @throws NullPointerException if identifier or effect is null
   */
  public boolean add(String identifier, DepthLayerEffect effect, int priority) {
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
   * Adds a depth layer effect to the registry with the specified identifier and default priority of 0.
   *
   * @param identifier a unique identifier for this effect (must not be null)
   * @param effect the depth layer effect to add (must not be null)
   * @return true if the effect was added, false if an effect with the same identifier already exists
   */
  public boolean add(String identifier, DepthLayerEffect effect) {
    return add(identifier, effect, 0);
  }

  /**
   * Removes the depth layer effect with the specified identifier from the registry.
   *
   * @param identifier the unique identifier of the effect to remove
   * @return true if an effect was removed, false if no effect with that identifier exists
   */
  public boolean remove(String identifier) {
    if (!effectMap.containsKey(identifier)) {
      return false;
    }

    DepthLayerEffect removedEffect = effectMap.remove(identifier);
    Integer priority = priorityMap.remove(identifier);
    Long insertionIndex = insertionIndexMap.remove(identifier);

    if (priority != null && insertionIndex != null) {
      removeFromSortedMap(new Entry(removedEffect, insertionIndex), priority);
    }

    return true;
  }

  /**
   * Gets the depth layer effect with the specified identifier.
   *
   * @param identifier the unique identifier of the effect
   * @return an Optional containing the effect, or empty if no effect with that identifier exists
   */
  public Optional<DepthLayerEffect> get(String identifier) {
    return Optional.ofNullable(effectMap.get(identifier));
  }

  /**
   * Changes the priority of an existing effect.
   *
   * @param identifier the unique identifier of the effect to update
   * @param newPriority the new priority level
   * @return true if the priority was changed, false if no effect with that identifier exists
   */
  public boolean changePriority(String identifier, int newPriority) {
    DepthLayerEffect effect = effectMap.get(identifier);
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

  /**
   * Checks whether any depth layer effects in this registry are currently enabled.
   *
   * @return true if at least one effect is enabled, false otherwise
   */
  public boolean hasEnabledEffects() {
    for (DepthLayerEffect effect : effectMap.values()) {
      if (effect.enabled()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the enabled state of all toggleable depth layer effects in this registry.
   *
   * @param enabled true to enable all effects, false to disable all effects
   */
  public void enableAll(boolean enabled) {
    for (DepthLayerEffect effect : effectMap.values()) {
      if (effect instanceof ToggleableDepthLayerEffect toggleable) {
        toggleable.enabled(enabled);
      }
    }
  }

  /**
   * Enables all toggleable depth layer effects in this registry.
   */
  public void enableAll() {
    enableAll(true);
  }

  /**
   * Disables all toggleable depth layer effects in this registry.
   */
  public void disableAll() {
    enableAll(false);
  }

  /**
   * Checks whether all toggleable depth layer effects in this registry are enabled.
   *
   * @return true if all toggleable effects are enabled, false otherwise or if no toggleable effects exist
   */
  public boolean allEnabled() {
    boolean hasToggleableEffects = false;

    for (DepthLayerEffect effect : effectMap.values()) {
      if (effect instanceof ToggleableDepthLayerEffect) {
        hasToggleableEffects = true;
        if (!effect.enabled()) {
          return false;
        }
      }
    }

    return hasToggleableEffects;
  }

  /**
   * Toggles the enabled state of all toggleable depth layer effects in this registry.
   *
   * @return the new enabled state after toggling
   */
  public boolean toggleAll() {
    boolean newState = !allEnabled();
    enableAll(newState);
    return newState;
  }

  /**
   * Clears all effects from this registry.
   */
  public void clear() {
    effectMap.clear();
    priorityMap.clear();
    insertionIndexMap.clear();
    sortedByPriority.clear();
  }

  /**
   * Checks whether this registry contains no effects.
   *
   * @return true if the registry is empty, false otherwise
   */
  public boolean isEmpty() {
    return effectMap.isEmpty();
  }

  /**
   * Gets an iterable of all depth layer effects in this registry, sorted by priority and insertion order.
   *
   * @param onlyEnabled if true, only enabled effects are included; if false, all effects are included
   * @return an iterable of depth layer effects in priority order
   */
  public Iterable<DepthLayerEffect> getSorted(boolean onlyEnabled) {
    return () ->
      new Iterator<>() {
        private final Iterator<Set<Entry>> priorityIterator = sortedByPriority.values().iterator();
        private Iterator<Entry> currentSetIterator = Collections.emptyIterator();
        private DepthLayerEffect nextEffect = null;

        @Override
        public boolean hasNext() {
          if (nextEffect != null) {
            return true;
          }

          while (true) {
            if (currentSetIterator.hasNext()) {
              DepthLayerEffect candidate = currentSetIterator.next().effect();
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
        public DepthLayerEffect next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }

          DepthLayerEffect result = nextEffect;
          nextEffect = null;
          return result;
        }
      };
  }

  /**
   * Gets an iterable of all enabled depth layer effects in this registry, sorted by priority and insertion order.
   *
   * @return an iterable of enabled depth layer effects in priority order
   */
  public Iterable<DepthLayerEffect> getEnabledSorted() {
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

  private record Entry(DepthLayerEffect effect, long insertionIndex)
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
   * Optional helper contract for mutable depth layer effects.
   *
   * Implementing classes can be toggled on and off through the registry's enable/disable methods.
   */
  public interface ToggleableDepthLayerEffect extends DepthLayerEffect {
    /**
     * Sets the enabled state of this effect.
     *
     * @param enabled true to enable the effect, false to disable it
     */
    void enabled(boolean enabled);
  }
}
