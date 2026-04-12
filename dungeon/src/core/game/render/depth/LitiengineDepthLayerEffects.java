package core.game.render.depth;

import java.util.*;

/**
 * Ordered collection of LITIENGINE effects for a single entity depth layer.
 *
 * <p>Effects are sorted first by priority and then by insertion order.
 */
public final class LitiengineDepthLayerEffects {

  private long insertionCounter = 0L;

  private final Map<String, LitiengineDepthLayerEffect> effectMap = new HashMap<>();
  private final Map<String, Integer> priorityMap = new HashMap<>();
  private final Map<String, Long> insertionIndexMap = new HashMap<>();
  private final TreeMap<Integer, Set<Entry>> sortedByPriority = new TreeMap<>();

  public boolean add(String identifier, LitiengineDepthLayerEffect effect, int priority) {
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

  public boolean add(String identifier, LitiengineDepthLayerEffect effect) {
    return add(identifier, effect, 0);
  }

  public boolean remove(String identifier) {
    if (!effectMap.containsKey(identifier)) {
      return false;
    }

    LitiengineDepthLayerEffect removedEffect = effectMap.remove(identifier);
    Integer priority = priorityMap.remove(identifier);
    Long insertionIndex = insertionIndexMap.remove(identifier);

    if (priority != null && insertionIndex != null) {
      removeFromSortedMap(new Entry(removedEffect, insertionIndex), priority);
    }

    return true;
  }

  public Optional<LitiengineDepthLayerEffect> get(String identifier) {
    return Optional.ofNullable(effectMap.get(identifier));
  }

  public boolean changePriority(String identifier, int newPriority) {
    LitiengineDepthLayerEffect effect = effectMap.get(identifier);
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

  public boolean hasEnabledEffects() {
    for (LitiengineDepthLayerEffect effect : effectMap.values()) {
      if (effect.enabled()) {
        return true;
      }
    }
    return false;
  }

  public void enableAll(boolean enabled) {
    for (LitiengineDepthLayerEffect effect : effectMap.values()) {
      if (effect instanceof ToggleableDepthLayerEffect toggleable) {
        toggleable.enabled(enabled);
      }
    }
  }

  public void enableAll() {
    enableAll(true);
  }

  public void disableAll() {
    enableAll(false);
  }

  public boolean allEnabled() {
    boolean hasToggleableEffects = false;

    for (LitiengineDepthLayerEffect effect : effectMap.values()) {
      if (effect instanceof ToggleableDepthLayerEffect) {
        hasToggleableEffects = true;
        if (!effect.enabled()) {
          return false;
        }
      }
    }

    return hasToggleableEffects;
  }

  public boolean toggleAll() {
    boolean newState = !allEnabled();
    enableAll(newState);
    return newState;
  }

  public void clear() {
    effectMap.clear();
    priorityMap.clear();
    insertionIndexMap.clear();
    sortedByPriority.clear();
  }

  public boolean isEmpty() {
    return effectMap.isEmpty();
  }

  public Iterable<LitiengineDepthLayerEffect> getSorted(boolean onlyEnabled) {
    return () ->
      new Iterator<>() {
        private final Iterator<Set<Entry>> priorityIterator = sortedByPriority.values().iterator();
        private Iterator<Entry> currentSetIterator = Collections.emptyIterator();
        private LitiengineDepthLayerEffect nextEffect = null;

        @Override
        public boolean hasNext() {
          if (nextEffect != null) {
            return true;
          }

          while (true) {
            if (currentSetIterator.hasNext()) {
              LitiengineDepthLayerEffect candidate = currentSetIterator.next().effect();
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
        public LitiengineDepthLayerEffect next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }

          LitiengineDepthLayerEffect result = nextEffect;
          nextEffect = null;
          return result;
        }
      };
  }

  public Iterable<LitiengineDepthLayerEffect> getEnabledSorted() {
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

  private record Entry(LitiengineDepthLayerEffect effect, long insertionIndex)
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

  /** Optional helper contract for mutable depth-layer effects. */
  public interface ToggleableDepthLayerEffect extends LitiengineDepthLayerEffect {
    void enabled(boolean enabled);
  }
}
