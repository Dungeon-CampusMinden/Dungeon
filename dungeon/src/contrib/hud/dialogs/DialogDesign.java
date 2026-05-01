package contrib.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import core.utils.FontHelper;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;

/** Some basic layout for Dialogs. */
public class DialogDesign {

  private static final String DIALOG_FONT = "fonts/Roboto-SemiBold.ttf";

  /** The default font specification for dialog text. */
  public static final FontSpec DIALOG_FONT_SPEC_NORMAL = FontSpec.of(DIALOG_FONT, 20, Color.BLACK);

  /** The font specification for dialog titles. */
  public static final FontSpec DIALOG_FONT_SPEC_TITLE = FontSpec.of(DIALOG_FONT, 36);

  /**
   * Simple Helper with default ScrollPane Configuration.
   *
   * @param skin how the ScrollPane should look like
   * @param actor a container which should be scrollable
   * @return the ScrollPane which then can be added to any UI Element
   */
  public static ScrollPane createScrollPane(Skin skin, Actor actor) {
    ScrollPane scrollPane = new ScrollPane(actor, skin);
    scrollPane.setFadeScrollBars(true);
    scrollPane.setScrollbarsOnTop(true);
    scrollPane.setScrollbarsVisible(true);
    scrollPane.addListener(
        new InputListener() {
          @Override
          public boolean mouseMoved(InputEvent event, float x, float y) {
            event.getStage().setScrollFocus(scrollPane);
            return false;
          }

          // Prevent scrolling when outside the scroll pane
          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (toActor != null && toActor.isDescendantOf(scrollPane)) return;
            if (event.getStage().getScrollFocus() != scrollPane) return;

            event.getStage().setScrollFocus(null);
          }

          // Rebuilding the UI will cause an exit->enter, so we need to set the scroll focus back
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (event.getStage() == null) return;
            event.getStage().setScrollFocus(scrollPane);
          }
        });
    return scrollPane;
  }

  /**
   * Creates a simple Dialog which only has static Text shown.
   *
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @param outputMsg Content displayed in the scrollable label
   * @return WTF? .
   */
  public static Group createTextDialog(final Skin skin, String outputMsg) {
    Label.LabelStyle labelStyle =
        new Label.LabelStyle(skin.get("blank-black", Label.LabelStyle.class));
    labelStyle.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 17, Color.BLACK, 0);
    labelStyle.fontColor = Color.BLACK;
    Label label = new Label(outputMsg, labelStyle);
    label.setWrap(true);
    label.setAlignment(Align.topLeft);

    Table table = new Table();
    table.top().left();
    table.add(label).growX().top().left();

    ScrollPane pane = createScrollPane(skin, table);
    pane.setScrollbarsOnTop(false);
    return pane;
  }

  /**
   * Adds a title to the given parent table with consistent styling and spacing.
   *
   * @param parent the Table to which the title should be added
   * @param title the text of the title to display
   */
  public static void addTitleTable(Table parent, String title) {
    Table titleTable = new Table();
    titleTable
        .add(Scene2dElementFactory.createLabel(title, DialogDesign.DIALOG_FONT_SPEC_TITLE))
        .center()
        .row();
    parent.add(titleTable).growX().height(48).padBottom(10).row();
  }

  /**
   * Configures a Dialog with default button spacing, content padding, and an optional title. This
   * method removes the default title table and applies consistent styling to the dialog's content
   * and buttons.
   *
   * @param dialog the Dialog to configure with default layout settings
   * @param title the text of the title to display at the top of the dialog, or an empty string for
   *     no title
   */
  public static void setDialogDefaults(Dialog dialog, String title) {
    dialog.getButtonTable().defaults().minWidth(150).space(10);

    dialog.getTitleTable().remove();
    Table content = dialog.getContentTable();
    content.pad(0, 10, 0, 10);

    if (!title.isBlank()) {
      DialogDesign.addTitleTable(content, title);
    }
  }
}
