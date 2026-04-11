package core.platform.litiengine.render.effects;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ordered collection of LITIENGINE sprite effects.
 *
 * <p>Effects are sorted first by priority, then by insertion order.
 */
public final class LitiengineSpriteEffects {

  private final Map<String, Entry> entries = new LinkedHashMap<>();
  private long insertionCounter = 0L;

  /**
   * Adds a new effect.
   *
   * @param identifier unique identifier
   * @param effect effect instance
   * @param priority lower values are applied earlier
   * @return true if added, false if identifier already exists
   */
  public boolean add(String identifier, LitiengineSpriteEffect effect, int priority) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(effect, "effect");

    if (entries.containsKey(identifier)) {
      return false;
    }

    entries.put(identifier, new Entry(effect, priority, insertionCounter++));
    return true;
  }

  /**
   * Removes an effect by identifier.
   *
   * @param identifier unique identifier
   * @return removed effect if present
   */
  public Optional<LitiengineSpriteEffect> remove(String identifier) {
    Entry removed = entries.remove(identifier);
    return removed == null ? Optional.empty() : Optional.of(removed.effect());
  }

  /**
   * Returns an effect by identifier.
   *
   * @param identifier unique identifier
   * @return effect if present
   */
  public Optional<LitiengineSpriteEffect> get(String identifier) {
    Entry entry = entries.get(identifier);
    return entry == null ? Optional.empty() : Optional.of(entry.effect());
  }

  /**
   * Returns all enabled effects in stable application order.
   *
   * @return enabled effects sorted by priority and insertion order
   */
  public List<LitiengineSpriteEffect> getEnabledSorted() {
    return entries.values().stream()
      .sorted(
        Comparator.comparingInt(Entry::priority).thenComparingLong(Entry::insertionIndex))
      .map(Entry::effect)
      .filter(LitiengineSpriteEffect::enabled)
      .collect(Collectors.toList());
  }

  /** @return true if no effects are stored */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  private record Entry(LitiengineSpriteEffect effect, int priority, long insertionIndex) {}
}
