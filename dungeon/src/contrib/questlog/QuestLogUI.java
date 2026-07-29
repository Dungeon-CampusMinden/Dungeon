package contrib.questlog;

import contrib.hud.dialogs.DialogFactory;
import core.Entity;
import core.Game;
import core.language.Translation;
import core.network.NetworkUtils;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Placeholder UI for displaying the shared quest log and creating player notes.
 *
 * <p>This class is intentionally small and plain. It uses existing text/input dialogs and a simple
 * formatted string so the quest log can be tested through the current multiplayer UI pipeline. It
 * is not meant to be the final frontend. A production UI should replace the dialog construction and
 * formatting parts with a dedicated view for tabs, entries, filtering, and visibility.
 *
 * <p>The server-backed flow is the important part to keep:
 *
 * <ol>
 *   <li>A client calls {@link #requestQuestLog(Entity)}.
 *   <li>The server receives {@link #COMMAND_SHOW_QUESTLOG}.
 *   <li>The server reads the authoritative {@link QuestLogComponent}.
 *   <li>The server shows this placeholder dialog for the requesting player.
 *   <li>If the player creates a note, the input response returns to the server.
 *   <li>The server stores the note through {@link QuestLogUtil#addPlayerNote(Entity, String,
 *       String, boolean)}.
 * </ol>
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
  private static final boolean USER_NOTE_ONLY_FOR_CREATOR = false;
  private static final Translation trans = new Translation("dialog.questlog");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(QuestLogUI.class);

  private QuestLogUI() {}

  /**
   * Requests that the quest log is shown for the given player.
   *
   * <p>On a network client this sends {@link #COMMAND_SHOW_QUESTLOG} to the server. In local or
   * server-side contexts this opens the placeholder quest log dialog directly for the given player.
   *
   * @param player player entity requesting the quest log
   */
  public static void requestQuestLog(Entity player) {
    if (NetworkUtils.isNetworkClient()) {
      Game.network().sendInput(InputMessage.custom(COMMAND_SHOW_QUESTLOG));
    } else {
      showQuestLogForPlayers(player.id());
    }
  }

  /**
   * Shows the shared quest log for the requested players.
   *
   * <p>This placeholder method creates a text dialog containing the formatted quest log and a
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
  public static boolean showQuestLogForPlayers(int... targetEntityIds) {
    return showQuestLogForPlayers(null, targetEntityIds);
  }

  /**
   * Shows the shared quest log for the requested players with a preferred selected tab.
   *
   * <p>The selected tab is resolved defensively: if the requested tab is missing or blank, the
   * newest tab is selected. This keeps the future dedicated quest log UI independent from the
   * storage order and gives callers a stable entry point for restoring or changing the sidebar
   * selection.
   *
   * @param selectedTab preferred tab to show in the detail area; may be {@code null}
   * @param targetEntityIds optional player entity IDs that should receive the quest log
   * @return {@code true} if a quest log was available and the display request was created, {@code
   *     false} if the quest log was not initialized
   */
  public static boolean showQuestLogForPlayers(String selectedTab, int... targetEntityIds) {
    return QuestLogUtil.getQuestLogComponent()
        .map(
            questLog -> {
              showFormattedQuestLogDialog(questLog, selectedTab, targetEntityIds);
              return true;
            })
        .orElseGet(
            () -> {
              logMissingQuestLog();
              return false;
            });
  }

  private static void showFormattedQuestLogDialog(
      QuestLogComponent questLog, String selectedTab, int... targetEntityIds) {
    DialogFactory.showTextDialog(
        formatQuestLogSelection(questLog, selectedTab),
        trans.text(T_TITLE),
        () -> openCreateNoteDialog(targetEntityIds),
        trans.text(T_CREATE),
        targetEntityIds);
  }

  private static void openCreateNoteDialog(int... targetEntityIds) {
    Optional<Integer> playerId = resolveSingleTargetPlayerId(targetEntityIds);
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
        payload -> handleSubmittedNote(targetPlayerId, payload),
        () -> {},
        targetPlayerId);
  }

  private static Optional<Integer> resolveSingleTargetPlayerId(int... targetEntityIds) {
    if (targetEntityIds.length == 1) {
      return Optional.of(targetEntityIds[0]);
    }
    if (targetEntityIds.length == 0) {
      return Game.player().map(Entity::id);
    }
    return Optional.empty();
  }

  private static void handleSubmittedNote(
      int playerEntityId, DialogResponseMessage.Payload payload) {
    Optional<String> note = noteTextFrom(payload);
    if (note.isEmpty()) {
      LOGGER.warn("Ignoring quest log note with unsupported payload '{}'.", payload);
      return;
    }

    Game.findEntityById(playerEntityId)
        .filter(
            player ->
                QuestLogUtil.addPlayerNote(
                    player, USER_NOTE_TAB, note.get(), USER_NOTE_ONLY_FOR_CREATOR))
        .ifPresent(player -> showQuestLogForPlayers(playerEntityId));
  }

  private static Optional<String> noteTextFrom(DialogResponseMessage.Payload payload) {
    if (payload instanceof DialogResponseMessage.StringValue(String note)) {
      return Optional.of(note);
    }
    return Optional.empty();
  }

  /**
   * Formats a quest log as readable text for the placeholder quest log dialog.
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

  /**
   * Builds the selection state for a quest log UI.
   *
   * <p>Tabs are ordered by newest entry first. The selected tab is the requested tab if it exists,
   * otherwise the newest tab. If the quest log is empty, {@link QuestLogSelection#selectedTab()} is
   * empty and {@link QuestLogSelection#selectedEntries()} returns an empty list.
   *
   * @param questLog quest log to read
   * @param requestedTab preferred selected tab; may be {@code null}
   * @return immutable selection state for sidebar/detail rendering
   */
  public static QuestLogSelection selectionFor(QuestLogComponent questLog, String requestedTab) {
    Objects.requireNonNull(questLog, "questLog");

    List<String> tabs = questLog.getTabsOrderedByLastEntry();
    Optional<String> selectedTab = selectTab(tabs, requestedTab);
    List<QuestLogEntry> selectedEntries = selectedTab.map(questLog::get).orElseGet(List::of);

    return new QuestLogSelection(tabs, selectedTab, selectedEntries);
  }

  /**
   * Resolves the selected tab from an ordered tab list.
   *
   * @param orderedTabs available tabs, already sorted for display
   * @param requestedTab preferred selected tab; may be {@code null}
   * @return requested tab if present, otherwise the first available tab
   */
  public static Optional<String> selectTab(List<String> orderedTabs, String requestedTab) {
    Objects.requireNonNull(orderedTabs, "orderedTabs");

    if (requestedTab != null && !requestedTab.isBlank() && orderedTabs.contains(requestedTab)) {
      return Optional.of(requestedTab);
    }
    return orderedTabs.stream().findFirst();
  }

  /**
   * Selects the next tab in display order, wrapping to the first tab at the end.
   *
   * @param questLog quest log to read
   * @param currentTab currently selected tab; may be {@code null}
   * @return next tab, or empty when the quest log has no tabs
   */
  public static Optional<String> selectNextTab(QuestLogComponent questLog, String currentTab) {
    Objects.requireNonNull(questLog, "questLog");
    return selectTabWithOffset(questLog.getTabsOrderedByLastEntry(), currentTab, 1);
  }

  /**
   * Selects the previous tab in display order, wrapping to the last tab at the beginning.
   *
   * @param questLog quest log to read
   * @param currentTab currently selected tab; may be {@code null}
   * @return previous tab, or empty when the quest log has no tabs
   */
  public static Optional<String> selectPreviousTab(QuestLogComponent questLog, String currentTab) {
    Objects.requireNonNull(questLog, "questLog");
    return selectTabWithOffset(questLog.getTabsOrderedByLastEntry(), currentTab, -1);
  }

  /**
   * Formats the selected quest view for the current placeholder dialog.
   *
   * <p>The output mirrors the intended visual UI structure: a sidebar list with the active quest
   * marked, followed by the selected quest detail entries. A dedicated Scene2D implementation can
   * use {@link #selectionFor(QuestLogComponent, String)} directly instead of parsing this text.
   *
   * @param questLog quest log to format
   * @param requestedTab preferred selected tab; may be {@code null}
   * @return readable selected quest view
   */
  public static String formatQuestLogSelection(QuestLogComponent questLog, String requestedTab) {
    QuestLogSelection selection = selectionFor(questLog, requestedTab);
    if (selection.isEmpty()) {
      return trans.text(T_EMPTY_QUESTLOG);
    }

    StringBuilder builder = new StringBuilder();
    appendSidebar(builder, selection);
    builder.append(System.lineSeparator()).append(System.lineSeparator());
    appendSelectedDetail(builder, selection);
    return builder.toString();
  }

  /**
   * Returns a compact sidebar label for a quest tab.
   *
   * @param tab tab name
   * @return trimmed tab label
   */
  public static String sidebarLabel(String tab) {
    return tab == null ? "" : tab.strip();
  }

  /**
   * Returns the title for the selected quest detail pane.
   *
   * @param selectedTab selected tab
   * @return display title
   */
  public static String detailTitle(String selectedTab) {
    return sidebarLabel(selectedTab);
  }

  /**
   * Returns the display text for a quest log entry without metadata.
   *
   * @param entry quest log entry
   * @return entry text, trimmed for display
   */
  public static String entryText(QuestLogEntry entry) {
    Objects.requireNonNull(entry, "entry");
    return entry.text().strip();
  }

  /**
   * Returns a short preview suitable for a narrow sidebar or collapsed row.
   *
   * @param text text to shorten
   * @param maxLength maximum display length, including ellipsis
   * @return shortened text
   */
  public static String preview(String text, int maxLength) {
    if (maxLength < 1) {
      throw new IllegalArgumentException("maxLength must be at least 1");
    }

    String normalized = text == null ? "" : text.strip().replaceAll("\\s+", " ");
    if (normalized.length() <= maxLength) {
      return normalized;
    }
    if (maxLength == 1) {
      return ".";
    }
    return normalized.substring(0, maxLength - 1).stripTrailing() + ".";
  }

  private static Optional<String> selectTabWithOffset(
      List<String> orderedTabs, String currentTab, int offset) {
    if (orderedTabs.isEmpty()) {
      return Optional.empty();
    }

    Optional<String> selectedTab = selectTab(orderedTabs, currentTab);
    int selectedIndex = selectedTab.map(orderedTabs::indexOf).orElse(0);
    int nextIndex = Math.floorMod(selectedIndex + offset, orderedTabs.size());
    return Optional.of(orderedTabs.get(nextIndex));
  }

  private static void appendSidebar(StringBuilder builder, QuestLogSelection selection) {
    builder.append(trans.text(T_TITLE)).append(System.lineSeparator());

    for (String tab : selection.tabs()) {
      boolean selected = selection.selectedTab().filter(tab::equals).isPresent();
      builder
          .append(selected ? "> " : "  ")
          .append(sidebarLabel(tab))
          .append(System.lineSeparator());
    }
  }

  private static void appendSelectedDetail(StringBuilder builder, QuestLogSelection selection) {
    String selectedTab = selection.selectedTab().orElse("");
    builder.append(detailTitle(selectedTab));

    for (QuestLogEntry entry : selection.selectedEntries()) {
      builder.append(System.lineSeparator()).append("- ").append(entryText(entry));
    }
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

  /**
   * Immutable view state for rendering a quest log with a sidebar selection and detail pane.
   *
   * @param tabs ordered sidebar tabs
   * @param selectedTab selected tab, or empty for an empty quest log
   * @param selectedEntries entries for the selected tab
   */
  public record QuestLogSelection(
      List<String> tabs, Optional<String> selectedTab, List<QuestLogEntry> selectedEntries) {

    /**
     * Creates immutable defensive copies of the selection lists.
     *
     * @param tabs ordered sidebar tabs
     * @param selectedTab selected tab, or empty for an empty quest log
     * @param selectedEntries entries for the selected tab
     */
    public QuestLogSelection {
      tabs = List.copyOf(Objects.requireNonNull(tabs, "tabs"));
      selectedTab = Objects.requireNonNull(selectedTab, "selectedTab");
      selectedEntries = List.copyOf(Objects.requireNonNull(selectedEntries, "selectedEntries"));
    }

    /**
     * Returns whether this selection contains no tabs.
     *
     * @return {@code true} when no quest can be selected
     */
    public boolean isEmpty() {
      return selectedTab.isEmpty();
    }
  }
}
