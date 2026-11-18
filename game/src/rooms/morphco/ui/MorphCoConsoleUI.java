package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Cursors;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import java.util.List;
import rooms.morphco.util.MorphCoText;

/**
 * Common frame of the Morph & Co. dialogs: a control panel around a canvas work area.
 *
 * <p>A yellow name plate and warning stripes on top, instruction and help below, the canvas in the
 * middle and a feedback display with the panel buttons at the bottom. The canvas keeps its zoom at
 * 1 so texts stay at their size; subclasses place their nodes for the available width in {@link
 * #arrange(float, float)} and the canvas scrolls vertically when they need more height than the
 * window.
 */
abstract class MorphCoConsoleUI extends CanvasUI {

  private static final float MAX_WIDTH = 1240;

  private final Table shell = new Table();
  private final Table buttons = new Table();
  private final Table display = new Table();
  private final Label feedback = MorphCoUI.label("", 18, Color.WHITE);
  private final Table canvasContent = new Table();
  private final ScrollPane scroll;
  private float arrangedWidth = -1;
  private float arrangedHeight = -1;
  private float contentHeight;

  /**
   * Creates the frame with an empty canvas.
   *
   * @param context dialog context of the server dialog
   * @param title localized title
   * @param instruction localized instruction, may be empty
   * @param help localized help line, may be empty
   */
  MorphCoConsoleUI(DialogContext context, String title, String instruction, String help) {
    super(
        context.dialogType().type(),
        new CanvasLayout(
            "",
            1,
            1,
            new CanvasOptions()
                .backgroundColor(MorphCoUI.FLOOR)
                .grid(24, true)
                .gridColor(MorphCoUI.GROOVE)
                .selectionEnabled(false)
                .multiSelectEnabled(false)
                .rubberBandEnabled(false)
                .keyboardShortcutsEnabled(false)
                .panButton(-1)
                .zoom(1, 1),
            false),
        CanvasSnapshot.empty(),
        context.dialogId(),
        List.of());
    clearChildren();
    setUserObject(Cursors.DEFAULT);
    shell.setFillParent(true);
    shell.top().pad(16, 20, 16, 20);
    shell.setBackground(MorphCoUI.fill(MorphCoUI.FRAME));

    Table plate = new Table();
    plate.setBackground(MorphCoUI.plate(MorphCoUI.SIGNAL));
    plate.pad(6, 18, 8, 18);
    plate.add(MorphCoUI.bold(title, 25, MorphCoUI.INK)).growX();
    Table header = new Table();
    header.add(plate).growX().minWidth(0).padRight(16);
    header
        .add(MorphCoUI.button(MorphCoText.text("ui.close"), false, this::requestClose))
        .minHeight(46);
    shell.add(header).growX().row();
    shell.add(new Stripes()).growX().height(8).padTop(8).padBottom(10).row();
    if (!instruction.isBlank())
      shell.add(MorphCoUI.label(instruction, 19, MorphCoUI.TEXT)).growX().padBottom(4).row();
    if (!help.isBlank())
      shell.add(MorphCoUI.label(help, 16, MorphCoUI.MUTED)).growX().padBottom(4).row();

    canvasContent.top();
    canvasContent.add(area()).growX();
    scroll = new ScrollPane(canvasContent, UIUtils.defaultSkin());
    var scrollStyle = new ScrollPane.ScrollPaneStyle(scroll.getStyle());
    scrollStyle.background = null;
    scroll.setStyle(scrollStyle);
    scroll.setScrollingDisabled(true, false);
    scroll.setFadeScrollBars(false);
    scroll.setFlickScroll(false);
    // The canvas would turn the wheel into zoom; here it scrolls the work area instead.
    scroll.addCaptureListener(
        new InputListener() {
          @Override
          public boolean scrolled(
              InputEvent event, float x, float y, float amountX, float amountY) {
            if (scroll.getMaxY() <= 0) return false;
            scroll.setScrollY(scroll.getScrollY() + amountY * 50);
            event.stop();
            return true;
          }
        });
    shell.add(scroll).grow().minSize(0).padTop(8).row();

    display.setBackground(MorphCoUI.screen());
    display.pad(10, 14, 10, 14);
    display.add(feedback).growX();
    display.setVisible(false);
    buttons.defaults().padLeft(10).minHeight(48);
    Table footer = new Table();
    footer.add(display).growX().minWidth(0);
    footer.add(buttons).right().bottom();
    shell.add(footer).growX().padTop(12);
    addActor(shell);
  }

  /**
   * Places the nodes for the available size.
   *
   * <p>World coordinates start at the top left corner of the work area and grow upwards, so nodes
   * get negative y coordinates.
   *
   * @param width usable width of the work area
   * @param height visible height of the work area
   * @return height the nodes need
   */
  protected abstract float arrange(float width, float height);

  /** Clears the feedback and places the nodes again before the next frame. */
  protected final void changed() {
    feedback("", true);
    arrangedWidth = -1;
  }

  @Override
  public void draw(Batch batch, float alpha) {
    setSize(Game.windowWidth(), Game.windowHeight());
    shell.validate();
    float available = Math.max(1, scroll.getWidth() - 18);
    float width = Math.min(MAX_WIDTH, available);
    if (width != arrangedWidth || scroll.getHeight() != arrangedHeight) {
      arrangedWidth = width;
      arrangedHeight = scroll.getHeight();
      contentHeight = arrange(width, arrangedHeight);
    }
    float height = Math.max(scroll.getHeight(), contentHeight + 4);
    if (area().getWidth() != available || area().getHeight() != height) {
      canvasContent.getCell(area()).width(available).height(height);
      canvasContent.invalidateHierarchy();
      shell.validate();
    }
    area().pan(Math.round((available - width) / 2), Math.round(height));
    super.draw(batch, alpha);
  }

  /**
   * Adds a button to the panel's button row.
   *
   * @param key text key below {@code morphCo}
   * @param primary whether this is the yellow main button
   * @param action action on click
   * @return the button
   */
  TextButton button(String key, boolean primary, Runnable action) {
    TextButton button = MorphCoUI.button(MorphCoText.text(key), primary, action);
    buttons.add(button);
    return button;
  }

  /** Removes every button from the panel's button row. */
  void clearButtons() {
    buttons.clearChildren();
  }

  /** Jumps back to the top of the work area, e.g. after turning a page. */
  void scrollToTop() {
    scroll.setScrollY(0);
    scroll.updateVisualScroll();
  }

  /**
   * Shows feedback on the display next to the buttons.
   *
   * @param text localized text, empty to clear
   * @param good whether the feedback is positive
   */
  void feedback(String text, boolean good) {
    feedback.setText(text);
    feedback.setColor(good ? MorphCoUI.GO : MorphCoUI.STOP);
    display.setVisible(!text.isEmpty());
  }

  /**
   * Sends an answer to the server.
   *
   * @param value encoded answer
   */
  void submit(String value) {
    area().fireServerEvent(MorphCoDialogs.SUBMIT, new DialogResponseMessage.StringValue(value));
  }

  /** Asks the server to close the dialog, like the room's dialogs always did. */
  @Override
  public void requestClose() {
    area().fireServerEvent(DialogContextKeys.ON_CLOSE);
  }

  // Every opening starts with the puzzle's initial layout; nothing goes to the canvas store.
  @Override
  public void flush() {}

  /** Warning stripes below the name plate. */
  private static final class Stripes extends Actor {
    @Override
    public void draw(Batch batch, float alpha) {
      MorphCoUI.hazard(batch, alpha * getColor().a, getX(), getY(), getWidth(), getHeight());
    }
  }
}
