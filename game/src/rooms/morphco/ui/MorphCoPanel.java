package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.elements.RichLabel;
import rooms.morphco.util.MorphCoText;

/**
 * Common frame of the Morph & Co. dialogs: a centered panel with title, instruction, scrollable
 * work area, feedback line and button row.
 */
abstract class MorphCoPanel extends Group {

  /** Dark text on the light panel. */
  static final Color INK = new Color(0.12f, 0.12f, 0.16f, 1f);

  /** Muted text for help lines. */
  static final Color MUTED = new Color(0.35f, 0.35f, 0.4f, 1f);

  /** Green feedback. */
  static final Color GOOD = new Color(0.1f, 0.5f, 0.2f, 1f);

  /** Red feedback. */
  static final Color BAD = new Color(0.65f, 0.15f, 0.1f, 1f);

  protected final DialogContext context;
  protected final Skin skin;
  protected final Table body;
  protected final Table buttons;
  private final Table root;
  private final Table panel;
  private final RichLabel feedback;

  /**
   * Creates the frame.
   *
   * @param context dialog context of the server dialog
   * @param title localized title
   * @param instruction localized instruction, may be empty
   * @param help localized help line, may be empty
   */
  MorphCoPanel(DialogContext context, String title, String instruction, String help) {
    this.context = context;
    this.skin = UIUtils.defaultSkin();
    setSize(Game.windowWidth(), Game.windowHeight());
    setTouchable(Touchable.enabled);

    root = new Table();
    root.setFillParent(true);
    addActor(root);

    panel = new Table(skin);
    panel.setBackground("generic-area-depth");
    panel.pad(18);
    panel.setTouchable(Touchable.enabled);

    Table header = new Table();
    header.add(Scene2dElementFactory.createLabel(title, 30, INK)).expandX().left();
    Button exit = Scene2dElementFactory.createExitButton();
    exit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            close();
          }
        });
    header.add(exit).size(40).right();
    panel.add(header).growX().row();
    panel.add(new Image(skin, "divider")).growX().height(5).padTop(4).padBottom(8).row();

    if (!instruction.isBlank()) {
      RichLabel text = text(instruction, 20);
      panel.add(text).growX().padBottom(4).row();
    }
    if (!help.isBlank()) {
      RichLabel helpLabel = new RichLabel(help, 16, MUTED);
      helpLabel.setWrap(true);
      helpLabel.setMaxPrefWidth(contentWidth());
      panel.add(helpLabel).growX().padBottom(8).row();
    }

    body = new Table();
    body.top().left();
    ScrollPane scroll = Scene2dElementFactory.createScrollPane(body, false, true);
    // Dragging a chip must not pan the work area.
    scroll.setFlickScroll(false);
    scroll.setFadeScrollBars(false);
    panel.add(scroll).grow().row();

    feedback = text("", 20);
    panel.add(feedback).growX().padTop(8).row();

    buttons = new Table();
    buttons.defaults().padLeft(10).height(48);
    panel.add(buttons).right().padTop(6);

    root.add(panel).width(panelWidth()).height(panelHeight());
  }

  private static float panelWidth() {
    return Math.max(320f, Math.min(1180f, Game.windowWidth() - 40f));
  }

  /**
   * Returns the usable width inside the panel.
   *
   * @return width of the work area
   */
  static float contentWidth() {
    return panelWidth() - 60f;
  }

  private static float panelHeight() {
    return Math.max(320f, Math.min(820f, Game.windowHeight() - 40f));
  }

  @Override
  public void act(float delta) {
    if (getWidth() != Game.windowWidth() || getHeight() != Game.windowHeight()) {
      setSize(Game.windowWidth(), Game.windowHeight());
      root.getCell(panel).width(panelWidth()).height(panelHeight());
      root.invalidateHierarchy();
    }
    super.act(delta);
  }

  /**
   * Adds a button to the button row.
   *
   * @param key text key below {@code morphCo}
   * @param style skin style of the button
   * @param action action on click
   * @return the button
   */
  TextButton button(String key, String style, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(MorphCoText.text(key), style, 22);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    buttons.add(button);
    return button;
  }

  /**
   * Shows feedback below the work area.
   *
   * @param text localized text, empty to clear
   * @param good whether the feedback is positive
   */
  void feedback(String text, boolean good) {
    feedback.setText(text.isEmpty() ? "" : "[color=" + (good ? "#1a8033" : "#a6261a") + "]" + text);
  }

  /** Clears the feedback line. */
  void clearFeedback() {
    feedback("", true);
  }

  /**
   * Sends an answer to the server.
   *
   * @param value encoded answer
   */
  void submit(String value) {
    DialogCallbackResolver.createButtonCallback(context.dialogId(), MorphCoDialogs.SUBMIT)
        .accept(new DialogResponseMessage.StringValue(value));
  }

  /** Closes the dialog through the server. */
  void close() {
    DialogCallbackResolver.createButtonCallback(context.dialogId(), DialogContextKeys.ON_CLOSE)
        .accept(null);
  }

  /**
   * Creates a dark label.
   *
   * @param text localized text
   * @param size font size
   * @return the label
   */
  static RichLabel text(String text, int size) {
    return text(text, size, contentWidth());
  }

  /**
   * Creates a dark label that wraps at a given width.
   *
   * @param text localized text
   * @param size font size
   * @param width maximum width before the text wraps
   * @return the label
   */
  static RichLabel text(String text, int size, float width) {
    RichLabel label = new RichLabel(text, size, INK);
    label.setWrap(true);
    label.setMaxPrefWidth(width);
    return label;
  }
}
