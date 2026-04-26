package contrib.modules.keypad.ui;

import contrib.hud.dialogs.input.DialogButtonInputHandler;
import contrib.hud.frame.DialogFrameRenderer;
import contrib.modules.keypad.KeypadComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.sound.SoundSpec;
import core.ui.overlay.BaseUiOverlay;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

/**
 * A UI overlay that renders a keypad dialog interface.
 *
 * <p>This overlay displays
 * a set of buttons and a small display area, allowing user interaction for
 * entering digits, submitting input, and triggering associated actions.
 *
 * <p>This class handles input management, rendering operations, and button press
 * events. It is designed to integrate with an entity that includes a
 * {@code KeypadComponent} for managing user interaction and input logic.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Render keypad buttons and the display area using predefined layout settings.</li>
 *   <li>Handle mouse events to detect button presses and trigger corresponding actions.</li>
 *   <li>Provide methods to set and retrieve the position, dimensions, and visibility of the overlay.</li>
 * </ul>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>{@code Entity}: Represents the keypad entity containing the logic for input actions.</li>
 *   <li>{@code DialogFrameRenderer}: Utility responsible for rendering frame and button visuals.</li>
 *   <li>{@code InputManager}: Input handling utility for detecting mouse interactions.</li>
 *   <li>{@code KeypadComponent}: Component of the keypad entity responsible for managing entered digits,
 *   backspace functionality, and input validation.</li>
 *   <li>{@code StageHandle}: Represents the game stage for retrieving mouse coordinates.</li>
 *   <li>{@code Game.audio()}: External audio utility for playing feedback sounds.</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>The class is immutable in terms of its core behavior, though position, dimensions,
 *   and visibility can be modified at runtime.</li>
 *   <li>The keypad buttons are hardcoded with a specific layout and labels.</li>
 * </ul>
 */
public final class KeypadDialogOverlay extends BaseUiOverlay {
  private static final int DEFAULT_WIDTH = 420;
  private static final int DEFAULT_HEIGHT = 500;

  private static final int DISPLAY_HEIGHT = 52;
  private static final int DISPLAY_SIDE_PADDING = 28;
  private static final int DISPLAY_TOP_OFFSET = 72;

  private static final int BUTTON_SIZE = 84;
  private static final int BUTTON_GAP = 12;
  private static final int BUTTON_COLUMNS = 3;
  private static final int BUTTON_TOP_GAP = 22;

  private static final List<String> BUTTON_LABELS =
    Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "Back", "0", "Submit");

  private final Entity keypad;
  private final DialogButtonInputHandler buttonInput =
    new DialogButtonInputHandler(BUTTON_LABELS.size());

  public KeypadDialogOverlay(Entity keypad) {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.keypad = keypad;

    for (int i = 0; i < BUTTON_LABELS.size(); i++) {
      String label = BUTTON_LABELS.get(i);
      this.buttonInput.onClick(i, () -> onButtonPress(label));
    }
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
      drawDisplay(g);

      for (int i = 0; i < BUTTON_LABELS.size(); i++) {
        DialogFrameRenderer.drawButton(
          g, buttons.get(i), BUTTON_LABELS.get(i), buttonInput.isPressed(i));
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void drawDisplay(Graphics2D g) {
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
    buttonInput.updateFromStage();
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
}
