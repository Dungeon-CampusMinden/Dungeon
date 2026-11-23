package rooms.morphco.ui;

import static engine.input.Binding.key;
import static engine.input.Binding.pad;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.input.ActionId;
import engine.input.Binding.Pad;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.input.UiActions;
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
 * <p>A yellow name plate and warning stripes on top, an optional instruction below, the canvas in
 * the middle and a feedback display with the panel buttons at the bottom. The canvas keeps its zoom
 * at 1 so texts stay at their size; subclasses place their nodes for the available width in {@link
 * #arrange(float, float)} and the canvas scrolls vertically when they need more height than the
 * window.
 *
 * <p>A dialog with a help text gets a "?" button next to "Close". It opens a popup with the text
 * above the dialog, which takes the input until it is closed again. The dialog's input context must
 * take {@link #HELP} so that keyboard and pad open it as well, see {@link
 * MorphCoDialogs#register()}.
 */
abstract class MorphCoConsoleUI extends CanvasUI {

  /** Opens the help popup; closes it as well. */
  static final ActionId HELP = new ActionId("morphco", "help", key(Keys.H), pad(Pad.Y));

  private static final ActionId DISMISS =
      new ActionId("morphco", "help_dismiss", key(Keys.ENTER), key(Keys.NUMPAD_ENTER));
  private static final ContextSpec HELP_INPUT =
      UiActions.modal("morphco_help", UiActions.CONFIRM, DISMISS, HELP);
  private static final float MAX_WIDTH = 1240;

  private final Table shell = new Table();
  private final Table buttons = new Table();
  private final Table display = new Table();
  private final Label feedback = MorphCoUI.label("", 18, Color.WHITE);
  private final Table canvasContent = new Table();
  private final ScrollPane scroll;
  private final Table helpLayer = new Table();
  private final InputContext helpInput = new InputContext(HELP_INPUT);
  private float arrangedWidth = -1;
  private float arrangedHeight = -1;
  private float contentHeight;

  /**
   * Creates the frame with an empty canvas.
   *
   * @param context dialog context of the server dialog
   * @param title localized title
   * @param instruction localized instruction, may be empty
   * @param help localized text of the help popup; without one the dialog has no "?" button
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
    if (!help.isBlank())
      header.add(MorphCoUI.button("?", false, this::showHelp)).minSize(46).padRight(10);
    header
        .add(MorphCoUI.button(MorphCoText.text("ui.close"), false, this::requestClose))
        .minHeight(46);
    shell.add(header).growX().row();
    shell.add(new Stripes()).growX().height(8).padTop(8).padBottom(10).row();
    if (!instruction.isBlank())
      shell.add(MorphCoUI.label(instruction, 19, MorphCoUI.TEXT)).growX().padBottom(4).row();

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
    if (!help.isBlank()) buildHelp(help);
  }

  // The dimmed layer covers the whole dialog, so clicks and the pad cursor reach only the popup.
  private void buildHelp(String help) {
    Table panel = new Table();
    panel.setBackground(MorphCoUI.plate(MorphCoUI.TAG));
    panel.setTouchable(Touchable.enabled);
    panel.pad(18, 24, 18, 24);
    panel.add(MorphCoUI.label(help, 19, MorphCoUI.INK)).width(620).row();
    panel
        .add(MorphCoUI.button(MorphCoText.text("ui.close"), true, this::hideHelp))
        .right()
        .minHeight(46)
        .padTop(16);
    helpLayer.setFillParent(true);
    helpLayer.setBackground(MorphCoUI.fill(MorphCoUI.DIM));
    helpLayer.setTouchable(Touchable.enabled);
    helpLayer.add(panel).pad(20);
    helpLayer.setVisible(false);
    helpLayer.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (event.getTarget() == helpLayer) hideHelp();
          }
        });
    for (ActionId action : List.of(UiActions.CLOSE, UiActions.CONFIRM, DISMISS, HELP))
      helpInput.onPress(action, this::hideHelp);
    UIUtils.onUiAction(this, HELP, this::showHelp);
    addActor(helpLayer);
  }

  private void showHelp() {
    helpLayer.setVisible(true);
    GameInput.activate(helpInput);
  }

  private void hideHelp() {
    helpLayer.setVisible(false);
    GameInput.deactivate(helpInput);
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage == null) hideHelp();
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
   */
  void button(String key, boolean primary, Runnable action) {
    buttons.add(MorphCoUI.button(MorphCoText.text(key), primary, action));
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
