package core.platform.litiengine.dialogs;

import contrib.modules.keypad.KeypadComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.sound.SoundSpec;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

/**
 * Minimal real keypad dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class LitiengineKeypadDialogOverlay implements LitiengineUiOverlay {

  private static final String TITLE = "Keypad";

  private static final int DEFAULT_WIDTH = 420;
  private static final int DEFAULT_HEIGHT = 500;

  private static final int DISPLAY_HEIGHT = 52;
  private static final int DISPLAY_SIDE_PADDING = 28;
  private static final int DISPLAY_TOP_OFFSET = 72;

  private static final int BUTTON_SIZE = 84;
  private static final int BUTTON_GAP = 12;
  private static final int BUTTON_ROWS = 4;
  private static final int BUTTON_COLUMNS = 3;
  private static final int BUTTON_TOP_GAP = 22;

  private static final List<String> BUTTON_LABELS =
    Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "Back", "0", "Submit");

  private final Entity keypad;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private int pressedButtonIndex = -1;
  private boolean leftButtonDownLastFrame = false;

  LitiengineKeypadDialogOverlay(Entity keypad) {
    this.keypad = keypad;
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
      int contentY =
        LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, TITLE);

      drawDisplay(g, contentY);

      List<Rectangle> buttons = buttonBounds();
      for (int i = 0; i < BUTTON_LABELS.size(); i++) {
        LitiengineDialogOverlaySupport.drawButton(
          g, buttons.get(i), BUTTON_LABELS.get(i), pressedButtonIndex == i);
      }
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void drawDisplay(Graphics2D g, int contentY) {
    Rectangle display = displayBounds();

    g.setColor(new Color(20, 20, 26, 230));
    g.fillRoundRect(display.x, display.y, display.width, display.height, 10, 10);

    g.setColor(new Color(180, 180, 210));
    g.drawRoundRect(display.x, display.y, display.width, display.height, 10, 10);

    String shown = keypad.fetch(KeypadComponent.class).orElseThrow().enteredString();

    g.setColor(Color.WHITE);
    FontMetrics fm = g.getFontMetrics();
    int textX = display.x + (display.width - fm.stringWidth(shown)) / 2;
    int textY = display.y + ((display.height - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(shown, textX, textY);
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedButtonIndex = -1;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    List<Rectangle> buttons = buttonBounds();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      pressedButtonIndex = findButtonIndex(mouseX, mouseY, buttons);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      int releasedIndex = findButtonIndex(mouseX, mouseY, buttons);
      int previouslyPressed = pressedButtonIndex;
      pressedButtonIndex = -1;

      if (previouslyPressed >= 0 && previouslyPressed == releasedIndex) {
        onButtonPress(BUTTON_LABELS.get(releasedIndex));
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private int findButtonIndex(int mouseX, int mouseY, List<Rectangle> buttons) {
    for (int i = 0; i < buttons.size(); i++) {
      if (buttons.get(i).contains(mouseX, mouseY)) {
        return i;
      }
    }
    return -1;
  }

  private void onButtonPress(String action) {
    KeypadComponent kc = keypad.fetch(KeypadComponent.class).orElseThrow();

    int number = -1;
    try {
      number = Integer.parseInt(action);
      kc.addDigit(number);
    } catch (NumberFormatException ex) {
      switch (action) {
        case "Back" -> kc.backspace();
        case "Submit" -> onSubmit();
        default -> {
          // no-op
        }
      }
    }

    if (!action.equals("Submit")) {
      float pitch = 1 + (number - 5) * 0.05f;
      Game.audio().playGlobal(SoundSpec.builder("retro_beep_01").pitch(pitch));
    }
  }

  private void onSubmit() {
    KeypadComponent kc = keypad.fetch(KeypadComponent.class).orElseThrow();
    if (kc.isUnlocked()) {
      return;
    }

    kc.checkUnlock();
    if (kc.isUnlocked()) {
      keypad.fetch(DrawComponent.class).orElseThrow().sendSignal("open");
      Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    } else {
      Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong"));
    }
  }

  private Rectangle displayBounds() {
    int displayWidth = width - 2 * DISPLAY_SIDE_PADDING;
    int displayX = x + DISPLAY_SIDE_PADDING;
    int displayY = y + DISPLAY_TOP_OFFSET;
    return new Rectangle(displayX, displayY, displayWidth, DISPLAY_HEIGHT);
  }

  private List<Rectangle> buttonBounds() {
    int totalWidth = BUTTON_COLUMNS * BUTTON_SIZE + (BUTTON_COLUMNS - 1) * BUTTON_GAP;
    int startX = x + (width - totalWidth) / 2;
    int startY = displayBounds().y + DISPLAY_HEIGHT + BUTTON_TOP_GAP;

    java.util.ArrayList<Rectangle> bounds = new java.util.ArrayList<>(BUTTON_LABELS.size());
    for (int i = 0; i < BUTTON_LABELS.size(); i++) {
      int col = i % BUTTON_COLUMNS;
      int row = i / BUTTON_COLUMNS;
      bounds.add(
        new Rectangle(
          startX + col * (BUTTON_SIZE + BUTTON_GAP),
          startY + row * (BUTTON_SIZE + BUTTON_GAP),
          BUTTON_SIZE,
          BUTTON_SIZE));
    }
    return bounds;
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
