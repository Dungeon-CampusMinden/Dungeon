package rooms.systemRecovery.story;

import engine.Game;
import engine.components.PlayerComponent;
import feature.hud.dialogs.DialogFactory;
import feature.systems.LevelEditorSystem;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;

/**
 * Sends the remote user's instructions one step at a time.
 *
 * <p>Each {@link StoryStep} describes exactly one upcoming action. A step is queued only after the
 * previous action has succeeded, so the story never previews later parts of a riddle. The server
 * owns the queue; the client only receives the resulting dialog and questlog update.
 */
public final class SystemRecoveryStoryDialogs {

  private static final long STORY_DELAY_MS = 900L;
  private static final String REMOTE_USER = "[color=#aaaaaa]REMOTE USER[/color]";
  private static final String REMOTE_USER_SPEAKER =
      "[speaker img=logo/cat_logo_64x64.png name=\"" + REMOTE_USER + "\"]";

  /** The first instruction after the player pulls the damaged energy lever. */
  public static final StoryStep ENERGY_ARRAY =
      step(
          "energy-array",
          "riddle1",
          "array",
          "Der Energie-Riegel ist beschädigt. Erzeuge im Terminal ein ganzzahliges Array"
              + " namens energie mit fünf Plätzen.");

  /** The values required after the energy array exists. */
  public static final StoryStep ENERGY_VALUES =
      step(
          "energy-values",
          "riddle1",
          "values",
          "Das Array ist angelegt. Setze nun die Energie: energie[0] = 40, energie[1] = 10,"
              + " energie[2] = 80, energie[3] = 30 und energie[4] = 60.");

  /** The next declaration for the module-storage room. */
  public static final StoryStep MODULE_ARRAY =
      step(
          "module-array",
          "riddle2",
          "array",
          "Erzeuge im Terminal ein String-Array namens module mit fünf Plätzen.");

  /** The module assignments after the module array exists. */
  public static final StoryStep MODULE_VALUES =
      step(
          "module-values",
          "riddle2",
          "values",
          "Fülle module mit den vorhandenen Bauteilen: CPU, RAM, GPU, SSD und NETWORK."
              + " Die Positionen sind auf den Sockeln angegeben.");

  /** The single removal operation for the defective GPU. */
  public static final StoryStep REMOVE_GPU =
      step(
          "remove-gpu",
          "riddle2",
          "remove_gpu",
          "Die GPU ist defekt. Entferne den GPU-Eintrag aus module, indem du genau diesen"
              + " Steckplatz leer setzt.");

  /** The array-length read required before the scanner can be used. */
  public static final StoryStep READ_MODULE_LENGTH =
      step(
          "module-length",
          "riddle2",
          "length",
          "Lies jetzt die Länge des Arrays module aus. Die Anzeige im Raum prüft den Wert.");

  /** The counting loop for the inventory scanner. */
  public static final StoryStep SCANNER_CODE =
      step(
          "scanner-code",
          "riddle3",
          "loop",
          "Zähle die belegten Einträge von module. Prüfe in einer Schleife jeden Eintrag und"
              + " erhöhe count nur, wenn der Eintrag nicht leer ist.");

  /** The physical scanner action after its code has been accepted. */
  public static final StoryStep SCANNER_LEVER =
      step(
          "scanner-lever",
          "riddle3",
          "scan",
          "Der Zählcode ist akzeptiert. Betätige jetzt den Scanner-Hebel und beobachte die"
              + " Prüfung der fünf Modulpositionen.");

  /** The package array declaration for the transport storage. */
  public static final StoryStep PACKAGES_ARRAY =
      step(
          "packages-array",
          "riddle4",
          "array",
          "Lege im Terminal ein ganzzahliges Array namens pakete an. Verwende die fünf"
              + " Gewichte 15, 40, 20, 60 und 30 in dieser Reihenfolge.");

  /** The loop that hands every package to the transport scanner. */
  public static final StoryStep PACKAGES_LOOP =
      step(
          "packages-loop",
          "riddle4",
          "loop",
          "Übergib jetzt jedes Paket genau einmal an den Lager-Scanner. Beginne beim ersten"
              + " Index und nutze die Länge von pakete als Schleifengrenze.");

  /** The next manual comparison after the transport sequence. */
  public static final StoryStep MANUAL_SORTING =
      step(
          "manual-sorting",
          "riddle5",
          "compare",
          "Die Sicherheitswerte sind ungeordnet. Untersuche am Display das aktuelle"
              + " Nachbarpaar und entscheide mit den beiden Schaltflächen, ob du es tauschst."
              + " Eine falsche Entscheidung setzt die Sortierung zurück.");

  /** The missing comparison expression for the sort-program chip. */
  public static final StoryStep BUBBLE_SORT_CODE =
      step(
          "bubble-sort-code",
          "riddle6",
          "code",
          "Die manuelle Sortierung ist abgeschlossen. Öffne am Rechner den vorbereiteten"
              + " Bubble-Sort-Code und ergänze die fehlende Vergleichsbedingung. Speichere"
              + " anschließend den vollständigen Code auf dem Sortierchip.");

  /** The action that starts the physical bubble-sort machine. */
  public static final StoryStep BUBBLE_SORT_MACHINE =
      step(
          "bubble-sort-machine",
          "riddle6",
          "machine",
          "Der Sortierchip ist programmiert. Setze ihn in die Bubble-Sort-Maschine ein.");

  /** The three array declarations for the data archive. */
  public static final StoryStep ARCHIVE_ARRAYS =
      step(
          "archive-arrays",
          "riddle7",
          "arrays",
          "Lege im Datenarchiv die drei benötigten Arrays an: energie für Zahlen, module"
              + " für Text und aktiv für Wahrheitswerte. Verwende jeweils die angezeigten"
              + " fünf beziehungsweise drei Einträge.");

