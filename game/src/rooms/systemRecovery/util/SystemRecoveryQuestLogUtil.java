package rooms.systemRecovery.util;

import engine.Game;
import engine.language.Translation;
import feature.questlog.QuestLogUtil;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Questlog setup and entries for the System Recovery escape room. */
public final class SystemRecoveryQuestLogUtil {

  private static final Translation QUESTLOG_ENTRIES = new Translation("questlog");
  private static final Set<String> ADDED_ENTRIES = ConcurrentHashMap.newKeySet();

  private SystemRecoveryQuestLogUtil() {}

  /** Creates the authoritative questlog for the current System Recovery level. */
  public static void initializeQuestLog() {
    ADDED_ENTRIES.clear();
    Game.add(QuestLogUtil.initServerQuestLog());
  }

  /** Adds one dialog instruction to the tab belonging to its riddle. */
  public static void addDialogEntry(String riddleKey, String entryKey) {
    if (riddleKey == null || entryKey == null) {
      return;
    }

    String uniqueKey = riddleKey + "." + entryKey;
    if (!ADDED_ENTRIES.add(uniqueKey)) return;

    boolean added =
        QuestLogUtil.add(
            QUESTLOG_ENTRIES.text(riddleKey + ".tab"),
            QUESTLOG_ENTRIES.text(riddleKey + ".entries." + entryKey));
    if (!added) {
      ADDED_ENTRIES.remove(uniqueKey);
    }
  }
}
