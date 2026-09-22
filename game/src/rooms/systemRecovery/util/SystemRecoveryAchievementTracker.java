package rooms.systemRecovery.util;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.util.interpreter.TerminalStep;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/**
 * Pure run-local achievement rules for System Recovery.
 *
 * <p>The tracker knows only about authoritative gameplay facts and emits stable achievement IDs.
 * Persistence, popups and recipient selection remain in {@link SystemRecoveryAchievements}.
 */
public final class SystemRecoveryAchievementTracker {

  private static final int WRONG_ATTEMPTS_FOR_FIRST_MILESTONE = 5;
  private static final int WRONG_ATTEMPTS_FOR_SYNTAX_MILESTONE = 10;

  private final Consumer<String> unlock;
  private final Set<SystemRecoveryPuzzle> hintedPuzzles = EnumSet.noneOf(SystemRecoveryPuzzle.class);
  private final Set<SystemRecoveryPuzzle> solvedPuzzles = EnumSet.noneOf(SystemRecoveryPuzzle.class);
  private final Set<SystemRecoveryPuzzle> failedPuzzles = EnumSet.noneOf(SystemRecoveryPuzzle.class);
  private final Set<SystemRecoveryPuzzle> failedTerminalPuzzles =
      EnumSet.noneOf(SystemRecoveryPuzzle.class);
  private final Set<String> failedUploads = new HashSet<>();
  private final Set<String> acceptedUploads = new HashSet<>();
  private final Set<String> emittedAchievements = new HashSet<>();
  private boolean debugRun;
  private boolean firstTerminalAttemptSeen;
  private int wrongTerminalAttempts;
  private int acceptedHints;

  /**
   * Creates a tracker that sends each achievement ID to the supplied sink at most once per run.
   *
   * @param unlock callback receiving newly earned achievement IDs
   */
  public SystemRecoveryAchievementTracker(Consumer<String> unlock) {
    this.unlock = unlock == null ? ignored -> {} : unlock;
  }

  /**
   * Starts a fresh run-local evaluation while leaving persisted unlocks untouched.
   *
   * @param debugRun whether the current room was started with debug controls enabled; debug mode
   *     does not suppress achievements, but it prevents the no-debug completion achievement
   */
  public void reset(boolean debugRun) {
    this.debugRun = debugRun;
    hintedPuzzles.clear();
    solvedPuzzles.clear();
    failedPuzzles.clear();
    failedTerminalPuzzles.clear();
    failedUploads.clear();
    acceptedUploads.clear();
    emittedAchievements.clear();
    firstTerminalAttemptSeen = false;
    wrongTerminalAttempts = 0;
    acceptedHints = 0;
  }

  /**
   * Captures the run-local facts used by conditional achievements.
   *
   * <p>This snapshot contains no persistent unlock status. Persistent unlocks remain owned by the
   * achievement store; the snapshot only makes continuing a checkpoint behave like the original
   * run.
   *
   * @return immutable run-local achievement state
   */
  public Snapshot snapshot() {
    return new Snapshot(
        debugRun,
        firstTerminalAttemptSeen,
        wrongTerminalAttempts,
        acceptedHints,
        hintedPuzzles.stream().map(SystemRecoveryPuzzle::id).toList(),
        solvedPuzzles.stream().map(SystemRecoveryPuzzle::id).toList(),
        failedPuzzles.stream().map(SystemRecoveryPuzzle::id).toList(),
        failedTerminalPuzzles.stream().map(SystemRecoveryPuzzle::id).toList(),
        List.copyOf(failedUploads),
        List.copyOf(acceptedUploads),
        List.copyOf(emittedAchievements));
  }

  /**
   * Restores run-local achievement facts without evaluating rules or emitting unlock callbacks.
   *
   * @param snapshot saved run-local state
   */
  public void restore(Snapshot snapshot) {
    if (snapshot == null) return;
    reset(snapshot.debugRun());
    firstTerminalAttemptSeen = snapshot.firstTerminalAttemptSeen();
    wrongTerminalAttempts = snapshot.wrongTerminalAttempts();
    acceptedHints = snapshot.acceptedHints();
    snapshot.hintedPuzzles().stream().map(SystemRecoveryAchievementTracker::puzzle).forEach(hintedPuzzles::add);
    snapshot.solvedPuzzles().stream().map(SystemRecoveryAchievementTracker::puzzle).forEach(solvedPuzzles::add);
    snapshot.failedPuzzles().stream().map(SystemRecoveryAchievementTracker::puzzle).forEach(failedPuzzles::add);
    snapshot.failedTerminalPuzzles().stream()
        .map(SystemRecoveryAchievementTracker::puzzle)
        .forEach(failedTerminalPuzzles::add);
    failedUploads.addAll(snapshot.failedUploads());
    acceptedUploads.addAll(snapshot.acceptedUploads());
    emittedAchievements.addAll(snapshot.emittedAchievements());
  }

