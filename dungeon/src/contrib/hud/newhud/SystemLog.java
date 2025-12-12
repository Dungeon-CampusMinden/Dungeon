package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class SystemLog extends Table implements HUDElement {

  private final Table messagesTable;
  private final ScrollPane scrollPane;

  private static final int MAX_MESSAGES = 25;

  public SystemLog(Skin skin) {
    super(skin);

    // setBackground(skin.getDrawable("dark-gray"));
    pad(5);

    messagesTable = new Table();
    messagesTable.top().left();

    scrollPane = new ScrollPane(messagesTable, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollingDisabled(true, false);

    scrollPane.getStyle().background = null;

    add(scrollPane).width(400).height(120).expand().fill();
  }

  @Override
  public void init() {
    layoutElement();
    addMessage("You picked up an iron sword.", "whitemessage");
    addMessage("You hit the Goblin for 6 damage!", "redmessage");
    addMessage("You feel stronger.", "greenmessage");
    addMessage("Warning: Low stamina!", "yellowmessage");
  }

  @Override
  public void layoutElement() {
    setPosition(20, 20); // unten links
    pack();
  }

  @Override
  public void update() {}

  public void addMessage(String text, String style) {
    Label label = new Label(text, getSkin(), style);
    label.setFontScale(0.5f);

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
