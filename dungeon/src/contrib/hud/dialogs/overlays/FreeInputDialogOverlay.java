package contrib.hud.dialogs.overlays;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.input.DialogButtonInputHandler;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.frame.DialogFrameMetrics;
import contrib.hud.dialogs.frame.DialogFrameRenderer;
import core.input.Keys;
import core.ui.overlay.BaseUiOverlay;
import core.utils.InputManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * A class representing a free input dialog overlay.
 *
 * <p>It displays a dialog with customizable title, question, placeholder text, and button labels,
 * allowing the user to input text and interact through confirm or cancel actions.
 *
 * <p>The dialog is drawn in a graphical context and responds to user input such as typing,
 * clicking buttons, or pressing control keys.
 *
 * <p>This class is intended to be used as part of a user interface overlay system
 * and interacts with game stages and input management.
 *
 * <p>Features:
 * <ul>
 *    <li>Renders a dialog frame with a title, question, and input field.</li>
 *    <li>Allows text input with optional placeholder text.</li>
 *    <li>Supports customizable button labels and button actions.</li>
 *    <li>Handles user interactions with keyboard and mouse inputs.</li>
 * </ul>
 */
public final class FreeInputDialogOverlay extends BaseUiOverlay {

  /** Default dialog title. */
  public static final String TITLE_DEFAULT = "Frage";

  /** Default confirm button label. */
  public static final String OK_BUTTON = "OK";

  /** Default cancel button label. */
  public static final String CANCEL_BUTTON = "Abbrechen";

  /** Default placeholder shown when the input is empty. */
  public static final String INPUT_PLACEHOLDER_DEFAULT = "Deine Antwort…";

  private static final int DEFAULT_WIDTH = 620;
  private static final int DEFAULT_HEIGHT = 300;
  private static final int BUTTON_GAP = 16;
  private static final int INPUT_HEIGHT = 38;
  private static final int INPUT_SIDE_PADDING = 24;
  private static final int INPUT_BOTTOM_GAP = 70;

  private final String title;
  private final String question;
  private final String placeholder;
  private final String confirmLabel;
  private final String cancelLabel;
  private final String dialogId;

  private final StringBuilder inputText;
  private final DialogButtonInputHandler buttonInput = new DialogButtonInputHandler(2);

  /**
   * Creates a free input dialog overlay.
   *
   * @param title dialog title
   * @param question question shown above the input field
   * @param prefill initial input value
   * @param placeholder placeholder shown when the input is empty
   * @param confirmLabel confirm button label
   * @param cancelLabel cancel button label
   * @param dialogId id used to resolve callbacks
   */
  public FreeInputDialogOverlay(
    String title,
    String question,
    String prefill,
    String placeholder,
    String confirmLabel,
    String cancelLabel,
    String dialogId) {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.title = title;
    this.question = question;
    this.placeholder = placeholder;
    this.confirmLabel = confirmLabel;
    this.cancelLabel = cancelLabel;
    this.dialogId = dialogId;
    this.inputText = new StringBuilder(prefill == null ? "" : prefill);
    this.buttonInput.onClick(0, this::onSubmit);
    this.buttonInput.onClick(1, this::onCancel);
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    List<Rectangle> buttons = buttonBounds();
    buttonInput.updateBounds(buttons);
    handleInput();

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      int textY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, title);

      int afterQuestionY =
        DialogFrameRenderer.drawWrappedText(
          g,
          question,
          x + DialogFrameMetrics.PADDING,
          textY,
          width - 2 * DialogFrameMetrics.PADDING);

      drawInputField(g, afterQuestionY + 14);

      DialogFrameRenderer.drawButton(
        g, buttons.get(0), confirmLabel, buttonInput.isPressed(0));
      DialogFrameRenderer.drawButton(
        g, buttons.get(1), cancelLabel, buttonInput.isPressed(1));
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void drawInputField(Graphics2D g, int preferredY) {
    Rectangle input = inputBounds(preferredY);

    g.setColor(new Color(20, 20, 26, 230));
    g.fillRoundRect(input.x, input.y, input.width, input.height, 10, 10);

    g.setColor(new Color(180, 180, 210));
    g.drawRoundRect(input.x, input.y, input.width, input.height, 10, 10);

    String shownText = inputText.toString();
    boolean showPlaceholder = shownText.isEmpty();

    if (showPlaceholder) {
      g.setColor(new Color(170, 170, 170));
      shownText = placeholder;
    } else {
      g.setColor(Color.WHITE);
      shownText = shownText + "_";
    }

    int textX = input.x + 12;
    int textY = input.y + ((input.height - g.getFontMetrics().getHeight()) / 2) + g.getFontMetrics().getAscent();
    g.drawString(shownText, textX, textY);
  }

  private void handleInput() {
    // typed text
    String typed = InputManager.consumeTypedCharacters();
    if (!typed.isEmpty()) {
      for (int i = 0; i < typed.length(); i++) {
        char c = typed.charAt(i);

        if (c == '\b') {
          if (!inputText.isEmpty()) {
            inputText.deleteCharAt(inputText.length() - 1);
          }
          continue;
        }

        if (c == 127) {
          if (!inputText.isEmpty()) {
            inputText.deleteCharAt(inputText.length() - 1);
          }
          continue;
        }

        if (!Character.isISOControl(c)) {
          inputText.append(c);
        }
      }
    }

    // control keys
    if (InputManager.isKeyJustPressed(Keys.BACKSPACE) && !inputText.isEmpty()) {
      inputText.deleteCharAt(inputText.length() - 1);
    }

    if (InputManager.isKeyJustPressed(Keys.DELETE) && !inputText.isEmpty()) {
      inputText.deleteCharAt(inputText.length() - 1);
    }

    if (InputManager.isKeyJustPressed(Keys.ENTER)) {
      onSubmit();
      return;
    }

    if (InputManager.isKeyJustPressed(Keys.ESCAPE)) {
      onCancel();
      return;
    }

    buttonInput.updateFromStage();
  }

  private void onSubmit() {
    DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.INPUT_CALLBACK)
      .accept(inputText.toString());
  }

  private void onCancel() {
    DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CANCEL)
      .accept(null);
  }

  private Rectangle inputBounds(int preferredY) {
    int inputWidth = width - 2 * INPUT_SIDE_PADDING;
    int inputX = x + INPUT_SIDE_PADDING;
    int maxY = y + height - INPUT_BOTTOM_GAP - INPUT_HEIGHT;
    int inputY = Math.min(preferredY, maxY);
    return new Rectangle(inputX, inputY, inputWidth, INPUT_HEIGHT);
  }

  private List<Rectangle> buttonBounds() {
    return DialogFrameRenderer.centeredButtonRow(x, y, width, height, 2, BUTTON_GAP);
  }
}
