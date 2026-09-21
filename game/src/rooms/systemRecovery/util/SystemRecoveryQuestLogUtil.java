package rooms.systemRecovery.util;

import engine.Game;
import feature.hints.Hint;
import feature.questlog.QuestLogUtil;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Questlog setup and entries for the System Recovery escape room. */
public final class SystemRecoveryQuestLogUtil {

  private static final Set<String> ADDED_ENTRIES = ConcurrentHashMap.newKeySet();

  private SystemRecoveryQuestLogUtil() {}

  /** Creates the authoritative questlog for the current System Recovery level. */
  public static void initializeQuestLog() {
    ADDED_ENTRIES.clear();
    Game.add(QuestLogUtil.initServerQuestLog());
  }

  /**
   * Adds one complete story dialog to the tab belonging to its riddle.
   *
   * <p>The speaker and dialog body remain separate transport keys, so every client can render the
   * same entry in its own language.
   *
   * @param riddleKey stable riddle translation key
   * @param dialogKey stable dialog identifier used for de-duplication
   * @param speakerKey key below {@code story}, for example {@code speaker} or {@code axiom}
   * @param messageKey key below {@code story} containing the complete dialog body
   */
  public static void addDialogEntry(
      String riddleKey, String dialogKey, String speakerKey, String messageKey) {
    if (riddleKey == null || dialogKey == null || speakerKey == null || messageKey == null) {
      return;
    }

    String uniqueKey = riddleKey + "." + dialogKey;
    if (!ADDED_ENTRIES.add(uniqueKey)) return;

    boolean added =
        QuestLogUtil.add(
            SystemRecoveryText.questKey(riddleKey + ".tab"),
            SystemRecoveryText.key("story." + speakerKey)
                + "\n"
                + SystemRecoveryText.key("story." + messageKey));
    if (!added) {
      ADDED_ENTRIES.remove(uniqueKey);
    }
  }

  /**
   * Adds one accepted telephone hint to the shared tab for its current riddle.
   *
   * @param riddleKey stable riddle translation key
   * @param hint accepted telephone hint
   */
  public static void addHintEntry(String riddleKey, Hint hint) {
    if (riddleKey == null || hint == null) return;

    String uniqueKey = riddleKey + ".hint." + hint.title() + "." + hint.text();
    if (!ADDED_ENTRIES.add(uniqueKey)) return;

    boolean added = QuestLogUtil.add(SystemRecoveryText.questKey(riddleKey + ".tab"), hint.text());
    if (!added) ADDED_ENTRIES.remove(uniqueKey);
  }

  /**
   * Adds the exact source accepted for a terminal-backed learning step to its riddle tab.
   *
   * @param attempt authoritative accepted terminal attempt
   */
  public static void addTerminalSolutionEntry(TerminalAttempt attempt) {
    if (attempt == null) return;
    TerminalStep.fromStateId(attempt.state())
        .flatMap(SystemRecoveryLearningStep::fromTerminalStep)
        .ifPresent(step -> addSolutionEntry(step, attempt.source()));
  }

  /**
   * Adds an accepted chip program or final result payload to the matching riddle tab.
   *
   * @param step completed learning step
   * @param source exact source or payload submitted by the player
   */
  public static void addSolutionEntry(SystemRecoveryLearningStep step, String source) {
    if (step == null || step.riddleKey() == null || source == null || source.isBlank()) return;

    String uniqueKey = step.hintKey() + ".solution." + source;
    if (!ADDED_ENTRIES.add(uniqueKey)) return;

    boolean added =
        QuestLogUtil.add(
            SystemRecoveryText.questKey(step.riddleKey() + ".tab"),
            SystemRecoveryText.questKey("solution", source));
    if (!added) ADDED_ENTRIES.remove(uniqueKey);
  }
}
