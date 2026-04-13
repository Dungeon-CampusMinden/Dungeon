package contrib.hud.dialogs;

import core.Game;
import core.input.Keys;
import core.input.MouseButtons;
import core.ui.overlay.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * A minimal real free-input dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class LitiengineFreeInputDialogOverlay implements LitiengineUiOverlay {

  static final String TITLE_DEFAULT = "Frage";
  static final String OK_BUTTON = "OK";
  static final String CANCEL_BUTTON = "Abbrechen";
  static final String INPUT_PLACEHOLDER_DEFAULT = "Deine Antwort…";

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

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private int pressedButtonIndex = -1;
  private boolean leftButtonDownLastFrame = false;

  LitiengineFreeInputDialogOverlay(
    String title,
    String question,
    String prefill,
    String placeholder,
    String confirmLabel,
    String cancelLabel,
    String dialogId) {
    this.title = title;
    this.question = question;
    this.placeholder = placeholder;
    this.confirmLabel = confirmLabel;
    this.cancelLabel = cancelLabel;
    this.dialogId = dialogId;
    this.inputText = new StringBuilder(prefill == null ? "" : prefill);
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    handleInput();

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      int textY = LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, title);

      int afterQuestionY =
        LitiengineDialogOverlaySupport.drawWrappedText(
          g,
          question,
          x + LitiengineDialogOverlaySupport.PADDING,
          textY,
          width - 2 * LitiengineDialogOverlaySupport.PADDING);

      drawInputField(g, afterQuestionY + 14);

      List<Rectangle> buttons = buttonBounds();
      LitiengineDialogOverlaySupport.drawButton(
        g, buttons.get(0), confirmLabel, pressedButtonIndex == 0);
      LitiengineDialogOverlaySupport.drawButton(
        g, buttons.get(1), cancelLabel, pressedButtonIndex == 1);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
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
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedButtonIndex = -1;
      leftButtonDownLastFrame = false;
      return;
    }

    // 1) typed text
    String typed = InputManager.consumeTypedCharacters();
    if (!typed.isEmpty()) {
      for (int i = 0; i < typed.length(); i++) {
        char c = typed.charAt(i);
        if (!Character.isISOControl(c)) {
          inputText.append(c);
        }
      }
    }

    // 2) control keys
    if (InputManager.isKeyJustPressed(Keys.BACKSPACE) && !inputText.isEmpty()) {
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

    // 3) mouse buttons
    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    List<Rectangle> buttons = buttonBounds();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      pressedButtonIndex = buttonIndexAt(mouseX, mouseY, buttons);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      int releasedIndex = buttonIndexAt(mouseX, mouseY, buttons);
      int previouslyPressed = pressedButtonIndex;
      pressedButtonIndex = -1;

      if (previouslyPressed >= 0 && previouslyPressed == releasedIndex) {
        if (releasedIndex == 0) {
          onSubmit();
        } else if (releasedIndex == 1) {
          onCancel();
        }
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private int buttonIndexAt(int mouseX, int mouseY, List<Rectangle> buttons) {
    for (int i = 0; i < buttons.size(); i++) {
      if (buttons.get(i).contains(mouseX, mouseY)) {
        return i;
      }
    }
    return -1;
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
    return LitiengineDialogOverlaySupport.centeredButtonRow(x, y, width, height, 2, BUTTON_GAP);
  }

  @Override
  public int x() {
    return x;
  }

  @Override
  public void x(int x) {
    this.x = x;
  }

  @Override
  public int y() {
    return y;
  }

  @Override
  public void y(int y) {
    this.y = y;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public void width(int width) {
    this.width = width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void height(int height) {
    this.height = height;
  }

  @Override
  public boolean visible() {
    return visible;
  }

  @Override
  public void visible(boolean visible) {
    this.visible = visible;
  }
}
