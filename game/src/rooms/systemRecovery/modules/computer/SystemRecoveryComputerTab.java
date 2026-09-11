package rooms.systemRecovery.modules.computer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;

/** Base class for simple System Recovery computer tabs. */
public abstract class SystemRecoveryComputerTab extends Table {

  /** All text labels in the System Recovery computer use the same high-contrast color. */
  protected static final Color LABEL_COLOR = Color.BLACK.cpy();

  private final String key;
  private final String title;
  protected final Skin skin;
  private DialogContext context;

  protected SystemRecoveryComputerTab(String key, String title) {
    this.key = key;
    this.title = title;
    this.skin = UIUtils.defaultSkin();
    top();
  }

  String key() {
    return key;
  }

  String title() {
    return title;
  }

  protected DialogContext context() {
    return context;
  }

  /** Creates a computer label with the shared black text color. */
  protected Label createLabel(String text, int fontSize) {
    return Scene2dElementFactory.createLabel(text, fontSize, LABEL_COLOR);
  }

  /** Creates a computer label with the shared black text color and a custom font. */
  protected Label createLabel(String text, engine.utils.FontSpec fontSpec) {
    return Scene2dElementFactory.createLabel(text, fontSpec.withColor(LABEL_COLOR));
  }

  /** Creates a computer button whose nested label also uses the shared black text color. */
  protected TextButton createButton(String text, String styleName, int fontSize) {
    TextButton button = Scene2dElementFactory.createButton(text, styleName, fontSize);
    Label.LabelStyle labelStyle = button.getLabel().getStyle();
    labelStyle.font =
        FontHelper.getFont(
            Scene2dElementFactory.FONT_PATH_BOLD, fontSize, LABEL_COLOR, 0, Color.WHITE);
    button.getLabel().setStyle(labelStyle);
    return button;
  }

  void context(DialogContext context) {
    this.context = context;
  }

  protected abstract void createActors();
}
