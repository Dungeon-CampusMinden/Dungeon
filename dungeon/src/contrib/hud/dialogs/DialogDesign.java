package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

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
   * @param align Alignment of the text
   */
  public static Group createTextDialog(final Skin skin, final String outputMsg, int align) {
    return createScrollPane(skin, new Container<>(new Label(outputMsg, skin)).align(align));
  }

  /**
   * Creates a simple Dialog which only has static Text shown.
   *
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @param outputMsg Content displayed in the scrollable label
   * @return the Group which then can be added to any UI Element
   */
  public static Group createTextDialog(final Skin skin, final String outputMsg) {
    return createTextDialog(skin, outputMsg, Align.center);
  }
}
