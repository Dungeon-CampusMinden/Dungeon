package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;

/** Read-only assistant transcript for the System Recovery computer. */
public class AssistantChatTab extends SystemRecoveryComputerTab {

  public static final String KEY = "assistant";
  private static final String LOGO_PATH = "logo/cat_logo_128x128.png";

  /** Creates the assistant chat tab. */
  public AssistantChatTab() {
    super(KEY, "Chat");
    createActors();
  }

  @Override
  protected void createActors() {
    Table content = new Table(skin);
    content.top();
    content.defaults().growX();

    Texture logoTexture = TextureMap.instance().textureAt(new SimpleIPath(LOGO_PATH));
    Image logo = new Image(logoTexture);
    content.add(logo).size(96).padBottom(12).row();

    Table messages = new Table(skin);
    messages.top();
    messages.defaults().growX().padBottom(8);
    addMessage(messages, "system", "System Recovery Assistant online.", false);
    addMessage(messages, "user", "Read-only diagnostic transcript loaded.", true);
    addMessage(
        messages,
        "system",
        "I can see the terminal mount point. Waiting for recovery instructions.",
        false);
    addMessage(messages, "system", "No commands have been executed in this session yet.", false);

    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(messages, false, true);
    scrollPane.setOverscroll(false, false);
    content.add(scrollPane).grow();

    add(content).grow();
  }

  private void addMessage(Table parent, String sender, String text, boolean alignRight) {
    Table row = new Table(skin);
    row.defaults().pad(0);

    Table bubble = new Table(skin);
    bubble.setBackground(alignRight ? "blue_square_flat" : "generic-area");
    bubble.pad(12);

    Label senderLabel =
        Scene2dElementFactory.createLabel(
            sender, FontSpec.of(Scene2dElementFactory.FONT_PATH_BOLD, 16, Color.LIGHT_GRAY));
    Label textLabel = Scene2dElementFactory.createLabel(text, 20, Color.WHITE);
    textLabel.setWrap(true);

    bubble.add(senderLabel).left().row();
    bubble.add(textLabel).width(620).left();

    if (alignRight) {
      row.add().growX();
      row.add(bubble).maxWidth(720).right();
    } else {
      row.add(bubble).maxWidth(720).left();
      row.add().growX();
    }
    parent.add(row).growX().row();
  }
}
