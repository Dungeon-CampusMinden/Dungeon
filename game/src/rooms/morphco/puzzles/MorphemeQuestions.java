package rooms.morphco.puzzles;

/** Numeric questions that guard the maintenance hatch of A-12 and the restart of the production. */
public final class MorphemeQuestions {

  /**
   * Types of free morphemes without the portmanteau morpheme: verbal, pronominal, nominal,
   * prepositional, adjectival, conjunctional and adverbial base morphemes and articles.
   */
  public static final String HATCH_CODE = "8";

  /** Morphemes in "Überlegungen": {über} + {leg} + {ung} + {en}. */
  public static final String RESTART_CODE = "4";

  private MorphemeQuestions() {}

  /**
   * Checks the answer at the maintenance hatch.
   *
   * @param answer entered digits
   * @return whether the hatch unlocks
   */
  public static boolean hatchAnswer(String answer) {
    return HATCH_CODE.equals(normalize(answer));
  }

  /**
   * Checks the answer to the restart security question.
   *
   * @param answer entered digits
   * @return whether the restart starts
   */
  public static boolean restartAnswer(String answer) {
    return RESTART_CODE.equals(normalize(answer));
  }

  private static String normalize(String answer) {
    if (answer == null) return "";
    String trimmed = answer.trim();
    int start = 0;
    while (start < trimmed.length() - 1 && trimmed.charAt(start) == '0') start++;
    return trimmed.substring(start);
  }
}
