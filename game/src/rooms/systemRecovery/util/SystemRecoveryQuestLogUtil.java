package rooms.systemRecovery.util;

import engine.Game;
import feature.hints.Hint;
import feature.questlog.QuestLogUtil;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
   * Adds one dialog instruction to the tab belonging to its riddle.
   *
   * @param riddleKey stable riddle translation key
   * @param entryKey stable dialog-entry translation key
   */
  public static void addDialogEntry(String riddleKey, String entryKey) {
    if (riddleKey == null || entryKey == null) {
      return;
    }

    String uniqueKey = riddleKey + "." + entryKey;
    if (!ADDED_ENTRIES.add(uniqueKey)) return;

    boolean added =
        QuestLogUtil.add(
            SystemRecoveryText.questKey(riddleKey + ".tab"),
            SystemRecoveryText.questKey(riddleKey + ".entries." + entryKey));
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

    boolean added =
        QuestLogUtil.add(
            SystemRecoveryText.questKey(riddleKey + ".tab"),
            SystemRecoveryText.questKey("hint-prefix") + "\n" + hint.title() + "\n" + hint.text());
    if (!added) ADDED_ENTRIES.remove(uniqueKey);
  }
}
