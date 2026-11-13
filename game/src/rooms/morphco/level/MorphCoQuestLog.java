package rooms.morphco.level;

import engine.Game;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.util.MorphCoText;

/**
 * The shared quest log, derived from the room state.
 *
 * <p>Every entry has a stable ID. When a task is finished, its entry is replaced by the finished
 * text and keeps its original timestamp, so the log reads like the notebook of the script: open
 * tasks first, "(erledigt)" later. Because the log is a function of the state, a loaded savegame
 * rebuilds exactly the same entries.
 */
final class MorphCoQuestLog {

  private static final String MAIN = "main";
  private static final String F04 = "f04";
  private static final String A12 = "a12";
  private static final String CLUES = "clues";

  private final Map<String, QuestLogEntry> entries = new HashMap<>();

  /** Creates the server quest log entity. */
  void initialize() {
    entries.clear();
    Game.add(QuestLogUtil.initServerQuestLog());
  }

  /**
   * Adds or updates all entries that follow from the state.
   *
   * @param state current room state
   */
  void sync(MorphCoState state) {
    boolean alarmOff = state.done(MorphCoMilestone.ALARM_OFF);
    boolean causes = state.done(MorphCoMilestone.CAUSES_FOUND);
    boolean a12 = state.done(MorphCoMilestone.A12_REPAIRED);
    boolean f04 = state.done(MorphCoMilestone.F04_REPAIRED);
    boolean handbook = state.done(MorphCoMilestone.HANDBOOK_READ);

    if (state.has(MorphCoState.ARRIVAL) || alarmOff) {
      put(MAIN, "alarm", alarmOff ? "alarm-done" : "alarm");
    }
    if (alarmOff) put(MAIN, "causes", causes ? "causes-done" : "causes");
    if (causes) {
      put(MAIN, "a12", a12 ? "a12-done" : "repair-a12");
      put(MAIN, "f04", f04 ? "f04-done" : "repair-f04");
      // The goal is visible from the start, as in the script; it becomes concrete once both run.
      String restart = a12 && f04 ? "restart" : "restart-open";
      if (state.done(MorphCoMilestone.PRODUCTION_RESTARTED)) restart = "restart-done";
      put(MAIN, "restart", restart);
    }

    if (handbook) {
      put(F04, "handbook", "handbook");
      boolean checked = state.has(MorphCoState.MACHINE_INSPECTED) || f04;
      put(F04, "inspect-machine", checked ? "inspect-machine-done" : "inspect-machine");
    }
    if (state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) put(F04, "word-classes", "word-classes");
    if (state.has(MorphCoState.MACHINE_INSPECTED)) {
      boolean inspected = state.has(MorphCoState.HATCH_OPENED) || a12;
      put(F04, "inspect-a12", inspected ? "inspect-a12-done" : "inspect-a12");
    }
    if (f04) put(F04, "repaired", "repaired");

    if (state.has(MorphCoState.HATCH_OPENED) || state.done(MorphCoMilestone.AFFIXES_SORTED)) {
      put(A12, "inspect", "inspect-done");
      put(A12, "sort", state.done(MorphCoMilestone.AFFIXES_SORTED) ? "sort-done" : "sort");
    }
    if (state.has(MorphCoState.SHAFTS_FILLED) || a12) {
      put(A12, "restart", a12 ? "restart-done" : "restart");
    }
    if (a12) {
      put(A12, "repaired", "repaired");
      put(A12, "go-f04", f04 ? "go-f04-done" : "go-f04");
    }

    if (state.has(MorphCoState.CONVEYOR_SEEN)) put(CLUES, "faulty-forms", "faulty-forms");
    if (state.has(MorphCoState.COMPLAINT_SEEN)) put(CLUES, "complaint", "complaint");
    if (handbook) putCollapsible("handbook");
    if (state.has(MorphCoState.MORPH1_READ)) putCollapsible("morph1");
    if (state.has(MorphCoState.MORPH2_READ)) putCollapsible("morph2");
    if (state.has(MorphCoState.A12_DISPLAY_SEEN)) put(CLUES, "a12-display", "a12-display");
    if (state.has(MorphCoState.LOCK_SEEN)) put(CLUES, "lock-question", "lock-question");
    if (state.has(MorphCoState.PILE_TAKEN)) put(CLUES, "fragments", "fragments");
  }

  private void put(String tab, String id, String entry) {
    String text = MorphCoText.questKey(tab + ".entries." + entry);
    replace(tab, id, text, Optional.empty());
  }

  private void putCollapsible(String entry) {
    String title = MorphCoText.questKey(CLUES + ".entries." + entry + "-title");
    String text = MorphCoText.questKey(CLUES + ".entries." + entry);
    replace(CLUES, entry, text, Optional.of(title));
  }

  private void replace(String tab, String id, String text, Optional<String> title) {
    String tabKey = MorphCoText.questKey(tab + ".tab");
    String entryId = tab + "/" + id;
    QuestLogEntry existing = entries.get(entryId);
    if (existing != null && existing.text().equals(text)) return;
    int timestamp = Game.currentTick();
    if (existing != null) {
      QuestLogEntry old = existing;
      QuestLogUtil.getQuestLogComponent().ifPresent(log -> log.remove(tabKey, old));
      timestamp = existing.timestamp();
    }
    QuestLogEntry entry =
        new QuestLogEntry(text, timestamp, false, QuestLogEntry.DEFAULT_OWNER, false, title);
    if (QuestLogUtil.add(tabKey, entry)) entries.put(entryId, entry);
  }
}
