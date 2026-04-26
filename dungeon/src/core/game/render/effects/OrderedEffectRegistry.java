package core.game.render.effects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Generic priority-ordered registry for render effects.
 *
 * <p>Effects are stored by identifier and iterated by ascending priority. Effects with the same
 * priority keep insertion order.
 *
 * @param <E> effect type stored in the registry
 */
public final class OrderedEffectRegistry<E> {

  private final Predicate<? super E> enabledPredicate;
  private final Predicate<? super E> toggleablePredicate;
  private final BiConsumer<? super E, Boolean> enabledSetter;

  private long insertionCounter = 0L;

  private final Map<String, E> effectMap = new HashMap<>();
  private final Map<String, Integer> priorityMap = new HashMap<>();
  private final Map<String, Long> insertionIndexMap = new HashMap<>();
  private final TreeMap<Integer, Set<Entry<E>>> sortedByPriority = new TreeMap<>();

  /**
   * Creates a new ordered effect registry.
   *
   * @param enabledPredicate predicate returning whether an effect is enabled
   * @param toggleablePredicate predicate returning whether an effect can be toggled by the registry
   * @param enabledSetter setter used to toggle mutable effects
   */
  public OrderedEffectRegistry(
      Predicate<? super E> enabledPredicate,
      Predicate<? super E> toggleablePredicate,
      BiConsumer<? super E, Boolean> enabledSetter) {
    this.enabledPredicate = Objects.requireNonNull(enabledPredicate, "enabledPredicate");
    this.toggleablePredicate = Objects.requireNonNull(toggleablePredicate, "toggleablePredicate");
    this.enabledSetter = Objects.requireNonNull(enabledSetter, "enabledSetter");
  }

  /**
   * Adds an effect to the registry with the specified identifier and priority.
   *
   * @param identifier unique effect identifier
   * @param effect effect to add
   * @param priority priority level; lower values are processed earlier
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, E effect, int priority) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(effect, "effect");

    if (effectMap.containsKey(identifier)) {
      return false;
    }

    long insertionIndex = insertionCounter++;
    Entry<E> entry = new Entry<>(effect, insertionIndex);

    effectMap.put(identifier, effect);
    priorityMap.put(identifier, priority);
    insertionIndexMap.put(identifier, insertionIndex);
    sortedByPriority.computeIfAbsent(priority, ignored -> new TreeSet<>()).add(entry);
    return true;
  }

  /**
   * Adds an effect to the registry with default priority 0.
   *
   * @param identifier unique effect identifier
   * @param effect effect to add
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, E effect) {
    return add(identifier, effect, 0);
  }

  /**
   * Removes an effect by identifier.
   *
   * @param identifier unique effect identifier
   * @return true if an effect was removed, false if no effect exists for the identifier
   */
  public boolean remove(String identifier) {
    if (!effectMap.containsKey(identifier)) {
      return false;
    }

    E removedEffect = effectMap.remove(identifier);
    Integer priority = priorityMap.remove(identifier);
    Long insertionIndex = insertionIndexMap.remove(identifier);

    if (priority != null && insertionIndex != null) {
      removeFromSortedMap(new Entry<>(removedEffect, insertionIndex), priority);
    }

    return true;
  }

  /**
   * Returns an effect by identifier.
   *
   * @param identifier unique effect identifier
   * @return effect if present
   */
  public Optional<E> get(String identifier) {
    return Optional.ofNullable(effectMap.get(identifier));
  }

  /**
   * Changes the priority of an existing effect.
   *
   * @param identifier unique effect identifier
   * @param newPriority new priority level
   * @return true if the priority was changed or already matched, false if no effect exists
   */
  public boolean changePriority(String identifier, int newPriority) {
    E effect = effectMap.get(identifier);
    Integer oldPriority = priorityMap.get(identifier);
    Long insertionIndex = insertionIndexMap.get(identifier);

    if (effect == null || oldPriority == null || insertionIndex == null) {
      return false;
    }

    if (oldPriority == newPriority) {
      return true;
    }

    Entry<E> entry = new Entry<>(effect, insertionIndex);
    removeFromSortedMap(entry, oldPriority);
    priorityMap.put(identifier, newPriority);
    sortedByPriority.computeIfAbsent(newPriority, ignored -> new TreeSet<>()).add(entry);
    return true;
  }

  /**
   * Checks whether any registered effect is enabled.
   *
   * @return true if at least one effect is enabled
   */
  public boolean hasEnabledEffects() {
    for (E effect : effectMap.values()) {
      if (enabledPredicate.test(effect)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the enabled state for all toggleable effects.
   *
   * @param enabled true to enable all toggleable effects, false to disable them
   */
  public void enableAll(boolean enabled) {
    for (E effect : effectMap.values()) {
      if (toggleablePredicate.test(effect)) {
        enabledSetter.accept(effect, enabled);
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
   * Checks whether all toggleable effects are enabled.
   *
   * @return true if at least one toggleable effect exists and all toggleable effects are enabled
   */
  public boolean allEnabled() {
    boolean hasToggleableEffects = false;

    for (E effect : effectMap.values()) {
      if (toggleablePredicate.test(effect)) {
        hasToggleableEffects = true;
        if (!enabledPredicate.test(effect)) {
          return false;
        }
      }
    }

    return hasToggleableEffects;
  }

  /**
   * Toggles all toggleable effects.
   *
   * @return the new enabled state that was applied
   */
  public boolean toggleAll() {
    boolean newState = !allEnabled();
    enableAll(newState);
    return newState;
  }

  /** Removes all effects from this registry. */
  public void clear() {
    effectMap.clear();
    priorityMap.clear();
    insertionIndexMap.clear();
    sortedByPriority.clear();
  }

  /**
   * Checks whether the registry is empty.
   *
   * @return true if no effects are registered
   */
  public boolean isEmpty() {
    return effectMap.isEmpty();
  }

  /**
   * Returns effects sorted by priority and insertion order.
   *
   * @param onlyEnabled whether only enabled effects should be returned
   * @return iterable view over sorted effects
   */
  public Iterable<E> getSorted(boolean onlyEnabled) {
    return () ->
        new Iterator<>() {
          private final Iterator<Set<Entry<E>>> priorityIterator =
              sortedByPriority.values().iterator();
          private Iterator<Entry<E>> currentSetIterator = Collections.emptyIterator();
          private E nextEffect = null;

          @Override
          public boolean hasNext() {
            if (nextEffect != null) {
              return true;
            }

            while (true) {
              if (currentSetIterator.hasNext()) {
                E candidate = currentSetIterator.next().effect();
                if (!onlyEnabled || enabledPredicate.test(candidate)) {
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
          public E next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }

            E result = nextEffect;
            nextEffect = null;
            return result;
          }
        };
  }

  /**
   * Returns enabled effects sorted by priority and insertion order.
   *
   * @return iterable view over enabled effects
   */
  public Iterable<E> getEnabledSorted() {
    return getSorted(true);
  }

  private void removeFromSortedMap(Entry<E> entry, int priority) {
    Set<Entry<E>> set = sortedByPriority.get(priority);
    if (set != null) {
      set.remove(entry);
      if (set.isEmpty()) {
        sortedByPriority.remove(priority);
      }
    }
  }

  private record Entry<E>(E effect, long insertionIndex) implements Comparable<Entry<E>> {
    @Override
    public int compareTo(Entry<E> other) {
      return Long.compare(this.insertionIndex, other.insertionIndex);
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Entry<?> other && this.insertionIndex == other.insertionIndex;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(insertionIndex);
    }
  }
}
