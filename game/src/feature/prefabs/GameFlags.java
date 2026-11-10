package feature.prefabs;

import engine.Entity;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * The flags of the running game. A flag is a marker with a name that either exists or does not.
 *
 * <p>The list is kept where the game has authority, i.e. on the server or in singleplayer. It does
 * not belong to a level, so a flag set in one level can be read in the next one. It is empty when
 * the game starts and reset when the level editor opens its prefab mode.
 *
 * <p>Flags are part of the game state: a save stores {@link #snapshot()} and passes it to {@link
 * #restore} once the saved level has been loaded again.
 */
public final class GameFlags {

  private static final Set<String> FLAGS = new LinkedHashSet<>();
  // Kept in registration order, so listeners are called in the order their prefabs spawned.
  private static final Set<Subscription> SUBSCRIPTIONS = new LinkedHashSet<>();

  private GameFlags() {}

  /**
   * Returns whether a flag exists.
   *
   * @param name flag name
   * @return true if the flag was set and not removed since
   */
  public static boolean isSet(String name) {
    return FLAGS.contains(name);
  }

  /**
   * Sets a flag. Does nothing if the flag already exists.
   *
   * @param name flag name
   * @param player player that caused the change, passed on to the listeners
   */
  public static void set(String name, Optional<Entity> player) {
    if (FLAGS.add(name)) notifyListeners(Map.of(name, true), player, false);
  }

  /**
   * Removes a flag. Does nothing if the flag does not exist.
   *
   * @param name flag name
   * @param player player that caused the change, passed on to the listeners
   */
  public static void remove(String name, Optional<Entity> player) {
    if (FLAGS.remove(name)) notifyListeners(Map.of(name, false), player, false);
  }

  /**
   * Removes a flag if it exists, otherwise sets it.
   *
   * @param name flag name
   * @param player player that caused the change, passed on to the listeners
   */
  public static void toggle(String name, Optional<Entity> player) {
    if (isSet(name)) remove(name, player);
    else set(name, player);
  }

  /**
   * Removes every flag without notifying the listeners.
   *
   * <p>Prefabs that remember flag states, e.g. the flag triggers, must be respawned afterwards, as
   * the level editor does when it opens its prefab mode.
   */
  public static void clear() {
    FLAGS.clear();
  }

  /**
   * Returns the existing flags for a save.
   *
   * @return names of all existing flags, sorted
   */
  public static List<String> snapshot() {
    return List.copyOf(new TreeSet<>(FLAGS));
  }

  /**
   * Replaces the existing flags with saved ones, e.g. from {@link #snapshot()}.
   *
   * <p>Every listener is called once with all of its flags that changed, marked as {@link
   * Change#restored() restored}, so it can tell the restore apart from changes during play. Call
   * this after the level of the save has been loaded, otherwise its listeners miss the restore.
   *
   * @param saved names of the flags that exist after the restore
   */
  public static void restore(Collection<String> saved) {
    Set<String> restored = new LinkedHashSet<>(saved);
    Map<String, Boolean> changed = new LinkedHashMap<>();
    for (String name : FLAGS) if (!restored.contains(name)) changed.put(name, false);
    for (String name : restored) if (!FLAGS.contains(name)) changed.put(name, true);
    FLAGS.clear();
    FLAGS.addAll(restored);
    notifyListeners(changed, Optional.empty(), true);
  }

  /**
   * Registers a listener called whenever some of the given flags start or stop existing.
   *
   * @param names watched flag names
   * @param listener called after the change
   * @return removes the listener again
   */
  public static Runnable listen(Collection<String> names, Listener listener) {
    Subscription subscription =
        new Subscription(Set.copyOf(names), Objects.requireNonNull(listener, "listener"));
    SUBSCRIPTIONS.add(subscription);
    return () -> SUBSCRIPTIONS.remove(subscription);
  }

  private static void notifyListeners(
      Map<String, Boolean> changed, Optional<Entity> player, boolean restored) {
    if (changed.isEmpty()) return;
    for (Subscription subscription : List.copyOf(SUBSCRIPTIONS)) {
      // An earlier listener may have removed this one, e.g. by despawning its prefab.
      if (!SUBSCRIPTIONS.contains(subscription)) continue;
      Map<String, Boolean> watched = new LinkedHashMap<>(changed);
      watched.keySet().retainAll(subscription.names);
      if (watched.isEmpty()) continue;
      subscription.listener.flagsChanged(new Change(Map.copyOf(watched), player, restored));
    }
  }

  /**
   * Changes of the watched flags of one listener.
   *
   * <p>A change during play has exactly one flag. A restore has every watched flag that started or
   * stopped existing through it.
   *
   * @param flags changed flags, each with whether it exists now
   * @param player player that caused the change, empty if none did, e.g. for a restore
   * @param restored whether the change comes from {@link #restore restoring} saved flags
   */
  public record Change(Map<String, Boolean> flags, Optional<Entity> player, boolean restored) {

    /**
     * Returns whether a flag started or stopped existing through this change.
     *
     * @param exists true for flags that started existing, false for flags that stopped
     * @return true if one of the flags changed that way
     */
    public boolean any(boolean exists) {
      return flags.containsValue(exists);
    }
  }

  /** Receives the changes of the watched flags. */
  @FunctionalInterface
  public interface Listener {
    /**
     * Called after some of the watched flags started or stopped existing.
     *
     * @param change the changed flags
     */
    void flagsChanged(Change change);
  }

  // Not a record, two equal registrations must stay separate.
  private static final class Subscription {
    private final Set<String> names;
    private final Listener listener;

    private Subscription(Set<String> names, Listener listener) {
      this.names = names;
      this.listener = listener;
    }
  }
}
