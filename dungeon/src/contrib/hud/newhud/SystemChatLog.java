package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * The SystemChatLog is a HUD element that displays in-game messages and system notifications to the
 * player in a scrollable log.
 *
 * <p>This element is largely prepared and can be fully used as soon as a source of messages becomes
 * available.
 */
public class SystemChatLog extends Table implements HUDElement {

  private final Table messagesTable;
  private final ScrollPane scrollPane;

  private static final int MAX_MESSAGES = 25;

  /**
   * Creates a new SystemChatLog.
   *
   * @param skin The skin that defines the appearance of UI elements.
   */
  public SystemChatLog(Skin skin) {
    super(skin);

    pad(5);

    messagesTable = new Table();
    messagesTable.top().left();

    scrollPane = new ScrollPane(messagesTable, skin, "thin-transparent");
    scrollPane.setScrollingDisabled(true, false);

    ScrollPane.ScrollPaneStyle style = scrollPane.getStyle();
    style.vScroll.setMinWidth(5);
    style.vScrollKnob.setMinWidth(5);

    add(scrollPane).width(400).height(120).expand().fill();
  }

  @Override
  public void init() {
    layoutElement();
    // nur für demonstrative zwecke
    addMessage("You enter a new room.", "whitemessage");
    addMessage("You found a health potion.", "whitemessage");
    addMessage(
        "You can use Items from the first two slots of your Inventory via the quick access with 1 and 2.",
        "yellowmessage");
    addMessage("You picked up an iron sword.", "whitemessage");
    addMessage("You hit the Goblin for 6 damage!", "redmessage");
    addMessage("You feel stronger.", "greenmessage");
    addMessage("Warning: Low stamina!", "yellowmessage");
  }

  @Override
  public void layoutElement() {
    setPosition(10, 10); // unten links
    pack();
  }

  @Override
  public void update() {
    /*
    Möglicher update ablauf, sobald z.B. ein SystemChatLogComponent existiert:

    Game.player().ifPresent(player -> {

      player.fetch(SystemChatLogComponent.class).ifPresent(logs -> {
          Map<String, style> newMessages = logs.getNewMessages();

          for (var message : newMessages.entrySet()) {
            addMessage(message.getKey(), message.getValue());
            logs.markMessagesAsOld();
          }
      });
    });
     */
  }

  private void addMessage(String text, String style) {
    Label label = new Label(text, getSkin(), style);
    label.setFontScale(0.5f);
    label.setWrap(true);

    messagesTable.row();
    messagesTable.add(label).left().expandX().fillX().padBottom(2);

    // alte Nachrichten löschen
    if (messagesTable.getChildren().size > MAX_MESSAGES) {
      messagesTable.getChildren().removeIndex(0);
    }

    // automatisches Scrollen zum Ende
    Gdx.app.postRunnable(() -> scrollPane.setScrollPercentY(1f));
  }
}
