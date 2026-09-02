package rooms.programming.modules.loops;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Known loop situations from the Programming 1 room concept. */
public final class LoopPuzzle {

  private static final List<LoopChallenge> CHALLENGES =
      List.of(
          challenge("long-corridor", LoopType.WHILE),
          challenge("wall-left", LoopType.WHILE),
          challenge("pressure-plate", LoopType.DO_WHILE),
          challenge("five-movement-crystals", LoopType.FOR),
          challenge("wall-right", LoopType.WHILE),
          challenge("magic-bridge", LoopType.DO_WHILE),
          challenge("burning-torches", LoopType.WHILE),
          challenge("three-rune-stones", LoopType.FOR),
          challenge("fog-corridor", LoopType.DO_WHILE),
          challenge("seven-crystals", LoopType.FOR),
          challenge("locked-gate", LoopType.WHILE),
          challenge("magic-rune", LoopType.DO_WHILE),
          challenge("four-guardians", LoopType.FOR),
          challenge("exit-in-fog", LoopType.WHILE),
          challenge("final-passage", LoopType.FOR));

  private LoopPuzzle() {}

  /** Returns the concept's current challenge order. */
  public static List<LoopChallenge> challenges() {
    return CHALLENGES;
  }

  /** Finds a challenge by its stable content ID. */
  public static Optional<LoopChallenge> challenge(String id) {
    if (id == null) {
      return Optional.empty();
    }
    return CHALLENGES.stream().filter(challenge -> challenge.id().equals(id)).findFirst();
  }

  /** Reports whether one answer matches the challenge. */
  public static boolean answerCorrect(String challengeId, LoopType answer) {
    return challenge(challengeId).map(challenge -> challenge.accepts(answer)).orElse(false);
  }

  /** Reports whether a complete answer set solves all current challenges. */
  public static boolean solved(Map<String, LoopType> answers) {
    return answers != null
        && answers.size() == CHALLENGES.size()
        && CHALLENGES.stream()
            .allMatch(challenge -> challenge.accepts(answers.get(challenge.id())));
  }

  private static LoopChallenge challenge(String id, LoopType expectedType) {
    return new LoopChallenge(id, expectedType);
  }
}
