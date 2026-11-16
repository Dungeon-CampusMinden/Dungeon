package feature.prefabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Whether the events of the instances of a level carry the player that caused them. An event
 * passing on a player, e.g. "Finished" of a delay, carries the players of the events linked to the
 * actions raising it, see {@link Prefab#playerSource}.
 *
 * <p>Actions that need a player run for every player if their triggering event carries none, see
 * {@link PrefabAction#needsPlayer}. Actions run by level code are not known here.
 */
public final class EventPlayers {

  private static final int WITH = 1;
  private static final int WITHOUT = 2;

  // Players of every event, by instance and event key.
  private final Map<String, Integer> events;

  /** Whether an event carries a player. */
  public enum Presence {
    /** The event is never raised, e.g. it passes on the player of an action nothing triggers. */
    NONE,
    /** The event always carries a player. */
    ALWAYS,
    /** The event carries a player only some of the times it is raised. */
    SOMETIMES,
    /** The event never carries a player. */
    NEVER
  }

  private EventPlayers(Map<String, Integer> events) {
    this.events = events;
  }

  /**
   * Follows the players through the links of a level.
   *
   * @param instances instances of the level; invalid ones and unknown prefab types are skipped
   * @return the players of every event
   */
  public static EventPlayers of(List<PrefabInstance> instances) {
    List<Source> sources = new ArrayList<>();
    List<FlagChange> flagChanges = new ArrayList<>();
    for (PrefabInstance instance : instances) {
      Optional<Prefab> prefab = PrefabRegistry.find(instance.type());
      if (prefab.isEmpty()) continue;
      PrefabInstance normalized;
      try {
        normalized = prefab.get().normalize(instance);
      } catch (IllegalArgumentException invalid) {
        continue;
      }
      for (PrefabPort port : prefab.get().ports(normalized, PrefabPort.Kind.EVENT)) {
        PlayerSource source = prefab.get().playerSource(normalized, (PrefabEvent<?>) port);
        sources.add(new Source(key(normalized.name(), port.key()), normalized.name(), source));
      }
      for (PrefabPort port : prefab.get().ports(normalized, PrefabPort.Kind.ACTION)) {
        Set<String> flags = prefab.get().changedFlags(normalized, (PrefabAction) port);
        if (!flags.isEmpty()) {
          flagChanges.add(new FlagChange(key(normalized.name(), port.key()), flags));
        }
      }
    }
    List<PrefabWiring.Connection> signals =
        PrefabWiring.connections(instances).stream()
            .filter(connection -> connection.input().kind() == PrefabPort.Kind.ACTION)
            .toList();

    Map<String, Integer> events = new HashMap<>();
    Map<String, Integer> actions = new HashMap<>();
    Map<String, Integer> flags = new HashMap<>();
    // The players only ever grow, so this ends once nothing grows anymore, also with loops.
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Source source : sources) {
        PlayerSource origin = source.origin();
        int players = (origin.withPlayer() ? WITH : 0) | (origin.withoutPlayer() ? WITHOUT : 0);
        for (String action : origin.actions()) {
          players |= actions.getOrDefault(key(source.instance(), action), 0);
        }
        for (String flag : origin.flags()) players |= flags.getOrDefault(flag, 0);
        changed |= grow(events, source.event(), players);
      }
      for (PrefabWiring.Connection connection : signals) {
        PrefabLink link = connection.link();
        int players = events.getOrDefault(key(link.source(), link.port()), 0);
        String action = key(connection.receiver().name(), connection.input().key());
        changed |= grow(actions, action, players);
      }
      for (FlagChange change : flagChanges) {
        int players = actions.getOrDefault(change.action(), 0);
        for (String flag : change.flags()) changed |= grow(flags, flag, players);
      }
    }
    return new EventPlayers(events);
  }

  /**
   * Returns whether an event of an instance carries a player.
   *
   * @param instance name of the instance
   * @param event key of the event
   * @return the presence of the player; {@link Presence#NONE} for unknown events
   */
  public Presence of(String instance, String event) {
    return presence(events.getOrDefault(key(instance, event), 0));
  }

  private static Presence presence(int players) {
    return switch (players) {
      case WITH -> Presence.ALWAYS;
      case WITHOUT -> Presence.NEVER;
      case WITH | WITHOUT -> Presence.SOMETIMES;
      default -> Presence.NONE;
    };
  }

  /**
   * Returns whether the event a link points at carries a player.
   *
   * @param link link from an event
   * @return the presence of the player; {@link Presence#NONE} if the link points at no event
   */
  public Presence of(PrefabLink link) {
    return of(link.source(), link.port());
  }

  private static boolean grow(Map<String, Integer> players, String key, int added) {
    int current = players.getOrDefault(key, 0);
    if ((current | added) == current) return false;
    players.put(key, current | added);
    return true;
  }

  private static String key(String instance, String port) {
    return instance + "\n" + port;
  }

  private record Source(String event, String instance, PlayerSource origin) {}

  private record FlagChange(String action, Set<String> flags) {}
}
