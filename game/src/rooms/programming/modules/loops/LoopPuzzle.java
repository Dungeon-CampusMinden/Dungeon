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

  /** Returns the forge's station order. */
  public static List<String> challenges() {
    return CHALLENGES;
  }

  /** Returns all collectible runes in station and loop-type order. */
  public static List<LoopRune> runes() {
    return RUNES;
  }

  /** Finds a rune by its exact canonical content ID. */
  public static Optional<LoopRune> rune(String id) {
    return RUNES.stream().filter(rune -> rune.id().equals(id)).findFirst();
  }

  /** Returns the three runes belonging to a known station. */
  public static List<LoopRune> runes(String challengeId) {
    return RUNES.stream().filter(rune -> rune.challengeId().equals(challengeId)).toList();
  }

  /** Reports whether every known loop situation has been completed. */
  public static boolean allCompleted(Set<String> challengeIds) {
    return challengeIds != null
        && challengeIds.size() == CHALLENGES.size()
        && challengeIds.containsAll(CHALLENGES);
  }
}
