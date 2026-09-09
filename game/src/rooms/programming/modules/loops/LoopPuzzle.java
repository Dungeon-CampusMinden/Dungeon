package rooms.programming.modules.loops;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Ordered forge stations and their collectible loop forms. */
public final class LoopPuzzle {

  private static final List<String> CHALLENGES =
      List.of("forge-press", "bellows", "chain-lift", "cooling-channel", "heart-gate");

  private static final List<LoopRune> RUNES =
      CHALLENGES.stream()
          .flatMap(
              challenge ->
                  Arrays.stream(LoopType.values())
                      .map(
                          type ->
                              new LoopRune(
                                  challenge
                                      + "-"
                                      + type.name().toLowerCase(Locale.ROOT).replace('_', '-'),
                                  challenge,
                                  type)))
          .toList();

  private LoopPuzzle() {}

  /**
   * @return immutable station IDs in progression order
   */
  public static List<String> challenges() {
    return CHALLENGES;
  }

  /**
   * @return all collectible runes in station and loop-type order
   */
  public static List<LoopRune> runes() {
    return RUNES;
  }

  /**
   * Finds a rune by its exact canonical content ID.
   *
   * @param id the rune ID
   * @return the matching rune, or empty for an unknown ID
   */
  public static Optional<LoopRune> rune(String id) {
    return RUNES.stream().filter(rune -> rune.id().equals(id)).findFirst();
  }

  /**
   * Returns the runes belonging to a station.
   *
   * @param challengeId the station ID
   * @return its three runes, or an empty list for an unknown station
   */
  public static List<LoopRune> runes(String challengeId) {
    return RUNES.stream().filter(rune -> rune.challengeId().equals(challengeId)).toList();
  }

  /**
   * Reports whether every known loop situation has been completed.
   *
   * @param challengeIds completed station IDs
   * @return true if the IDs match the complete set of stations
   */
  public static boolean allCompleted(Set<String> challengeIds) {
    return challengeIds != null
        && challengeIds.size() == CHALLENGES.size()
        && challengeIds.containsAll(CHALLENGES);
  }
}
