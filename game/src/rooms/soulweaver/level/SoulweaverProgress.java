package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.tracking.AttemptDetails;
import engine.tracking.Tracking;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import rooms.soulweaver.modules.loops.LoopPuzzle;
import rooms.soulweaver.modules.loops.LoopRune;
import rooms.soulweaver.state.SoulweaverPhase;

/**
 * Authoritative journal and tracking for the room's discoveries and learning progress. Each act has
 * its own journal tab with a compact history of outcomes, released tips and completions.
 */
public final class SoulweaverProgress {
  private static final Map<String, QuestLogEntry> RECORDED = new LinkedHashMap<>();
  private static final Set<String> COMPLETED = new HashSet<>();
  private static final Set<String> STARTED = new HashSet<>();
  private static final Map<String, QuestLogEntry> HISTORY = new HashMap<>();
  private static Optional<QuestLogEntry> taskOverview = Optional.empty();
  private static String currentObjective = "";

  private SoulweaverProgress() {}

  /** Starts a fresh room journal before spawning puzzle runtimes. */
  static void initialize() {
    RECORDED.clear();
    COMPLETED.clear();
    STARTED.clear();
    HISTORY.clear();
    taskOverview = Optional.empty();
    currentObjective = "";
    Game.add(QuestLogUtil.initServerQuestLog());
    updateTasks("Erkunde Valerius' Werkstatt und finde heraus, wie du Nox wieder erwecken kannst.");
  }

  /**
   * Marks the puzzles of earlier acts as completed without reporting them again. The saved act's
   * own entry sets the current objective.
   *
   * @param phase Act II, III or IV
   */
  static void restore(SoulweaverPhase phase) {
    COMPLETED.addAll(List.of("vessels", "essences"));
    if (phase != SoulweaverPhase.LOOPS) {
      for (int i = 0; i < LoopPuzzle.challenges().size(); i++) COMPLETED.add("cellar-" + i);
    }
    if (phase == SoulweaverPhase.DECISIONS) COMPLETED.add("methods");
    record("continued", "Aufgaben", "Spiel ab Akt " + (phase.ordinal() + 1) + " fortgesetzt.");
  }

  /**
   * Records an available puzzle once, without revealing its solution.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objective current objective shown in the journal
   */
  public static void started(String puzzleId, String objective) {
    if (Game.isMultiplayerClient()) return;
    Tracking.puzzleStarted(puzzleId);
    STARTED.add(puzzleId);
    if (COMPLETED.contains(puzzleId)) return;
    currentObjective = puzzleId;
    updateTasks(objective);
  }

  /**
   * Records the real successful puzzle outcome once.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param summary successful outcome shown in the journal
   */
  public static void solved(String puzzleId, String summary) {
    if (Game.isMultiplayerClient()) return;
    Tracking.puzzleSolved(puzzleId);
    record("solved:" + puzzleId, tab(puzzleId), summary);
    COMPLETED.add(puzzleId);
    if (currentObjective.equals(puzzleId)) {
      currentObjective = "";
      updateTasks(
          switch (puzzleId) {
            case "essences" -> "Aktiviere Nox an der Bindungsfläche.";
            case "cellar-4" -> "Folge Nox zur Methodenwerkstatt.";
            case "methods" -> "Folge Nox durch den Nebenausgang zum Labyrinth der Entscheidungen.";
            case "decisions" ->
                "Lies die Schriftrolle vor dem Herzfeuer und bringe die Opfergabe dar.";
            default -> "Nox bereitet den nächsten Schritt vor.";
          });
    }
  }

  /**
   * Appends one outcome to an always expanded history block in the act's tab. A block keeps the
   * position where it started, so blocks, tips and completions read in the order they began.
   *
   * @param puzzleId stable room-local puzzle identifier; selects the act tab
   * @param block single-line block title, unique within the act
   * @param line appended outcome
   */
  public static void log(String puzzleId, String block, String line) {
    if (Game.isMultiplayerClient()) return;
    String tab = tab(puzzleId);
    String key = tab + ":" + block;
    QuestLogEntry previous = HISTORY.get(key);
    QuestLogEntry entry =
        new QuestLogEntry(
            (previous == null ? "[color=#dbb463]" + block + "[/color]" : previous.text())
                + "\n"
                + line,
            previous == null ? Game.currentTick() : previous.timestamp(),
            false,
            QuestLogEntry.DEFAULT_OWNER,
            false);
    QuestLogUtil.getQuestLogComponent()
        .ifPresent(
            log -> {
              if (previous != null) log.remove(tab, previous);
              log.add(tab, entry);
              HISTORY.put(key, entry);
            });
  }

  private static String tab(String puzzleId) {
    return switch (puzzleId) {
      case "vessels", "essences" -> "Akt I · Seelenbindung";
      case "methods" -> "Akt III · Werkstatt";
      case "decisions" -> "Akt IV · Labyrinth";
      default -> "Akt II · Keller";
    };
  }

