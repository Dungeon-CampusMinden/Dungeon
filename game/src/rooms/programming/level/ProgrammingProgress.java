package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.tracking.AttemptDetails;
import engine.tracking.Tracking;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopRune;
import tools.jackson.databind.json.JsonMapper;

/** Authoritative journal and tracking for the room's discoveries and learning progress. */
public final class ProgrammingProgress {
  private static final Map<String, QuestLogEntry> RECORDED = new LinkedHashMap<>();
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final Set<String> COMPLETED = new HashSet<>();
  private static Optional<QuestLogEntry> taskOverview = Optional.empty();
  private static String currentObjective = "";

  private ProgrammingProgress() {}

  /** Starts a fresh room journal before spawning puzzle runtimes. */
  static void initialize() {
    RECORDED.clear();
    COMPLETED.clear();
    taskOverview = Optional.empty();
    currentObjective = "";
    Game.add(QuestLogUtil.initServerQuestLog());
    updateTasks("Erkunde Valerius' Werkstatt und finde heraus, wie du Nox wieder erwecken kannst.");
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
    record("solved:" + puzzleId, "Fortschritt", summary);
    COMPLETED.add(puzzleId);
    if (currentObjective.equals(puzzleId)) {
      currentObjective = "";
      updateTasks(
          switch (puzzleId) {
            case "essences" -> "Aktiviere Nox an der Bindungsfläche.";
            case "cellar-4" -> "Folge Nox zur Methodenwerkstatt.";
            case "methods" -> "Alle Rätsel gelöst! Verlasse die Werkstatt durch den Nebenausgang.";
            default -> "Nox bereitet den nächsten Schritt vor.";
          });
    }
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
    Tracking.attempt(
        puzzleId,
        objectId,
        answerKind,
        rawAnswer,
        details.failureReasons().isEmpty(),
        participantId,
        details);
  }

  /**
   * Keeps released help in the journal and attributes it to the requesting participant.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param hintId released hint identifier
   * @param text displayed text
   * @param who acting player, or null when no player is available
   */
  public static void hint(String puzzleId, String hintId, String text, Entity who) {
    if (Game.isMultiplayerClient()) return;
    participant(who).ifPresent(id -> Tracking.hintUsed(puzzleId, hintId, id));
    record("hint:" + puzzleId + ":" + hintId, "Hilfe", text);
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
   * Records a meaningful action using an actor captured before asynchronous execution.
   *
   * @param objectId stable interacted-object identifier
   * @param actionId tracked action identifier
   * @param participantId session-scoped anonymous participant
   */
  public static void interaction(String objectId, String actionId, UUID participantId) {
    if (!Game.isMultiplayerClient()) Tracking.interaction(objectId, actionId, participantId);
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
    record("discovery:" + objectId, "Fundstücke", title + "\n\n" + text);
  }

  /**
   * Gives collected runes distinct titles even while their source is collapsed.
   *
   * @param rune collected loop rune
   * @param who acting player, or null when no player is available
   */
  static void discoverRune(LoopRune rune, Entity who) {
    String type = rune.program().type().name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    discover(
        "rune-" + rune.id(),
        "Schleifenrune " + (LoopPuzzle.runes().indexOf(rune) + 1) + " · " + type,
        rune.code(),
        who);
  }

  private static void record(String key, String tab, String text) {
    if (RECORDED.containsKey(key)) return;
    QuestLogEntry entry = new QuestLogEntry(text, false);
    if (QuestLogUtil.add(tab, entry)) {
      RECORDED.put(key, entry);
      updateReferences();
    }
  }

  /**
   * Serializes only public entries; private notes travel in the requesting player's dialog.
   *
   * @return serialized public journal entries
   */
  public static String publicJournal() {
    List<JournalEntry> entries =
        QuestLogUtil.getQuestLogComponent().stream()
            .flatMap(log -> log.getEntries().entrySet().stream())
            .flatMap(
                tab ->
                    tab.getValue().stream()
                        .filter(entry -> !entry.onlyForCreator())
                        .map(
                            entry ->
                                new JournalEntry(
                                    tab.getKey(),
                                    entry.text(),
                                    entry.timestamp(),
                                    entry.userCreated(),
                                    entry.owner())))
            .toList();
    return JSON.writeValueAsString(entries);
  }

  /**
   * Restores the synchronized public journal even before its carrier entity has spawned.
   *
   * @param serialized public journal JSON received from the host
   */
  public static void receiveJournal(String serialized) {
    if (!Game.isMultiplayerClient()) return;
    QuestLogComponent log = new QuestLogComponent();
    for (JournalEntry entry : JSON.readValue(serialized, JournalEntry[].class)) {
      log.add(
          entry.tab(),
          new QuestLogEntry(
              entry.text(), entry.timestamp(), entry.userCreated(), entry.owner(), false));
    }
    Entity journal = QuestLogUtil.getQuestLog().orElseGet(() -> new Entity("Programming journal"));
    journal.add(log);
    QuestLogUtil.setClientQuestLog(journal);
  }

  /**
   * Public journal wire representation.
   *
   * @param tab journal tab name
   * @param text displayed text
   * @param timestamp game tick when the entry was created
   * @param userCreated whether a player wrote the entry
   * @param owner journal entry owner
   */
  public record JournalEntry(
      String tab, String text, int timestamp, boolean userCreated, String owner) {}
}
