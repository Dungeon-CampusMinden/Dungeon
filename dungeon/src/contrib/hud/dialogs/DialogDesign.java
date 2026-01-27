package contrib.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.utils.FontHelper;

/** Some basic layout for Dialogs. */
public class DialogDesign extends VerticalGroup {

  /** Creates a Left aligned {@link VerticalGroup} which completely fills the Parent UI Element. */
  public DialogDesign() {
    super();
    setFillParent(true);
    left();
  }

  /**
   * Simple Helper with default ScrollPane Configuration.
   *
   * @param skin how the ScrollPane should look like
   * @param container a container which should be scrollable
   * @return the ScrollPane which then can be added to any UI Element
   */
  public static ScrollPane createScrollPane(final Skin skin, final Actor container) {
    ScrollPane scrollPane = new ScrollPane(container, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollbarsVisible(true);
    return scrollPane;
  }

  /**
   * Simple default Textarea with default Text.
   *
   * @param skin how the ScrollPane should look like
   * @return the TextArea which then can be added to any UI Element
   */
  public static TextArea createEditableText(final Skin skin) {
    return new TextArea("Click here...", skin);
  }

  /**
   * Creates a simple Dialog which only has static Text shown.
   *
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @param outputMsg Content displayed in the scrollable label
   * @return WTF? .
   */
  public static Group createTextDialog(final Skin skin, String outputMsg) {
    outputMsg = UIUtils.formatString(outputMsg);
    Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get("blank-black", Label.LabelStyle.class));
    labelStyle.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 17, Color.BLACK, 0);
    labelStyle.fontColor = Color.BLACK;
    Label label = new Label(outputMsg, labelStyle);
    return createScrollPane(skin, new Container<>(label).align(Align.center));
  }
}
