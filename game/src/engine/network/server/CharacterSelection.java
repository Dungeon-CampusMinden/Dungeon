package engine.network.server;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.network.messages.s2c.CharacterOptions;
import feature.components.CharacterClassComponent;
import feature.entities.CharacterClass;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.types.AvailableCharacterPrefab;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Authoritative character catalog and named character reservations for the current game run.
 *
 * <p>Reservations survive disconnects and are included in saved-game snapshots. They are reset when
 * a new run starts with {@link #clear()}.
 */
public final class CharacterSelection {

  private static final Map<String, CharacterClass> ASSIGNMENTS = new LinkedHashMap<>();

  private CharacterSelection() {}

  /**
   * Returns the marker/fallback catalog without a player-specific assignment.
   *
   * @return catalog options with current reservation counts
   */
  public static synchronized List<CharacterOptions.Option> catalog() {
    return options(null);
  }

  /**
   * Returns catalog options and admission state for a named player.
   *
   * <p>If the name already has a saved assignment, that class remains selectable even when its
   * limit has been reached; all other classes are not selectable for that name.
   *
   * @param playerName player name to include assignment state for, or {@code null} for no
   *     player-specific state
   * @return immutable per-class options including counts and availability
   */
  public static synchronized List<CharacterOptions.Option> options(String playerName) {
    Map<CharacterClass, Integer> catalog = characterLimits();
    CharacterClass assignedClass = playerName == null ? null : ASSIGNMENTS.get(playerName);

    // Keep a saved assignment visible even if the current level's catalog has since changed.
    if (assignedClass != null) {
      catalog.putIfAbsent(assignedClass, 0);
    }

    Map<CharacterClass, Integer> counts = playerCounts();
    List<CharacterOptions.Option> options = new ArrayList<>(catalog.size());
    for (Map.Entry<CharacterClass, Integer> entry : catalog.entrySet()) {
      CharacterClass characterClass = entry.getKey();
      int maxCount = entry.getValue();
      int playerCount = counts.getOrDefault(characterClass, 0);
      boolean assigned = assignedClass == characterClass;
      boolean selectable =
          assigned || (assignedClass == null && hasCapacity(maxCount, playerCount));
      options.add(
          new CharacterOptions.Option(characterClass, maxCount, playerCount, selectable, assigned));
    }
    return List.copyOf(options);
  }

  /**
   * Reserves an available character class for a named local player.
   *
   * <p>This is suitable for single-process games which choose a class before creating the player
   * entity. An existing named assignment is immutable for the duration of the run.
   *
   * @param playerName player identity
   * @param characterClass selected class
   * @return {@code true} if the assignment was accepted or was already the same assignment
   */
  public static synchronized boolean assign(String playerName, CharacterClass characterClass) {
    Objects.requireNonNull(characterClass, "characterClass");
    return reserve(playerName, Optional.of(characterClass), 0).status() == Status.ACCEPTED;
  }

  /**
   * Returns an immutable snapshot of all named assignments, including disconnected reservations and
   * local players created without a network handshake.
   *
   * @return immutable player-name to character-class assignments
   */
  public static synchronized Map<String, CharacterClass> assignments() {
    Map<String, CharacterClass> assignments = new LinkedHashMap<>(ASSIGNMENTS);
    Game.allPlayers()
        .forEach(
            entity -> {
              String name =
                  entity.fetch(PlayerComponent.class).map(PlayerComponent::playerName).orElse(null);
              if (name == null || name.isBlank()) return;
              entity
                  .fetch(CharacterClassComponent.class)
                  .map(CharacterClassComponent::characterClass)
                  .ifPresent(characterClass -> assignments.putIfAbsent(name, characterClass));
            });
    return Collections.unmodifiableMap(assignments);
  }

  /**
   * Replaces the current named assignments with a saved snapshot.
   *
   * <p>This validates identity keys and values but deliberately does not check current catalog
   * limits: saved reservations must be restored before the catalog is necessarily loaded and must
   * remain rejoinable even if the current level's limits have changed.
   *
   * @param assignments saved name-to-class reservations
   * @throws IllegalArgumentException if an assignment has an invalid name or a null class
   */
  public static synchronized void restoreAssignments(Map<String, CharacterClass> assignments) {
    Objects.requireNonNull(assignments, "assignments");
    Map<String, CharacterClass> validated = new LinkedHashMap<>();
    assignments.forEach(
        (name, characterClass) -> {
          if (!isValidPlayerName(name)) {
            throw new IllegalArgumentException("Invalid player name in assignments: " + name);
          }
          validated.put(
              name, Objects.requireNonNull(characterClass, "character class for " + name));
        });
    ASSIGNMENTS.clear();
    ASSIGNMENTS.putAll(validated);
  }

  /** Clears all reservations for a new game run. */
  public static synchronized void clear() {
    ASSIGNMENTS.clear();
  }

  /**
   * Checks for a retained character reservation.
   *
   * @param playerName player identity
   * @return whether the name already has a reservation
   */
  static synchronized boolean hasAssignment(String playerName) {
    return ASSIGNMENTS.containsKey(playerName);
  }

  /**
   * Returns the existing assignment for a player name.
   *
   * @param playerName player identity
   * @return reserved character, if present
   */
  static synchronized Optional<CharacterClass> assignment(String playerName) {
    return Optional.ofNullable(ASSIGNMENTS.get(playerName));
  }

  /**
   * Counts distinct named identities occupying player slots.
   *
   * @return retained and live player count
   */
  static synchronized int identityCount() {
    return identityCount(List.of());
  }

  /**
   * Counts player identities, including authenticated sessions without a reservation.
   *
   * @param sessionNames authenticated session identities
   * @return distinct retained, live, and authenticated player count
   */
  static synchronized int identityCount(Iterable<String> sessionNames) {
    Set<String> identities = new HashSet<>(ASSIGNMENTS.keySet());
    Game.allPlayers()
        .map(entity -> entity.fetch(PlayerComponent.class).orElse(null))
        .filter(Objects::nonNull)
        .map(PlayerComponent::playerName)
        .filter(Objects::nonNull)
        .forEach(identities::add);
    sessionNames.forEach(
        name -> {
          if (name != null) {
            identities.add(name);
          }
        });
    return identities.size();
  }

  /**
   * Retains a session's original class if it is not present in the snapshot.
   *
   * @param playerName player identity
   * @param characterClass original character class
   */
  static synchronized void retainAssignment(String playerName, CharacterClass characterClass) {
    if (isValidPlayerName(playerName) && characterClass != null) {
      ASSIGNMENTS.putIfAbsent(playerName, characterClass);
    }
  }

  /**
   * Atomically validates and reserves the requested class, or chooses a fallback.
   *
   * <p>A single catalog entry is used directly. Otherwise, {@code fallbackStartIndex} preserves the
   * configured server round-robin order when no class is explicitly requested. The caller runs this
   * from the authoritative inbound-message queue.
   *
   * @param playerName player identity
   * @param requested selected class, or empty to use a fallback
   * @param fallbackStartIndex first configured fallback index to consider
   * @return admission status and accepted character, if any
   */
  static synchronized Admission reserve(
      String playerName, Optional<CharacterClass> requested, int fallbackStartIndex) {
    if (!isValidPlayerName(playerName)) {
      return new Admission(Status.UNAVAILABLE, null);
    }
    Objects.requireNonNull(requested, "requested");

    CharacterClass existing = ASSIGNMENTS.get(playerName);
    if (existing != null) {
      if (requested.isPresent() && requested.orElseThrow() != existing) {
        return new Admission(Status.UNAVAILABLE, null);
      }
      return new Admission(Status.ACCEPTED, existing);
    }

    Map<CharacterClass, Integer> limits = characterLimits();
    Map<CharacterClass, Integer> counts = playerCounts();
    if (requested.isPresent()) {
      CharacterClass characterClass = requested.orElseThrow();
      Integer maxCount = limits.get(characterClass);
      if (maxCount == null) {
        return new Admission(Status.UNAVAILABLE, null);
      }
      if (!hasCapacity(maxCount, counts.getOrDefault(characterClass, 0))) {
        return new Admission(Status.FULL, null);
      }
      ASSIGNMENTS.put(playerName, characterClass);
      return new Admission(Status.ACCEPTED, characterClass);
    }

    List<CharacterClass> fallbackClasses =
        limits.size() == 1
            ? List.copyOf(limits.keySet())
            : PreRunConfiguration.multiplayerCharacterClasses();
    boolean foundConfiguredClass = false;
    int start = Math.floorMod(fallbackStartIndex, fallbackClasses.size());
    for (int offset = 0; offset < fallbackClasses.size(); offset++) {
      CharacterClass candidate = fallbackClasses.get((start + offset) % fallbackClasses.size());
      Integer maxCount = limits.get(candidate);
      if (maxCount == null) {
        continue;
      }
      foundConfiguredClass = true;
      if (hasCapacity(maxCount, counts.getOrDefault(candidate, 0))) {
        ASSIGNMENTS.put(playerName, candidate);
        return new Admission(Status.ACCEPTED, candidate);
      }
    }
    return new Admission(foundConfiguredClass ? Status.FULL : Status.UNAVAILABLE, null);
  }

  private static boolean isValidPlayerName(String playerName) {
    return playerName != null && !playerName.isBlank() && !playerName.contains("_");
  }

  private static boolean hasCapacity(int maxCount, int playerCount) {
    return maxCount < 0 || playerCount < maxCount;
  }

  /**
   * Builds the current level catalog, falling back to the configured multiplayer classes
   * (unlimited) when that level has no available-character markers.
   *
   * @return class limits in catalog order
   */
  private static Map<CharacterClass, Integer> characterLimits() {
    Map<CharacterClass, Integer> limits = new LinkedHashMap<>();
    boolean foundMarker = false;
    var level = Game.currentLevel().orElse(null);
    if (level != null) {
      for (PrefabInstance instance : level.activePrefabs()) {
        if (!(PrefabRegistry.find(instance.type()).orElse(null)
            instanceof AvailableCharacterPrefab)) {
          continue;
        }
        foundMarker = true;
        CharacterClass characterClass =
            CharacterClass.valueOf(AvailableCharacterPrefab.CHARACTER_CLASS.get(instance));
        int maxCount = AvailableCharacterPrefab.MAX_COUNT.get(instance);
        limits.merge(characterClass, maxCount, CharacterSelection::aggregateLimit);
      }
    }

    if (!foundMarker) {
      PreRunConfiguration.multiplayerCharacterClasses()
          .forEach(characterClass -> limits.put(characterClass, -1));
    }
    return limits;
  }

  /**
   * Combines duplicate limits, with unlimited markers dominating and finite sums saturating.
   *
   * @param first first marker's limit
   * @param second second marker's limit
   * @return combined limit
   */
  private static int aggregateLimit(int first, int second) {
    if (first < 0 || second < 0) {
      return -1;
    }
    long sum = (long) first + second;
    return sum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
  }

  /**
   * Counts retained named assignments and live Game players without double-counting names present
   * in both sources.
   *
   * @return occupied slots per character class
   */
  private static Map<CharacterClass, Integer> playerCounts() {
    Map<CharacterClass, Integer> counts = new EnumMap<>(CharacterClass.class);
    Set<String> countedNames = new LinkedHashSet<>();
    ASSIGNMENTS.forEach(
        (name, characterClass) -> {
          countedNames.add(name);
          counts.merge(characterClass, 1, Integer::sum);
        });

    for (Entity entity : Game.allPlayers().toList()) {
      PlayerComponent player = entity.fetch(PlayerComponent.class).orElse(null);
      CharacterClassComponent character = entity.fetch(CharacterClassComponent.class).orElse(null);
      if (player == null || character == null) {
        continue;
      }
      String playerName = player.playerName();
      if (playerName != null && !countedNames.add(playerName)) {
        continue;
      }
      counts.merge(character.characterClass(), 1, Integer::sum);
    }
    return counts;
  }

  enum Status {
    ACCEPTED,
    UNAVAILABLE,
    FULL
  }

  record Admission(Status status, CharacterClass characterClass) {}
}
