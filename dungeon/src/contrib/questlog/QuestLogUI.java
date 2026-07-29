package contrib.questlog;

import contrib.hud.dialogs.DialogFactory;
import core.Entity;
import core.Game;
import core.language.Translation;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.utils.logging.DungeonLogger;
import java.util.Optional;

/**
 * Proof-of-concept UI helpers for displaying or printing the shared quest log.
 *
 * <p>This class intentionally uses a simple OK dialog and plain text formatting so the quest log
 * can be tested through the existing multiplayer UI pipeline. It is not intended to be the final
 * player experience. A real quest log UI should replace this proof of concept with a dedicated
 * dialog that can show tabs, entry metadata, filtering, and per-player visibility in a proper
 * layout.
 *
 * <p>The multiplayer path mirrors other high-level client/server helpers: clients request the quest
 * log with a custom input command, the server reads the authoritative {@link QuestLogComponent},
 * and the server sends a dialog containing the formatted quest log back to the requesting client.
 * When the player creates a note from that dialog, the text input response is sent back to the
 * server and the server adds the entry with the requesting player as creator.
 */
public final class QuestLogUI {

  /** Custom input command used by clients to request the quest log from the server. */
  public static final String COMMAND_SHOW_QUESTLOG = "core:questlog";

  private static final String USER_NOTE_TAB = "Notes";
  private static final String T_TITLE = "title";
  private static final String T_EMPTY_QUESTLOG = "empty";
  private static final String T_CREATE = "create";
  private static final String T_NOTE_PROMPT = "note_prompt";
  private static final String T_NOTE_PLACEHOLDER = "note_placeholder";
  private static final String T_CANCEL = "cancel";
  private static final Translation trans = new Translation("dialog.questlog");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(QuestLogUI.class);

  private QuestLogUI() {}

  /**
   * Requests that the quest log is shown for the given player.
   *
   * <p>On a network client this sends {@link #COMMAND_SHOW_QUESTLOG} to the server. In local or
   * server-side contexts this opens the current proof-of-concept quest log dialog directly for the
   * given player.
   *
   * @param player player entity requesting the quest log
   */
  public static void requestQuestLog(Entity player) {
    if (core.network.NetworkUtils.isNetworkClient()) {
      Game.network().sendInput(InputMessage.custom(COMMAND_SHOW_QUESTLOG));
    } else {
      printQuestLogForClients(player.id());
    }
  }

  /**
   * Prints the shared quest log to the local console.
   *
   * <p>This method exists only for debugging this proof of concept. Gameplay code should use {@link
   * #requestQuestLog(Entity)}.
   */
  public static void printQuestLog() {
    QuestLogUtil.getQuestLogComponent()
        .ifPresentOrElse(
            questLog -> System.out.println(formatQuestLog(questLog)),
            QuestLogUI::logMissingQuestLog);
  }

  /**
   * Prints the given quest log component to the local console.
   *
   * <p>This method exists only for debugging this proof of concept. Gameplay code should use {@link
   * #requestQuestLog(Entity)}.
   *
   * @param questLog the quest log component to print
   */
  public static void printQuestLog(QuestLogComponent questLog) {
    System.out.println(formatQuestLog(questLog));
  }

  /**
   * Sends the shared quest log from the server to clients and asks them to display it.
   *
   * <p>This proof-of-concept method creates a text dialog containing the formatted quest log and a
   * create button. The create button opens a text input dialog for the requesting player. When the
   * player submits a non-blank note, the server adds it to the shared quest log with the player
   * name as creator.
   *
   * <p>When called on a headless multiplayer server, the {@link contrib.systems.HudSystem} sends
   * that dialog to the selected clients. If no target entity IDs are provided, all connected
   * clients receive it, but note creation needs exactly one requesting player entity so the creator
   * can be resolved.
   *
   * <p>If the quest log was not initialized, this method does not show anything to players. It logs
   * the missing setup in the background and returns {@code false}.
   *
   * @param targetEntityIds optional player entity IDs that should receive the quest log
   * @return {@code true} if a quest log was available and the display request was created, {@code
   *     false} if the quest log was not initialized
   */
  public static boolean printQuestLogForClients(int... targetEntityIds) {
    return QuestLogUtil.getQuestLogComponent()
        .map(
            questLog -> {
              DialogFactory.showTextDialog(
                  formatQuestLog(questLog),
                  trans.text(T_TITLE),
                  () -> showCreateEntryDialog(targetEntityIds),
                  trans.text(T_CREATE),
                  targetEntityIds);
              return true;
            })
        .orElseGet(
            () -> {
              logMissingQuestLog();
              return false;
            });
  }

  private static void showCreateEntryDialog(int... targetEntityIds) {
    Optional<Integer> playerId = requestingPlayerId(targetEntityIds);
    if (playerId.isEmpty()) {
      LOGGER.warn("Cannot create quest log entry without exactly one requesting player.");
      return;
    }

    int targetPlayerId = playerId.get();
    DialogFactory.showInputDialog(
        trans.text(T_NOTE_PROMPT),
        trans.text(T_TITLE),
        "",
        trans.text(T_NOTE_PLACEHOLDER),
        trans.text(T_CREATE),
        trans.text(T_CANCEL),
        payload -> addPlayerNote(targetPlayerId, payload),
        () -> {},
        targetPlayerId);
  }

  private static Optional<Integer> requestingPlayerId(int... targetEntityIds) {
    if (targetEntityIds.length == 1) {
      return Optional.of(targetEntityIds[0]);
    }
    if (targetEntityIds.length == 0) {
      return Game.player().map(Entity::id);
    }
    return Optional.empty();
  }

  private static void addPlayerNote(int playerEntityId, DialogResponseMessage.Payload payload) {
    if (!(payload instanceof DialogResponseMessage.StringValue(String note))) {
      LOGGER.warn("Ignoring quest log note with unsupported payload '{}'.", payload);
      return;
    }

    Game.findEntityById(playerEntityId)
        .filter(player -> QuestLogUtil.addPlayerNote(player, USER_NOTE_TAB, note, false))
        .ifPresent(player -> printQuestLogForClients(playerEntityId));
  }

  /**
   * Formats a quest log as readable text for the proof-of-concept quest log dialog.
   *
   * <p>Tabs are ordered by their newest entry first. Entries inside each tab keep their insertion
   * order.
   *
   * @param questLog the quest log component to format
   * @return a readable quest log representation
   */
  public static String formatQuestLog(QuestLogComponent questLog) {
    StringBuilder builder = new StringBuilder();

    for (String tab : questLog.getTabsOrderedByLastEntry()) {
      appendTab(builder, tab, questLog.get(tab));
    }

    if (builder.isEmpty()) {
      return trans.text(T_EMPTY_QUESTLOG);
    }

    return builder.toString();
  }

  private static void appendTab(
      StringBuilder builder, String tab, Iterable<QuestLogEntry> entries) {
    if (!builder.isEmpty()) {
      builder.append(System.lineSeparator()).append(System.lineSeparator());
    }

    builder.append("[").append(tab).append("]");
    for (QuestLogEntry entry : entries) {
      builder
          .append(System.lineSeparator())
          .append("- ")
          .append(entry.text())
          .append(" (")
          .append(entry.owner())
          .append(", tick ")
          .append(entry.timestamp())
          .append(")");
    }
  }

  private static void logMissingQuestLog() {
    LOGGER.warn("Quest log was requested, but no QuestLogComponent is initialized.");
  }
}
