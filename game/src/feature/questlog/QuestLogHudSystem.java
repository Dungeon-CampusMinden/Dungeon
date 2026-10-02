package feature.questlog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.System;
import engine.components.PlayerComponent;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.UIUtils;
import feature.input.configuration.KeyboardConfig;
import java.util.HashSet;
import java.util.Set;

/**
 * Client-side HUD button in the bottom-right corner that opens the quest log.
 *
 * <p>The button shows the quest log key binding and a badge with the number of entries the local
 * player has not read yet. An entry counts as read once the quest log dialog displayed it on this
 * client. The read state is local, not persisted, and reset whenever no quest log exists.
 */
public final class QuestLogHudSystem extends System {

  private static final String ICON_PATH = "items/rpg/item_book_brown.png";
  private static final float ICON_SIZE = 56f;
  static final float BADGE_SIZE = 26f;
  private static final float SCREEN_MARGIN = 16f;
  private static final int MAX_BADGE_COUNT = 9;
  private static final FontSpec FONT_BADGE = FontSpec.of(14, Color.WHITE);
  private static final FontSpec FONT_KEY = FontSpec.of(18, Color.WHITE, 2f, Color.BLACK);

  private static final Set<ReadKey> READ_ENTRIES = new HashSet<>();

  private Table hud;
  private Table badge;
  private Label badgeLabel;
  private Label keyLabel;

  /** Creates the quest log HUD system. */
  public QuestLogHudSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (Game.isHeadless()) return;
    Game.stage()
        .ifPresent(
            stage -> {
              if (hud == null) hud = buildHud();
              if (hud.getStage() != stage) {
                stage.addActor(hud);
                hud.toBack();
              }
              if (!QuestLogUtil.isInitialized()) READ_ENTRIES.clear();
              hud.setVisible(QuestLogUtil.isInitialized() && Game.player().isPresent());
              if (!hud.isVisible()) return;

              int unread = unreadCount();
              badge.setVisible(unread > 0);
              badgeLabel.setText(badgeText(unread));
              keyLabel.setText(Input.Keys.toString(KeyboardConfig.QUESTLOG_OPEN.value()));
              hud.pack();
              hud.setPosition(stage.getWidth() - SCREEN_MARGIN, SCREEN_MARGIN, Align.bottomRight);
            });
  }

  private Table buildHud() {
    Image icon = new Image(TextureMap.instance().textureAt(new SimpleIPath(ICON_PATH)));
    badgeLabel = Scene2dElementFactory.createLabel("", FONT_BADGE);
    badge = badge(badgeLabel);
    Table badgeOverlay = new Table();
    badgeOverlay.top().right().add(badge).size(BADGE_SIZE);

    Stack iconStack = new Stack(icon, badgeOverlay);
    keyLabel = Scene2dElementFactory.createLabel("", FONT_KEY);

    Table table = new Table();
    table.setTouchable(Touchable.enabled);
    table.add(iconStack).size(ICON_SIZE).row();
    table.add(keyLabel);
    table.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            Game.player().ifPresent(QuestLogUI::requestQuestLog);
          }
        });
    return table;
  }

  /**
   * Creates the red unread badge used by the HUD button and the quest log sidebar.
   *
   * @param count number of unread entries
   * @return badge actor showing the count, capped at "9+"
   */
  static Table badge(int count) {
    return badge(Scene2dElementFactory.createLabel(badgeText(count), FONT_BADGE));
  }

  private static Table badge(Label label) {
    label.setAlignment(Align.center);
    Table badge = new Table();
    badge.setBackground(
        UIUtils.defaultSkin().newDrawable("round-white", Color.valueOf("D32F2FFF")));
    badge.pad(0);
    badge.add(label).grow();
    return badge;
  }

  private static String badgeText(int count) {
    return count > MAX_BADGE_COUNT ? MAX_BADGE_COUNT + "+" : String.valueOf(count);
  }

  /**
   * Marks an entry shown in the quest log dialog as read.
   *
   * <p>The dialog content and the synchronized quest log arrive independently on network clients,
   * so the dialog marks exactly the entries it displays.
   *
   * @param tab tab of the entry
   * @param owner owner of the entry
   * @param timestamp timestamp of the entry
   * @param text display text of the entry, see {@link QuestLogUI#entryText(QuestLogEntry)}
   */
  static void markRead(String tab, String owner, int timestamp, String text) {
    READ_ENTRIES.add(new ReadKey(tab, owner, timestamp, text));
  }

  /**
   * Returns the number of entries in a tab that the local player has not read yet.
   *
   * <p>Notes written by the local player never count as unread.
   *
   * @param tab tab to count
   * @return unread entry count, or 0 if the quest log was not initialized
   */
  static int unreadCount(String tab) {
    String viewerName = viewerName();
    return QuestLogUtil.getQuestLogComponent()
        .map(
            questLog ->
                (int)
                    QuestLogUI.visibleEntriesFor(questLog, tab, viewerName).stream()
                        .filter(entry -> !isOwnNote(entry, viewerName))
                        .filter(entry -> !READ_ENTRIES.contains(ReadKey.of(tab, entry)))
                        .count())
        .orElse(0);
  }

  private static int unreadCount() {
    return QuestLogUtil.getQuestLogComponent().stream()
        .flatMap(questLog -> questLog.getQuestlogTabs().stream())
        .mapToInt(QuestLogHudSystem::unreadCount)
        .sum();
  }

  private static boolean isOwnNote(QuestLogEntry entry, String viewerName) {
    return entry.userCreated() && viewerName != null && viewerName.equalsIgnoreCase(entry.owner());
  }

  private static String viewerName() {
    return Game.player()
        .flatMap(player -> player.fetch(PlayerComponent.class))
        .map(PlayerComponent::playerName)
        .orElse(null);
  }

  private record ReadKey(String tab, String owner, int timestamp, String text) {
    private static ReadKey of(String tab, QuestLogEntry entry) {
      return new ReadKey(tab, entry.owner(), entry.timestamp(), QuestLogUI.entryText(entry));
    }
  }
}
