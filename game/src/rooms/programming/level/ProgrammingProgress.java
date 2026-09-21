package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.tracking.Tracking;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopRune;
import tools.jackson.databind.json.JsonMapper;

/** Authoritative journal and tracking for the room's discoveries and learning progress. */
public final class ProgrammingProgress {
  private static final Set<String> RECORDED = new HashSet<>();
  private static final JsonMapper JSON = JsonMapper.builder().build();

  private ProgrammingProgress() {}

  /** Starts a fresh room journal before spawning puzzle runtimes. */
  static void initialize() {
    RECORDED.clear();
    Game.add(QuestLogUtil.initServerQuestLog());
    record(
        "arrival",
        "Aufgaben",
        "Erkunde Valerius' Werkstatt und finde heraus, wie du Nox wieder erwecken kannst.");
  }

  /** Records an available puzzle once, without revealing its solution. */
  public static void started(String puzzleId, String objective) {
    if (Game.isMultiplayerClient()) return;
    Tracking.puzzleStarted(puzzleId);
    record("start:" + puzzleId, "Aufgaben", objective);
  }

  /** Records the real successful puzzle outcome once. */
  public static void solved(String puzzleId, String summary) {
    if (Game.isMultiplayerClient()) return;
    Tracking.puzzleSolved(puzzleId);
    record("solved:" + puzzleId, "Fortschritt", summary);
  }

  /** Resolves the actor at submission time so delayed outcomes retain their attribution. */
  public static Optional<UUID> participant(Entity who) {
    return who == null ? Optional.empty() : Tracking.participantForEntity(who.id());
  }

  /** Stores each complete submitted answer and its authoritative outcome. */
  public static void attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      UUID participantId) {
    if (Game.isMultiplayerClient()) return;
    Tracking.attempt(puzzleId, objectId, answerKind, rawAnswer, correct, participantId);
  }

  /** Keeps released help in the journal and attributes it to the requesting participant. */
  public static void hint(String puzzleId, String hintId, String text, Entity who) {
    if (Game.isMultiplayerClient()) return;
    participant(who).ifPresent(id -> Tracking.hintUsed(puzzleId, hintId, id));
    record("hint:" + puzzleId + ":" + hintId, "Hilfe", text);
  }

  /** Records a meaningful action without adding repetitive journal entries. */
  public static void interaction(String objectId, String actionId, Entity who) {
    participant(who).ifPresent(id -> interaction(objectId, actionId, id));
  }

  /** Records a meaningful action using an actor captured before asynchronous execution. */
  public static void interaction(String objectId, String actionId, UUID participantId) {
    if (!Game.isMultiplayerClient()) Tracking.interaction(objectId, actionId, participantId);
  }

  /** Adds found information once; repeated visits remain visible in tracking. */
  public static void discover(String objectId, String title, String text, Entity who) {
    if (Game.isMultiplayerClient()) return;
    interaction(objectId, "discover", who);
    record("discovery:" + objectId, "Fundstücke", title + "\n\n" + text);
  }

  /** Gives collected runes distinct titles even while their source is collapsed. */
  static void discoverRune(LoopRune rune, Entity who) {
    String type = rune.program().type().name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    discover(
        "rune-" + rune.id(),
        "Schleifenrune " + (LoopPuzzle.runes().indexOf(rune) + 1) + " · " + type,
        rune.code(),
        who);
  }

  private static void record(String key, String tab, String text) {
    if (!RECORDED.contains(key) && QuestLogUtil.add(tab, text)) RECORDED.add(key);
  }

  /** Serializes only public entries; private notes travel in the requesting player's dialog. */
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

  /** Restores the synchronized public journal even before its carrier entity has spawned. */
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

  /** Public journal wire representation. */
  public record JournalEntry(
      String tab, String text, int timestamp, boolean userCreated, String owner) {}
}
