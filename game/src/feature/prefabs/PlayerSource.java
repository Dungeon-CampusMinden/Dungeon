package feature.prefabs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Where the player an event of an instance carries comes from, see {@link Prefab#playerSource}. The
 * level editor follows it to check whether actions that need a player get one, see {@link
 * EventPlayers}.
 *
 * @param withPlayer whether the instance raises the event by itself, with the player that caused it
 * @param withoutPlayer whether the instance raises the event by itself, without a player
 * @param actions keys of the actions of the instance raising the event with the player of their
 *     triggering event, e.g. "Start" of a delay
 * @param flags game flags whose changes raise the event, with the player that changed the flag
 */
public record PlayerSource(
    boolean withPlayer, boolean withoutPlayer, Set<String> actions, Set<String> flags) {

  /** The instance raises the event by itself, always with the player that caused it. */
  public static final PlayerSource PLAYER = new PlayerSource(true, false, Set.of(), Set.of());

  /** The instance raises the event by itself, never with a player. */
  public static final PlayerSource NO_PLAYER = new PlayerSource(false, true, Set.of(), Set.of());

  /** Copies the action keys and flags. */
  public PlayerSource {
    actions = Set.copyOf(actions);
    flags = Set.copyOf(flags);
  }

  /**
   * Creates the source of an event that actions of the instance raise, with the player of their
   * triggering event.
   *
   * @param actions actions of the prefab type
   * @return the source
   */
  public static PlayerSource passing(PrefabAction... actions) {
    Set<String> keys = new HashSet<>();
    for (PrefabAction action : actions) keys.add(action.key());
    return new PlayerSource(false, false, keys, Set.of());
  }

  /**
   * Creates the source of an event that changes of game flags raise, with the player that changed
   * the flag.
   *
   * @param flags watched game flags
   * @return the source
   */
  public static PlayerSource flags(Collection<String> flags) {
    return new PlayerSource(false, false, Set.of(), Set.copyOf(flags));
  }

  /**
   * Combines two sources of an event.
   *
   * @param other another source of the same event
   * @return the source of both
   */
  public PlayerSource or(PlayerSource other) {
    Set<String> allActions = new HashSet<>(actions);
    allActions.addAll(other.actions);
    Set<String> allFlags = new HashSet<>(flags);
    allFlags.addAll(other.flags);
    return new PlayerSource(
        withPlayer || other.withPlayer, withoutPlayer || other.withoutPlayer, allActions, allFlags);
  }
}
