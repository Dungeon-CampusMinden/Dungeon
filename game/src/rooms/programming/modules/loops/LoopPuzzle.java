package rooms.programming.modules.loops;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Five checkpoints and the 24 collectible programs available at every checkpoint. */
public final class LoopPuzzle {

  private static final List<String> CHALLENGES =
      List.of("forge-press", "bellows", "chain-lift", "cooling-channel", "heart-gate");

  private static final List<LoopRune> RUNES = buildRunes();

  private static List<LoopRune> buildRunes() {
    List<LoopRune> runes = new ArrayList<>();
    for (int index = 0; index < CHALLENGES.size(); index++) {
      String challenge = CHALLENGES.get(index);
      for (LoopType type : LoopType.values()) {
        LoopProgram.Condition condition =
            switch (index) {
              case 0, 4 -> LoopProgram.Condition.AT_GOAL;
              case 1 ->
                  type == LoopType.DO_WHILE
                      ? LoopProgram.Condition.NOT_GOAL
                      : LoopProgram.Condition.FREE;
              default -> LoopProgram.Condition.FREE;
            };
        int count = index == 0 ? 3 : 2;
        List<LoopProgram.Action> body =
            switch (index) {
              case 2 ->
                  type == LoopType.DO_WHILE
                      ? List.of(LoopProgram.Action.MOVE, LoopProgram.Action.ATTACK)
                      : List.of(LoopProgram.Action.ATTACK, LoopProgram.Action.MOVE);
              case 3 -> List.of(LoopProgram.Action.JUMP, LoopProgram.Action.MOVE);
              case 4 ->
                  List.of(
                      LoopProgram.Action.MOVE,
                      LoopProgram.Action.LEFT,
                      LoopProgram.Action.MOVE,
                      LoopProgram.Action.RIGHT);
              default -> List.of(LoopProgram.Action.MOVE);
            };
        List<LoopProgram.Action> after =
            switch (index) {
              case 0 -> List.of(LoopProgram.Action.LEFT);
              case 1 ->
                  List.of(
                      type == LoopType.DO_WHILE
                          ? LoopProgram.Action.RIGHT
                          : LoopProgram.Action.LEFT);
              case 2, 3 -> List.of(LoopProgram.Action.RIGHT);
              default -> List.of();
            };
        String suffix = type.name().toLowerCase(Locale.ROOT).replace('_', '-');
        runes.add(
            new LoopRune(
                challenge + "-" + suffix,
                challenge,
                type,
                "Rune " + (runes.size() + 1) + " · " + suffix,
                new LoopProgram(type, condition, count, body, after)));
      }
    }
    add(
        runes,
        "patrol",
        "Eckenprobe",
        2,
        List.of(LoopProgram.Action.LEFT, LoopProgram.Action.MOVE));
    add(
        runes,
        "turn-left",
        "Linke Ecke",
        1,
        List.of(LoopProgram.Action.LEFT, LoopProgram.Action.MOVE));
    add(
        runes,
        "turn-right",
        "Rechte Ecke",
        1,
        List.of(LoopProgram.Action.RIGHT, LoopProgram.Action.MOVE));
    add(
        runes,
        "backtrack",
        "Kehrtwende",
        1,
        List.of(LoopProgram.Action.LEFT, LoopProgram.Action.LEFT, LoopProgram.Action.MOVE));
    add(runes, "short", "Kurzer Marsch", 1, List.of(LoopProgram.Action.MOVE));
    add(runes, "long", "Langer Marsch", 9, List.of(LoopProgram.Action.MOVE));
    runes.add(
        new LoopRune(
            "archive-spin",
            "archive",
            LoopType.WHILE,
            "Ewiger Kreisel",
            new LoopProgram(
                LoopType.WHILE,
                LoopProgram.Condition.ALWAYS,
                0,
                List.of(LoopProgram.Action.LEFT),
                List.of())));
    add(runes, "jump", "Sprungprobe", 1, List.of(LoopProgram.Action.JUMP));
    add(runes, "attack", "Kampfprobe", 1, List.of(LoopProgram.Action.ATTACK));
    return List.copyOf(runes);
  }

  private static void add(
      List<LoopRune> runes, String id, String title, int count, List<LoopProgram.Action> body) {
    runes.add(
        new LoopRune(
            "archive-" + id,
            "archive",
            LoopType.FOR,
            title,
            new LoopProgram(LoopType.FOR, LoopProgram.Condition.ALWAYS, count, body, List.of())));
  }

  private LoopPuzzle() {}

  /**
   * @return immutable station IDs in progression order
   */
  public static List<String> challenges() {
    return CHALLENGES;
  }

  /**
   * @return all collectible runes in their stable archive order
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
   * Returns the runes authored for a station. This grouping does not restrict execution.
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