  private static SystemRecoveryPuzzle puzzle(String id) {
    return java.util.Arrays.stream(SystemRecoveryPuzzle.values())
        .filter(puzzle -> puzzle.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown System Recovery puzzle: " + id));
  }

  /**
   * Serialized state needed to continue conditional achievements across a checkpoint load.
   *
   * @param debugRun whether the run was started with debug controls
   * @param firstTerminalAttemptSeen whether any terminal attempt has occurred
   * @param wrongTerminalAttempts number of rejected terminal submissions
   * @param acceptedHints number of accepted telephone hints
   * @param hintedPuzzles puzzles for which at least one hint was accepted
   * @param solvedPuzzles puzzles completed during the run
   * @param failedPuzzles puzzles with an unsuccessful physical or terminal attempt
   * @param failedTerminalPuzzles puzzles with an unsuccessful terminal attempt
   * @param failedUploads chip types that received an unsuccessful upload
   * @param acceptedUploads chip types that received a successful upload
   * @param emittedAchievements achievement IDs already emitted during this run
   */
  public record Snapshot(
      boolean debugRun,
      boolean firstTerminalAttemptSeen,
      int wrongTerminalAttempts,
      int acceptedHints,
      List<String> hintedPuzzles,
      List<String> solvedPuzzles,
      List<String> failedPuzzles,
      List<String> failedTerminalPuzzles,
      List<String> failedUploads,
      List<String> acceptedUploads,
      List<String> emittedAchievements) {

    /**
     * Creates a defensive, validated snapshot value.
     *
     * @param debugRun whether the run was started with debug controls
     * @param firstTerminalAttemptSeen whether any terminal attempt has occurred
     * @param wrongTerminalAttempts number of rejected terminal submissions
     * @param acceptedHints number of accepted telephone hints
     * @param hintedPuzzles puzzles for which at least one hint was accepted
     * @param solvedPuzzles puzzles completed during the run
     * @param failedPuzzles puzzles with an unsuccessful physical or terminal attempt
     * @param failedTerminalPuzzles puzzles with an unsuccessful terminal attempt
     * @param failedUploads chip types that received an unsuccessful upload
     * @param acceptedUploads chip types that received a successful upload
     * @param emittedAchievements achievement IDs already emitted during this run
     */
    public Snapshot {
      if (wrongTerminalAttempts < 0 || acceptedHints < 0) {
        throw new IllegalArgumentException("Achievement counters must not be negative.");
      }
      hintedPuzzles = normalizeIds(hintedPuzzles);
      solvedPuzzles = normalizeIds(solvedPuzzles);
      failedPuzzles = normalizeIds(failedPuzzles);
      failedTerminalPuzzles = normalizeIds(failedTerminalPuzzles);
      failedUploads = normalizeStrings(failedUploads);
      acceptedUploads = normalizeStrings(acceptedUploads);
      emittedAchievements = normalizeStrings(emittedAchievements);
    }

    /** @return an empty state for legacy savegames without achievement data */
    public static Snapshot empty() {
      return new Snapshot(false, false, 0, 0, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static List<String> normalizeIds(List<String> ids) {
      return java.util.stream.Stream.ofNullable(ids)
          .flatMap(List::stream)
          .map(String::valueOf)
          .peek(Snapshot::validatePuzzleId)
          .distinct()
          .sorted()
          .toList();
    }

    private static void validatePuzzleId(String id) {
      puzzle(id);
    }

    private static List<String> normalizeStrings(List<String> values) {
      return java.util.stream.Stream.ofNullable(values)
          .flatMap(List::stream)
          .map(String::valueOf)
          .filter(value -> !value.isBlank())
          .distinct()
          .sorted()
          .toList();
    }
  }

  /**
   * Records one authoritative terminal attempt.
   *
   * @param attempt submitted terminal source and state
   * @param correct whether the authoritative interpreter accepted the source
   */
  public void terminalAttempt(TerminalAttempt attempt, boolean correct) {
    if (attempt == null) return;
    SystemRecoveryPuzzle puzzle = SystemRecoveryPuzzle.fromTerminalState(attempt.state());
    if (!firstTerminalAttemptSeen) {
      firstTerminalAttemptSeen = true;
      if (correct && attempt.state() == TerminalStep.ENERGY_ARRAY.stateId()) {
        emit(SystemRecoveryAchievements.DIRECT_FIRST_TRY);
      }
    }
    if (!correct) {
      wrongTerminalAttempts++;
      failedPuzzles.add(puzzle);
      failedTerminalPuzzles.add(puzzle);
      if (wrongTerminalAttempts >= WRONG_ATTEMPTS_FOR_FIRST_MILESTONE) {
        emit(SystemRecoveryAchievements.INTENTIONAL_FAILURE);
      }
      if (wrongTerminalAttempts >= WRONG_ATTEMPTS_FOR_SYNTAX_MILESTONE) {
        emit(SystemRecoveryAchievements.SYNTAX_PERSONALITY);
      }
    }
  }

  /**
   * Records one physical or item-related interaction attempt.
   *
   * @param puzzle puzzle receiving the interaction
   * @param input interaction payload
   * @param correct whether the authoritative interaction succeeded
   * @param playerId acting player ID
   */
  public void physicalAttempt(
      SystemRecoveryPuzzle puzzle, String input, boolean correct, int playerId) {
    if (puzzle == null || correct) return;
    failedPuzzles.add(puzzle);
  }

  /**
   * Records one accepted shared telephone hint.
   *
   * @param puzzle puzzle whose hint was accepted
   * @param hintId stable hint identifier
   */
  public void hintUsed(SystemRecoveryPuzzle puzzle, String hintId) {
    if (puzzle == null) return;
    hintedPuzzles.add(puzzle);
    acceptedHints++;
    if (acceptedHints >= 3) emit(SystemRecoveryAchievements.ECHO_STILL_THERE);
    if (hintId != null && hintId.endsWith(":solution")) {
      emit(SystemRecoveryAchievements.JUST_ASKING);
    }
  }

  /**
   * Records the first completion of a concrete riddle.
   *
   * @param puzzle completed puzzle
   */
  public void puzzleSolved(SystemRecoveryPuzzle puzzle) {
    if (puzzle == null || !solvedPuzzles.add(puzzle)) return;
    if (!hintedPuzzles.contains(puzzle)) {
      emit(SystemRecoveryAchievements.NO_FINISHED_SOLUTION);
    }
    if (failedPuzzles.contains(puzzle)) {
      emit(SystemRecoveryAchievements.NOT_A_BUG);
    }
    if (puzzle == SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE
        && !failedTerminalPuzzles.contains(puzzle)) {
      emit(SystemRecoveryAchievements.NO_LOST_INDEX);
    }

    switch (puzzle) {
      case ENERGY -> emit(SystemRecoveryAchievements.INITIALIZATION_COMPLETE);
      case MODULE_STORAGE -> emit(SystemRecoveryAchievements.MODULAR_THINKING);
      case INVENTORY_SCANNER -> emit(SystemRecoveryAchievements.COUNT_WHAT_EXISTS);
      case TRANSPORT_STORAGE -> emit(SystemRecoveryAchievements.EVERY_PACKAGE_COUNTS);
      case BUBBLE_SORT -> emit(SystemRecoveryAchievements.SORTED_IS_SORTED);
      case TWO_DIMENSIONAL_STORAGE -> emit(SystemRecoveryAchievements.TWO_DIMENSIONAL_THINKING);
      case SEARCH_ROBOT -> {
        emit(SystemRecoveryAchievements.SEARCH_COMPLETE);
        emit(SystemRecoveryAchievements.EVERY_CELL_COUNTS);
      }
      case SYSTEM_CORE -> {
        emit(SystemRecoveryAchievements.THREE_RESULTS_ONE_CORE);
        emit(SystemRecoveryAchievements.FREE_VARIABLE);
        if (!debugRun) emit(SystemRecoveryAchievements.DEBUG_IS_NOT_GAMEPLAY);
      }
      default -> {}
    }

    if (solvedPuzzles.contains(SystemRecoveryPuzzle.INVENTORY_SCANNER)
        && solvedPuzzles.contains(SystemRecoveryPuzzle.TRANSPORT_STORAGE)
        && !failedTerminalPuzzles.contains(SystemRecoveryPuzzle.INVENTORY_SCANNER)
        && !failedTerminalPuzzles.contains(SystemRecoveryPuzzle.TRANSPORT_STORAGE)) {
      emit(SystemRecoveryAchievements.LOOP_UNDERSTANDING);
    }
  }

  /**
   * Records one successful or failed chip-program upload.
   *
   * @param chipType stable chip type
   * @param correct whether the program was accepted and written
   */
  public void chipUploadAttempt(String chipType, boolean correct) {
    if (chipType == null || chipType.isBlank()) return;
    if (correct) acceptedUploads.add(chipType);
    else failedUploads.add(chipType);
    if (correct) {
      emit(SystemRecoveryAchievements.NOT_FOR_CONSUMPTION);
      if (acceptedUploads.contains("sort")
          && acceptedUploads.contains("search")
          && failedUploads.isEmpty()) {
        emit(SystemRecoveryAchievements.CLEAN_UPLOAD);
      }
    }
  }

  /**
   * Records a completed Petri-net learning step for progress-specific achievements.
   *
   * @param step completed learning step
   */
  public void learningStepCompleted(SystemRecoveryLearningStep step) {
    if (step == null) return;
    switch (step) {
      case ARCHIVE_ACCESS -> emit(SystemRecoveryAchievements.ARCHIVE_ACCESS);
      case SEARCH_PROGRAM -> emit(SystemRecoveryAchievements.LOCATOR_ESTABLISHED);
      case SYSTEM_CORE_ACCESS -> emit(SystemRecoveryAchievements.AXIOM_RELAX);
      case CORE_SEARCH_ROBOT -> emit(SystemRecoveryAchievements.EVERY_CELL_COUNTS);
      default -> {}
    }
  }

  private void emit(String achievementId) {
    if (emittedAchievements.add(achievementId)) unlock.accept(achievementId);
  }
}
