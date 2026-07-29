package contrib.questlog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.hud.elements.RichLabel;
import core.Entity;
import core.Game;
import core.language.Translation;
import core.network.NetworkUtils;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Collections;
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

  private static final DialogType DIALOG_TYPE = () -> "QUEST_LOG";
  private static final String CTX_TABS = "questlog.tabs";
  private static final String CTX_SELECTED_TAB = "questlog.selectedTab";
  private static final String CTX_ENTRY_TABS = "questlog.entryTabs";
  private static final String CTX_ENTRY_TEXTS = "questlog.entryTexts";
  private static final String CTX_ENTRY_OWNERS = "questlog.entryOwners";
  private static final String CTX_ENTRY_TIMESTAMPS = "questlog.entryTimestamps";
  private static final String T_TITLE = "title";
  private static final String T_EMPTY_QUESTLOG = "empty";
  private static final String T_CREATE = "create";
  private static final String T_NOTE_PROMPT = "note_prompt";
  private static final String T_NOTE_PLACEHOLDER = "note_placeholder";
  private static final String T_CANCEL = "cancel";
  private static final boolean USER_NOTE_ONLY_FOR_CREATOR = false;
  private static final float UI_WIDTH = 980f;
  private static final float UI_HEIGHT = 650f;
  private static final float SIDEBAR_WIDTH = 290f;
  private static final float ROW_HEIGHT = 62f;
  private static final float CONTENT_WIDTH = UI_WIDTH - SIDEBAR_WIDTH - 76f;
  private static final FontSpec FONT_TITLE =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 28, new Color(0.78f, 0.66f, 0.51f, 1f));
  private static final FontSpec FONT_SECTION =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 15, new Color(0.74f, 0.52f, 0.24f, 1f));
  private static final FontSpec FONT_BODY =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 16, new Color(0.70f, 0.68f, 0.61f, 1f));
  private static final FontSpec FONT_MUTED =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 13, new Color(0.46f, 0.45f, 0.40f, 1f));
  private static final FontSpec FONT_SELECTED =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 15, new Color(0.86f, 0.78f, 0.64f, 1f));
  private static final FontSpec FONT_ROW =
      FontSpec.of("fonts/Roboto-SemiBold.ttf", 15, new Color(0.62f, 0.60f, 0.54f, 1f));
  private static final Translation trans = new Translation("dialog.questlog");
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(QuestLogUI.class);

  static {
    DialogFactory.register(DIALOG_TYPE, QuestLogUI::build);
  }

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
    UIComponent ui =
        DialogFactory.show(createDialogContext(questLog, selectedTab), targetEntityIds);

    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          openCreateNoteDialog(selectedTabFrom(data), targetEntityIds);
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(DialogContextKeys.ON_CANCEL, data -> UIUtils.closeDialog(ui));
  }

  /**
   * Builds the quest log Scene2D dialog from a dialog context.
   *
   * <p>This method is registered as the custom quest log dialog builder. Networked clients receive
   * only primitive/string array attributes and reconstruct the visual state locally.
   *
   * @param ctx dialog context
   * @return renderable quest log UI or a headless placeholder
   */
  public static Group build(DialogContext ctx) {
    QuestLogViewData viewData = viewDataFrom(ctx);
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(
          trans.text(T_TITLE), viewData.toHeadlessText(), trans.text(T_CREATE));
    }
    return new BaseContainerUI(new QuestLogDialog(ctx.dialogId(), viewData), false, true);
  }

  private static DialogContext createDialogContext(QuestLogComponent questLog, String selectedTab) {
    QuestLogViewData viewData = viewDataFrom(questLog, selectedTab);
    return DialogContext.builder()
        .type(DIALOG_TYPE)
        .put(DialogContextKeys.TITLE, trans.text(T_TITLE))
        .put(CTX_TABS, viewData.tabs().toArray(new String[0]))
        .put(CTX_SELECTED_TAB, viewData.selectedTab())
        .put(CTX_ENTRY_TABS, viewData.entryTabs().toArray(new String[0]))
        .put(CTX_ENTRY_TEXTS, viewData.entryTexts().toArray(new String[0]))
        .put(CTX_ENTRY_OWNERS, viewData.entryOwners().toArray(new String[0]))
        .put(CTX_ENTRY_TIMESTAMPS, viewData.entryTimestamps())
        .build();
  }

  private static QuestLogViewData viewDataFrom(DialogContext ctx) {
    return new QuestLogViewData(
        List.of(ctx.find(CTX_TABS, String[].class).orElse(new String[0])),
        ctx.find(CTX_SELECTED_TAB, String.class).orElse(""),
        List.of(ctx.find(CTX_ENTRY_TABS, String[].class).orElse(new String[0])),
        List.of(ctx.find(CTX_ENTRY_TEXTS, String[].class).orElse(new String[0])),
        List.of(ctx.find(CTX_ENTRY_OWNERS, String[].class).orElse(new String[0])),
        ctx.find(CTX_ENTRY_TIMESTAMPS, int[].class).orElse(new int[0]));
  }

  private static QuestLogViewData viewDataFrom(QuestLogComponent questLog, String requestedTab) {
    QuestLogSelection selection = selectionFor(questLog, requestedTab);
    List<String> entryTabs = new ArrayList<>();
    List<String> entryTexts = new ArrayList<>();
    List<String> entryOwners = new ArrayList<>();
    List<Integer> entryTimestamps = new ArrayList<>();

    for (String tab : selection.tabs()) {
      for (QuestLogEntry entry : questLog.get(tab)) {
        entryTabs.add(tab);
        entryTexts.add(entryText(entry));
        entryOwners.add(entry.owner());
        entryTimestamps.add(entry.timestamp());
      }
    }

    return new QuestLogViewData(
        selection.tabs(),
        selection.selectedTab().orElse(""),
        entryTabs,
        entryTexts,
        entryOwners,
        entryTimestamps.stream().mapToInt(Integer::intValue).toArray());
  }

  private static void openCreateNoteDialog(String selectedTab, int... targetEntityIds) {
    if (selectedTab == null || selectedTab.isBlank()) {
      LOGGER.warn("Cannot create quest log entry without a selected tab.");
      return;
    }

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
        payload -> handleSubmittedNote(targetPlayerId, selectedTab, payload),
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
      int playerEntityId, String selectedTab, DialogResponseMessage.Payload payload) {
    Optional<String> note = noteTextFrom(payload);
    if (note.isEmpty()) {
      LOGGER.warn("Ignoring quest log note with unsupported payload '{}'.", payload);
      return;
    }

    Game.findEntityById(playerEntityId)
        .filter(
            player ->
                QuestLogUtil.addPlayerNote(
                    player, selectedTab, note.get(), USER_NOTE_ONLY_FOR_CREATOR))
        .ifPresent(player -> showQuestLogForPlayers(selectedTab, playerEntityId));
  }

  private static String selectedTabFrom(DialogResponseMessage.Payload payload) {
    if (payload instanceof DialogResponseMessage.StringValue(String selectedTab)) {
      return selectedTab;
    }
    LOGGER.warn("Quest log create action did not provide a selected tab payload '{}'.", payload);
    return "";
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
      builder.append(System.lineSeparator()).append("- ").append(entry.text());

      metadataFor(entry.owner())
          .ifPresent(metadata -> builder.append(" (").append(metadata).append(")"));
    }
  }

  private static Optional<String> metadataFor(String owner) {
    if (owner == null || owner.isBlank() || QuestLogEntry.DEFAULT_OWNER.equalsIgnoreCase(owner)) {
      return Optional.empty();
    }
    return Optional.of(owner);
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

  private record QuestLogEntryView(String tab, String text, String owner, int timestamp) {}

  private record QuestLogViewData(
      List<String> tabs,
      String selectedTab,
      List<String> entryTabs,
      List<String> entryTexts,
      List<String> entryOwners,
      int[] entryTimestamps) {

    private QuestLogViewData {
      tabs = List.copyOf(Objects.requireNonNull(tabs, "tabs"));
      selectedTab = Objects.requireNonNull(selectedTab, "selectedTab");
      entryTabs = List.copyOf(Objects.requireNonNull(entryTabs, "entryTabs"));
      entryTexts = List.copyOf(Objects.requireNonNull(entryTexts, "entryTexts"));
      entryOwners = List.copyOf(Objects.requireNonNull(entryOwners, "entryOwners"));
      entryTimestamps = Objects.requireNonNull(entryTimestamps, "entryTimestamps").clone();
    }

    private List<QuestLogEntryView> entriesFor(String tab) {
      if (tab == null || tab.isBlank()) {
        return List.of();
      }

      int entryCount =
          Collections.min(List.of(entryTabs.size(), entryTexts.size(), entryOwners.size()));
      entryCount = Math.min(entryCount, entryTimestamps.length);
      List<QuestLogEntryView> entries = new ArrayList<>();

      for (int i = 0; i < entryCount; i++) {
        if (tab.equals(entryTabs.get(i))) {
          entries.add(
              new QuestLogEntryView(
                  entryTabs.get(i), entryTexts.get(i), entryOwners.get(i), entryTimestamps[i]));
        }
      }
      return entries;
    }

    private String toHeadlessText() {
      if (tabs.isEmpty()) {
        return trans.text(T_EMPTY_QUESTLOG);
      }
      StringBuilder builder = new StringBuilder();
      for (String tab : tabs) {
        if (!builder.isEmpty()) {
          builder.append(System.lineSeparator()).append(System.lineSeparator());
        }
        builder.append(tab);
        for (QuestLogEntryView entry : entriesFor(tab)) {
          builder.append(System.lineSeparator()).append("- ").append(entry.text());
        }
      }
      return builder.toString();
    }
  }

  private static final class QuestLogDialog extends Table {
    private final String dialogId;
    private final QuestLogViewData viewData;
    private final Skin skin;
    private final Table sidebar;
    private final Container<Table> detailContainer;
    private final Drawable rowNormal;
    private final Drawable rowSelected;
    private String selectedTab;

    private QuestLogDialog(String dialogId, QuestLogViewData viewData) {
      this.dialogId = dialogId;
      this.viewData = viewData;
      this.skin = UIUtils.defaultSkin();
      this.sidebar = new Table();
      this.detailContainer = new Container<>();
      this.rowNormal = skin.newDrawable("generic-area", new Color(0.08f, 0.09f, 0.09f, 0.92f));
      this.rowSelected = skin.newDrawable("generic-area", new Color(0.30f, 0.23f, 0.15f, 0.98f));
      this.selectedTab = resolveInitialSelectedTab(viewData);

      buildLayout();
      refresh();
    }

    private void buildLayout() {
      setSize(UI_WIDTH, UI_HEIGHT);
      setBackground(skin.newDrawable("window_background_big", new Color(0.08f, 0.09f, 0.09f, 1f)));
      top().left();
      pad(18f);

      ScrollPane sidebarScroll = Scene2dElementFactory.createScrollPane(sidebar, false, true);
      sidebarScroll.setOverscroll(false, false);
      sidebarScroll.setFadeScrollBars(false);

      detailContainer
          .top()
          .left()
          .background(skin.newDrawable("generic-area", new Color(0.07f, 0.08f, 0.08f, 0.74f)));

      add(sidebarScroll).width(SIDEBAR_WIDTH).height(UI_HEIGHT - 36f).left().top();
      add(detailContainer)
          .width(UI_WIDTH - SIDEBAR_WIDTH - 36f)
          .height(UI_HEIGHT - 36f)
          .left()
          .top();
    }

    private void refresh() {
      rebuildSidebar();
      detailContainer.setActor(buildDetail());
      invalidateHierarchy();
    }

    private void rebuildSidebar() {
      sidebar.clearChildren();
      sidebar.top().left();
      sidebar.setBackground(skin.newDrawable("generic-area", new Color(0.05f, 0.06f, 0.06f, 1f)));

      if (viewData.tabs().isEmpty()) {
        sidebar
            .add(label(trans.text(T_EMPTY_QUESTLOG), FONT_MUTED, true))
            .width(SIDEBAR_WIDTH - 40f)
            .pad(22f)
            .left()
            .top();
        return;
      }

      for (String tab : viewData.tabs()) {
        sidebar.add(buildTabRow(tab)).width(SIDEBAR_WIDTH - 8f).height(ROW_HEIGHT).row();
      }
    }

    private Table buildTabRow(String tab) {
      boolean selected = tab.equals(selectedTab);
      Table row = new Table();
      row.left();
      row.setBackground(selected ? rowSelected : rowNormal);
      row.setTouchable(Touchable.enabled);

      RichLabel title =
          label(preview(sidebarLabel(tab), 34), selected ? FONT_SELECTED : FONT_ROW, false);
      row.add(title).growX().left().padLeft(22f).padRight(14f);

      row.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              selectedTab = tab;
              refresh();
            }
          });
      return row;
    }

    private Table buildDetail() {
      Table detail = new Table();
      detail.top().left();
      detail.pad(24f);

      Table header = new Table();
      header.left();
      header.add(label(detailTitle(selectedTab), FONT_TITLE, false)).growX().left();
      TextButton close = new TextButton("x", skin, "red-outline");
      close.getLabel().setFontScale(0.55f);
      close.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CANCEL)
                  .accept(null);
            }
          });
      header.add(close).size(34f).right();
      detail.add(header).width(CONTENT_WIDTH).padBottom(16f).row();

      List<QuestLogEntryView> entries = viewData.entriesFor(selectedTab);
      if (entries.isEmpty()) {
        detail
            .add(label(trans.text(T_EMPTY_QUESTLOG), FONT_BODY, true))
            .width(CONTENT_WIDTH)
            .left()
            .top()
            .padBottom(18f)
            .row();
      } else {
        addEntryList(detail, entries);
      }

      detail.add().growY().row();
      detail.add(buildFooter()).width(CONTENT_WIDTH).left().bottom();
      return detail;
    }

    private void addEntryList(Table detail, List<QuestLogEntryView> entries) {
      for (QuestLogEntryView entry : entries) {
        detail
            .add(label(entry.text(), FONT_BODY, true))
            .width(CONTENT_WIDTH)
            .left()
            .top()
            .padBottom(10f)
            .row();
        Optional<String> metadata = metadataFor(entry.owner());
        if (metadata.isPresent()) {
          detail
              .add(label(metadata.get(), FONT_MUTED, false))
              .width(CONTENT_WIDTH)
              .left()
              .padBottom(22f)
              .row();
        }
      }
    }

    private Table buildFooter() {
      Table footer = new Table();
      footer.left();

      TextButton addNote = new TextButton("+  " + trans.text(T_CREATE), skin, "blue-outline");
      addNote.getLabel().setFontScale(0.55f);
      addNote.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
                  .accept(new DialogResponseMessage.StringValue(selectedTab));
            }
          });
      footer.add(addNote).height(42f).width(190f).left();
      return footer;
    }

    private RichLabel label(String text, FontSpec font, boolean wrap) {
      RichLabel label = new RichLabel(RichLabel.toRichText(text), font, false);
      label.setWrap(wrap);
      return label;
    }

    private static String resolveInitialSelectedTab(QuestLogViewData viewData) {
      if (!viewData.selectedTab().isBlank() && viewData.tabs().contains(viewData.selectedTab())) {
        return viewData.selectedTab();
      }
      return viewData.tabs().stream().findFirst().orElse("");
    }
  }
}
