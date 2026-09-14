package rooms.systemRecovery.petrinet;

/**
 * Player-facing places in the System Recovery progress net.
 *
 * <p>Each place represents one actionable recovery state. The Petri net only stores the active
 * token; the attached {@code HintComponent} is read by {@code HintSystem} when the player asks the
 * telephone for help.
 */
public enum SystemRecoveryProgressPlace {
  OPENING_CALL_PENDING("opening-call", "riddle1", null),
  R1_ARRAY("r1-array", "riddle1", "energy-array"),
  R1_VALUES("r1-values", "riddle1", "energy-values"),
  R1_BATTERY("r1-battery", "riddle1", "energy-battery"),
  R2_ARRAY("r2-array", "riddle2", "module-array"),
  R2_VALUES("r2-values", "riddle2", "module-values"),
  R2_GPU("r2-gpu", "riddle2", "remove-gpu"),
  R2_LENGTH("r2-length", "riddle2", "module-length"),
  R2_DISPLAY("r2-display", "riddle2", "open-scanner-door"),
  R2_DOOR("r2-door", "riddle2", "open-scanner-door"),
  R3_COUNT("r3-count", "riddle3", "scanner-code"),
  R3_LEVER("r3-lever", "riddle3", "scanner-lever"),
  R3_SCAN("r3-scan", "riddle3", "scanner-lever"),
  R3_DOOR("r3-door", "riddle3", "scanner-door"),
  R4_ARRAY("r4-array", "riddle4", "packages-array"),
  R4_COLLECT("r4-collect", "riddle4", "packages-loop"),
  R5_COMPARE("r5-compare", "riddle5", "manual-sorting"),
  R6_STICK("r6-stick", "riddle6", "bubble-sort-code"),
  R6_CODE("r6-code", "riddle6", "bubble-sort-code"),
  R6_MACHINE("r6-machine", "riddle6", "bubble-sort-code"),
  R7_ARCHIVE("r7-archive", "riddle7", "archive-arrays"),
  R8_ARRAY("r8-array", "riddle8", "storage-array"),
  R8_VALUES("r8-values", "riddle8", "storage-values"),
  R8_CHIP("r8-chip", "riddle8", "storage-unlocked"),
  R9_PROGRAM("r9-program", "riddle9", "search-program"),
  R9_CONTROLLER("r9-controller", "riddle9", "search-controller"),
  R9_SCAN("r9-scan", "riddle9", "search-scan"),
  R10_SORT("r10-sort", "riddle10", "central-sort"),
  R10_COUNT("r10-count", "riddle10", "central-count"),
  R10_SEARCH("r10-search", "riddle10", "central-search"),
  R10_FINAL("r10-final", "riddle10", "completed"),
  SYSTEM_ACCESS("system-access", "riddle10", "completed"),
  ESCAPE_COMPLETE("escape-complete", "riddle10", "completed");

  private final String hintKey;
  private final String riddleKey;
  private final String storyKey;

  SystemRecoveryProgressPlace(String hintKey, String riddleKey, String storyKey) {
    this.hintKey = hintKey;
    this.riddleKey = riddleKey;
    this.storyKey = storyKey;
  }

  /** @return stable key used by the hint catalog and debug tooling */
  public String hintKey() {
    return hintKey;
  }

  /** @return quest-log tab key owning this place */
  public String riddleKey() {
    return riddleKey;
  }

  /** @return story key used for the concrete third hint, or {@code null} */
  public String storyKey() {
    return storyKey;
  }
}