  /** The two-dimensional array declaration for the storage room. */
  public static final StoryStep STORAGE_ARRAY =
      step(
          "storage-array",
          "riddle8",
          "create",
          "Erzeuge für den zweidimensionalen Speicher ein ganzzahliges Raster mit drei"
              + " Zeilen und vier Spalten.");

  /** The marked coordinate assignments in the storage room. */
  public static final StoryStep STORAGE_VALUES =
      step(
          "storage-values",
          "riddle8",
          "fill",
          "Befülle die markierten Speicherstellen: [0][2] mit 1, [1][3] mit 2 und [2][1]"
              + " mit 3.");

  /** The requested cell read after the storage grid has been filled. */
  public static final StoryStep STORAGE_READ =
      step(
          "storage-read",
          "riddle8",
          "read",
          "Lies anschließend die angeforderte Zelle aus dem Raster aus. Verwende zuerst den"
              + " Zeilenindex und danach den Spaltenindex.");

  /** The nested search loop for the battery map. */
  public static final StoryStep SEARCH_ROBOT =
      step(
          "search-robot",
          "riddle9",
          "search",
          "Durchsuche die gesamte Karte mit einer äußeren und einer inneren Schleife. Wenn"
              + " du das Batteriesignal findest, rufe roboter.collect() auf.");

  /** The first central-computer check. */
  public static final StoryStep CENTRAL_SORT =
      step(
          "central-sort",
          "riddle10",
          "sort",
          "Im Rechenzentrum wartet die erste Prüfung: Vergleiche benachbarte Werte des"
              + " Arrays und vertausche sie, wenn sie nicht aufsteigend geordnet sind.");

  /** The central module-count check. */
  public static final StoryStep CENTRAL_COUNT =
      step(
          "central-count",
          "riddle10",
          "count",
          "Zähle nun die belegten Einträge in modules. Leere Einträge dürfen den Zähler"
              + " nicht erhöhen.");

  /** The central map search check. */
  public static final StoryStep CENTRAL_SEARCH =
      step(
          "central-search",
          "riddle10",
          "search",
          "Durchsuche zum Abschluss das gesamte Raster. Bei jeder gefundenen Batterie soll"
              + " roboter.collect() ausgeführt werden.");

  /** The final story response after all central checks. */
  public static final StoryStep COMPLETED =
      step(
          "completed",
          "riddle10",
          "complete",
          "Alle Prüfungen sind bestätigt. Die Systemwiederherstellung kann fortgesetzt werden.");

  private final Set<String> shownToPlayer = ConcurrentHashMap.newKeySet();
  private final Queue<PendingDialog> pendingDialogs = new ConcurrentLinkedQueue<>();
  private static final ThreadLocal<Integer> TERMINAL_PLAYER = new ThreadLocal<>();

  /** Creates the story controller for one authoritative level instance. */
  public SystemRecoveryStoryDialogs() {}

  /** Dispatches delayed dialogs. This is intentionally inert while the level editor is active. */
  public void tick() {
    if (!Game.isHeadless() && LevelEditorSystem.active()) return;

    long now = System.currentTimeMillis();
    PendingDialog pending;
    while ((pending = pendingDialogs.peek()) != null && pending.executeAt() <= now) {
      pendingDialogs.poll();
      SystemRecoveryQuestLogUtil.addDialogEntry(
          pending.step().riddleKey(), pending.step().entryKey());
      DialogFactory.showDialogDialog(pending.step().script(), () -> {}, pending.playerId());
    }
  }

  /** Queues one instruction for one player after a successful personal action. */
  public void announceForPlayer(StoryStep step, int playerId) {
    showStepAfterDelay(step, playerId);
  }

  /** Queues one shared-room instruction for every currently connected player. */
  public void announceToAllPlayers(StoryStep step) {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .mapToInt(engine.Entity::id)
        .forEach(playerId -> showStepAfterDelay(step, playerId));
  }

  /** Queues the final shared story response. */
  public void announceCompletionToAllPlayers() {
    announceToAllPlayers(COMPLETED);
  }

  /** Executes terminal work with the submitting player available to success callbacks. */
  public static <T> T withTerminalPlayer(int playerId, Supplier<T> action) {
    Integer previous = TERMINAL_PLAYER.get();
    TERMINAL_PLAYER.set(playerId);
    try {
      return action.get();
    } finally {
      if (previous == null) TERMINAL_PLAYER.remove();
      else TERMINAL_PLAYER.set(previous);
    }
  }

  /**
   * @return the player currently executing a terminal callback, if there is one
   */
  public static OptionalInt currentTerminalPlayer() {
    Integer playerId = TERMINAL_PLAYER.get();
    return playerId == null ? OptionalInt.empty() : OptionalInt.of(playerId);
  }

  private void showStepAfterDelay(StoryStep step, int playerId) {
    if (step == null || playerId < 0) return;
    String key = step.id() + ":" + playerId;
    if (!shownToPlayer.add(key)) return;
    pendingDialogs.add(
        new PendingDialog(System.currentTimeMillis() + STORY_DELAY_MS, step, playerId));
  }

  private static StoryStep step(String id, String riddleKey, String entryKey, String text) {
    return new StoryStep(id, riddleKey, entryKey, REMOTE_USER_SPEAKER + text);
  }

  /** One atomic instruction shown after the previous puzzle action. */
  public record StoryStep(String id, String riddleKey, String entryKey, String script) {}

  private record PendingDialog(long executeAt, StoryStep step, int playerId) {}
}