  /**
   * Replaces only the room's overview; player notes in the same tab remain untouched.
   *
   * @param current current task description
   */
  private static void updateTasks(String current) {
    QuestLogUtil.getQuestLogComponent()
        .ifPresent(
            log -> {
              taskOverview.ifPresent(entry -> log.remove("Aufgaben", entry));
              QuestLogEntry entry =
                  new QuestLogEntry(
                      "[color=#dbb463]Aktuelle Aufgabe[/color]\n> " + current,
                      taskOverview.map(QuestLogEntry::timestamp).orElse(Game.currentTick()),
                      false,
                      QuestLogEntry.DEFAULT_OWNER,
                      false);
              log.add("Aufgaben", entry);
              taskOverview = Optional.of(entry);
            });
    updateReferences();
  }

  /** Keeps the current task limited to found reference material and its released tips. */
  private static void updateReferences() {
    List<QuestLogEntry> references = new ArrayList<>();
    List<String> discoveries =
        switch (currentObjective) {
          case "vessels", "essences" -> List.of("variables-translation");
          case "cellar-0", "cellar-1", "cellar-2", "cellar-3", "cellar-4" ->
              List.of("archive-instructions");
          case "methods" -> List.of("workshop-experiments");
          default -> List.of();
        };
    for (String discovery : discoveries) {
      QuestLogEntry entry = RECORDED.get("discovery:" + discovery);
      if (entry != null) references.add(entry);
    }
    RECORDED.forEach(
        (key, entry) -> {
          if (!currentObjective.isEmpty() && key.startsWith("hint:" + currentObjective + ":"))
            references.add(entry);
        });
    QuestLogUtil.getQuestLogComponent().ifPresent(log -> log.overview("Aufgaben", references));
  }

  /**
   * Resolves the actor at submission time so delayed outcomes retain their attribution.
   *
   * @param who acting player, or null when no player is available
   * @return anonymous participant identifier, or empty when the actor is unknown
   */
  public static Optional<UUID> participant(Entity who) {
    return who == null ? Optional.empty() : Tracking.participantForEntity(who.id());
  }

  /**
   * Stores each complete submitted answer and its authoritative outcome.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objectId stable interacted-object identifier
   * @param answerKind answer representation
   * @param rawAnswer complete submitted answer
   * @param participantId session-scoped anonymous participant
   * @param details help state and final failure reasons
   */
  public static void attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      UUID participantId,
      AttemptDetails details) {
    if (Game.isMultiplayerClient()) return;
    Tracking.attempt(puzzleId, objectId, answerKind, rawAnswer, participantId, details);
  }

  /**
   * Keeps released help in the journal and attributes it to the requesting participant.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param hintId released hint identifier
   * @param title title of the collapsed hint
   * @param text displayed text
   * @param who acting player, or null when no player is available
   */
  public static void hint(String puzzleId, String hintId, String title, String text, Entity who) {
    if (Game.isMultiplayerClient()) return;
    participant(who).ifPresent(id -> Tracking.hintUsed(puzzleId, hintId, id));
    record(
        "hint:" + puzzleId + ":" + hintId, tab(puzzleId), QuestLogEntry.collapsible(title, text));
  }

  /**
   * Records a meaningful action without adding repetitive journal entries.
   *
   * @param objectId stable interacted-object identifier
   * @param actionId tracked action identifier
   * @param who acting player, or null when no player is available
   */
  public static void interaction(String objectId, String actionId, Entity who) {
    participant(who).ifPresent(id -> interaction(objectId, actionId, id));
  }

  /**
   * Records a meaningful action using an actor captured before asynchronous execution. An object id
   * naming a started, unsolved puzzle also scopes the event to that puzzle, so analysis can find
   * the first contact.
   *
   * @param objectId stable interacted-object identifier
   * @param actionId tracked action identifier
   * @param participantId session-scoped anonymous participant
   */
  public static void interaction(String objectId, String actionId, UUID participantId) {
    if (Game.isMultiplayerClient()) return;
    if (STARTED.contains(objectId) && !COMPLETED.contains(objectId))
      Tracking.interaction(objectId, objectId, actionId, participantId);
    else Tracking.interaction(objectId, actionId, participantId);
  }

  /**
   * Adds found information once; repeated visits remain visible in tracking.
   *
   * @param objectId stable interacted-object identifier
   * @param title display title
   * @param text displayed text
   * @param who acting player, or null when no player is available
   */
  public static void discover(String objectId, String title, String text, Entity who) {
    if (Game.isMultiplayerClient()) return;
    interaction(objectId, "discover", who);
    record("discovery:" + objectId, "Fundstücke", QuestLogEntry.collapsible(title, text));
  }

  /**
   * Gives collected runes distinct titles even while their source is collapsed.
   *
   * @param rune collected loop rune
   * @param who acting player, or null when no player is available
   */
  static void discoverRune(LoopRune rune, Entity who) {
    discover("rune-" + rune.id(), runeTitle(rune), rune.code(), who);
  }

  /**
   * @param rune loop rune
   * @return journal name shared by its discovery and its executions
   */
  static String runeTitle(LoopRune rune) {
    String type = rune.program().type().name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    return "Schleifenrune " + (LoopPuzzle.runes().indexOf(rune) + 1) + " · " + type;
  }

  private static void record(String key, String tab, String text) {
    record(key, tab, new QuestLogEntry(text, false));
  }

  private static void record(String key, String tab, QuestLogEntry entry) {
    if (RECORDED.containsKey(key)) return;
    if (QuestLogUtil.add(tab, entry)) {
      RECORDED.put(key, entry);
      updateReferences();
    }
  }
}
