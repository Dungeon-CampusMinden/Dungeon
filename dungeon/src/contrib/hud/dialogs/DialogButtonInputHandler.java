package contrib.hud.dialogs;

import contrib.hud.elements.Button;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared press/release input handling for dialog buttons.
 *
 * <p>The handler keeps dialog overlays focused on layout and callback decisions while reusing the
 * same click semantics as {@link Button}: a click is triggered only when press and release happen
 * on the same button.
 */
public final class DialogButtonInputHandler {

  private final List<Button> buttons = new ArrayList<>();

  /**
   * Creates a handler for a fixed number of dialog buttons.
   *
   * @param buttonCount number of buttons to manage
   */
  public DialogButtonInputHandler(int buttonCount) {
    for (int i = 0; i < buttonCount; i++) {
      buttons.add(new Button(0, 0, 1, 1));
    }
  }

  /**
   * Sets the click callback for a button.
   *
   * @param index button index
   * @param onClick callback invoked on click
   */
  public void onClick(int index, Runnable onClick) {
    button(index).onClick(ignored -> {
      if (onClick != null) {
        onClick.run();
      }
    });
  }

  /**
   * Updates button bounds from the current dialog layout.
   *
   * @param bounds button bounds in stage coordinates
   */
  public void updateBounds(List<Rectangle> bounds) {
    for (int i = 0; i < bounds.size() && i < buttons.size(); i++) {
      Rectangle rect = bounds.get(i);
      Button button = buttons.get(i);
      button.x(rect.x);
      button.y(rect.y);
      button.width(rect.width);
      button.height(rect.height);
    }
  }

  /**
   * Updates all managed buttons from the explicit mouse state.
   *
   * @param mouseX mouse x in stage coordinates
   * @param mouseY mouse y in stage coordinates
   * @param leftButtonDown whether the primary mouse button is currently down
   */
  public void update(int mouseX, int mouseY, boolean leftButtonDown) {
    for (Button button : buttons) {
      button.update(mouseX, mouseY, leftButtonDown);
    }
  }

  /**
   * Updates all managed buttons from the current game stage mouse state.
   *
   * @return true if a stage was available and the buttons were updated, false otherwise
   */
  public boolean updateFromStage() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      resetInteractionState();
      return false;
    }

    update(stage.mouseX(), stage.mouseY(), InputManager.isButtonPressed(MouseButtons.LEFT));
    return true;
  }

  /** Resets transient interaction state for all buttons. */
  public void resetInteractionState() {
    for (Button button : buttons) {
      button.resetInteractionState();
    }
  }

  /**
   * Returns whether the indexed button is currently pressed.
   *
   * @param index button index
   * @return true if the button is pressed
   */
  public boolean isPressed(int index) {
    return button(index).isPressed();
  }

  /**
   * Returns the current bounds of the indexed button.
   *
   * @param index button index
   * @return button bounds in stage coordinates
   */
  public Rectangle bounds(int index) {
    Button button = button(index);
    return new Rectangle(button.x(), button.y(), button.width(), button.height());
  }

  private Button button(int index) {
    return buttons.get(index);
  }
}
