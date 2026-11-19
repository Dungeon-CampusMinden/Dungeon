package rooms.morphco.level;

import engine.Game;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.util.MorphCoText;

/**
 * The shared quest log, derived from the room state.
 *
 * <p>Every entry has a stable ID. When a task changes, its entry is replaced and keeps its original
 * timestamp. Tasks carry a box that is ticked once they are done; the tabs of F-04 and A-12 hold
 * one checklist each, with the steps of the repair indented below it; the side quest at the snack
 * machine gets a tab of the same kind once it has started. Because the log is a function of the
 * state, a loaded savegame rebuilds exactly the same entries.
 */
final class MorphCoQuestLog {

  private static final String MAIN = "main";
  private static final String F04 = "f04";
  private static final String A12 = "a12";
  private static final String CLUES = "clues";
  private static final String HINTS = "hints";
  private static final String SNACK = "snack";
  private static final String OPEN = "[img=rooms/morphCo/art/task_open.png] ";
  private static final String DONE = "[img=rooms/morphCo/art/task_done.png] ";
  private static final String INDENT = "      ";

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
    boolean inspected = state.has(MorphCoState.MACHINE_INSPECTED);
    boolean hatch = state.has(MorphCoState.HATCH_OPENED);
    boolean sorted = state.done(MorphCoMilestone.AFFIXES_SORTED);

    boolean card = state.cardTaken();
    if (state.has(MorphCoState.ARRIVAL) || card) {
      put(MAIN, "card", task(0, card, MAIN, card ? "card-done" : "card"));
    }
    if (state.has(MorphCoState.ARRIVAL) || alarmOff) {
      put(MAIN, "alarm", task(0, alarmOff, MAIN, alarmOff ? "alarm-done" : "alarm"));
    }
    if (alarmOff) put(MAIN, "causes", task(0, causes, MAIN, causes ? "causes-done" : "causes"));
    if (causes) {
      boolean wired = state.has(MorphCoState.FUSE_BOX_WIRED);
      put(MAIN, "fuse-box", task(0, wired, MAIN, wired ? "fuse-box-done" : "fuse-box"));
      put(MAIN, "a12", task(0, a12, MAIN, "repair-a12"));
      put(MAIN, "f04", task(0, f04, MAIN, "repair-f04"));
      // The goal is visible from the start, as in the script; it becomes concrete once both run.
      boolean restarted = state.done(MorphCoMilestone.PRODUCTION_RESTARTED);
      String restart = a12 && f04 ? "restart" : "restart-open";
      if (restarted) restart = "restart-done";
      put(MAIN, "restart", task(0, restarted, MAIN, restart));
    }

    if (handbook) {
      List<String> steps = new ArrayList<>();
      steps.add(task(0, f04, MAIN, "repair-f04"));
      steps.add(task(1, true, F04, "handbook"));
      steps.add(task(1, inspected || f04, F04, "inspect-machine"));
      if (state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) {
        steps.add(task(2, true, F04, "word-classes"));
      }
      if (inspected) steps.add(task(1, hatch || a12, F04, "inspect-a12"));
      put(F04, "tasks", String.join("\n", steps));
    }
    if (hatch || sorted) {
      List<String> steps = new ArrayList<>();
      steps.add(task(0, a12, MAIN, "repair-a12"));
      steps.add(task(1, true, A12, "inspect"));
      steps.add(task(1, sorted, A12, "sort"));
      if (state.has(MorphCoState.PILE_TAKEN)
          || state.has(MorphCoState.SHAFTS_FILLED)
          || !state.assembled().isEmpty()) {
        steps.add(task(1, state.allAssembled(), A12, "circumfixes"));
      }
      if (state.has(MorphCoState.SHAFTS_FILLED) || a12) steps.add(task(1, a12, A12, "restart"));
      if (a12) steps.add(task(1, f04, A12, "go-f04"));
      put(A12, "tasks", String.join("\n", steps));
    }

    // The side quest stays hidden until the snack machine has been used.
    if (state.has(MorphCoState.SNACK_SEEN)) put(SNACK, "tasks", snackTasks(state));

    if (state.has(MorphCoState.CONVEYOR_SEEN)) put(CLUES, "faulty-forms", clue("faulty-forms"));
    if (state.has(MorphCoState.COMPLAINT_SEEN)) put(CLUES, "complaint", clue("complaint"));
    if (handbook) putCollapsible("handbook");
    if (state.has(MorphCoState.MORPH1_READ)) put(CLUES, "morph1", clue("morph1"));
    if (state.has(MorphCoState.MORPH2_READ)) put(CLUES, "morph2", clue("morph2"));
    if (MorphCoBooks.anyHandout(state)) put(CLUES, "handout", clue("handout"));
    if (state.has(MorphCoState.A12_DISPLAY_SEEN)) put(CLUES, "a12-display", clue("a12-display"));
    if (state.has(MorphCoState.LOCK_SEEN)) put(CLUES, "lock-question", clue("lock-question"));
    if (state.has(MorphCoState.PILE_TAKEN)) put(CLUES, "fragments", clue("fragments"));

    for (ShiftLead.Step step : ShiftLead.Step.values()) {
      for (HintHotline.Stage stage : HintHotline.Stage.values()) {
        HintHotline.Offer hint = new HintHotline.Offer(step, stage);
        if (!state.has(hint.flag())) continue;
        String text =
            MorphCoText.questKey(HINTS + ".entries." + stage.key(), MorphCoText.key(hint.text()));
        replace(HINTS, hint.id(), text, Optional.empty());
      }
    }
  }

  private static String snackTasks(MorphCoState state) {
    boolean stuck = state.has(MorphCoState.SNACK_STUCK);
    List<String> steps = new ArrayList<>();
    steps.add(task(0, stuck, SNACK, stuck ? "bar-done" : "bar"));
    steps.add(task(1, state.has(MorphCoState.SNACK_ASKED), SNACK, "ask"));
    if (state.has(MorphCoState.SNACK_ASKED)) {
      steps.add(task(1, state.has(MorphCoState.STORAGE_OPEN), SNACK, "storage"));
    }
    if (state.has(MorphCoState.STORAGE_OPEN)) {
      steps.add(task(1, state.has(MorphCoState.LOCKER_KEY), SNACK, "key"));
    }
    if (state.has(MorphCoState.LOCKER_KEY)) {
      steps.add(task(1, state.has(MorphCoState.KITCHEN_OPEN), SNACK, "return-key"));
    }
    if (state.has(MorphCoState.KITCHEN_OPEN)) {
      steps.add(task(1, state.has(MorphCoState.BANKNOTE), SNACK, "banknote"));
    }
    if (state.has(MorphCoState.BANKNOTE)) steps.add(task(1, stuck, SNACK, "pay"));
    return String.join("\n", steps);
  }

  private void put(String tab, String id, String text) {
    replace(tab, id, text, Optional.empty());
  }

  private static String clue(String entry) {
    return MorphCoText.questKey(CLUES + ".entries." + entry);
  }

  // One line of a checklist: indentation for subtasks, a box that is ticked once the task is done.
  private static String task(int depth, boolean done, String tab, String entry) {
    return INDENT.repeat(depth)
        + (done ? DONE : OPEN)
        + MorphCoText.questKey(tab + ".entries." + entry);
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
