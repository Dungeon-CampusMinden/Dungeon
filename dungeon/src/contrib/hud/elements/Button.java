package contrib.hud.elements;

import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

/**
 * Represents a UI button that can handle interactions such as hover, click, and press events.
 *
 * <p>The button supports backend-neutral input handling and provides functionality to update
 * its state, retrieve its visual state, and react to user interactions via a customizable
 * consumer callback.
 */
public class Button {

  private static final IPath BUTTON_IDLE_PATH = new SimpleIPath("hud/button/button_idle.png");
  private static final IPath BUTTON_HOVER_PATH = new SimpleIPath("hud/button/button_hover.png");
  private static final IPath BUTTON_PRESS_PATH = new SimpleIPath("hud/button/button_press.png");

  protected int x, y, width, height;

  private boolean pressed = false;
  private boolean hovered = false;
  private boolean leftButtonDownLastFrame = false;
  private Consumer<Button> onClick = ignored -> {};

  /**
   * Represents the visual state of a UI element, such as a button.
   *
   * <ul>
   *   <li>IDLE: The default state when no interaction is occurring.
   *   <li>HOVER: The state when the cursor is hovering over the element.
   *   <li>PRESSED: The state when the element is actively being interacted with, such as when a mouse button is pressed.
   * </ul>
   */
  public enum VisualState {
    IDLE,
    HOVER,
    PRESSED
  }

  /**
   * Create a new button.
   *
   * @param x The x position in global stage coordinates
   * @param y The y position in global stage coordinates
   * @param width The width of the button
   * @param height The height of the button
   */
  public Button(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Set the onClick consumer.
   *
   * @param onClick The consumer to be called when the button is clicked
   */
  public void onClick(Consumer<Button> onClick) {
    this.onClick = onClick != null ? onClick : ignored -> {};
  }

  /**
   * Updates the button from the current engine-neutral stage/input abstractions.
   *
   * <p>This keeps the existing GDX call sites working while also allowing other backends to drive
   * the same state machine explicitly via {@link #update(int, int, boolean)}.
   */
  public void updateFromStage() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      resetInteractionState();
      return;
    }

    update(
      stage.mouseX(),
      Math.round(stage.getHeight()) - stage.mouseY(),
      InputManager.isButtonPressed(MouseButtons.LEFT));
  }

  /**
   * Updates the button state from explicit mouse data.
   *
   * <p>This method is backend-neutral and can be used by non-libGDX render/input paths such as
   * LITIENGINE overlays.
   *
   * @param mouseX mouse x in HUD/screen coordinates
   * @param mouseY mouse y in HUD/screen coordinates
   * @param leftButtonDown whether the primary mouse button is currently down
   */
  public void update(int mouseX, int mouseY, boolean leftButtonDown) {
    this.hovered = contains(mouseX, mouseY);

    if (leftButtonDown && !leftButtonDownLastFrame && this.hovered) {
      this.pressed = true;
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      boolean clickReleasedOnSameButton = this.pressed && this.hovered;
      this.pressed = false;

      if (clickReleasedOnSameButton) {
        this.onClick.accept(this);
      }
    }

    if (!leftButtonDown && !leftButtonDownLastFrame) {
      this.pressed = false;
    }

    this.leftButtonDownLastFrame = leftButtonDown;
  }

  /** Resets the transient interaction state. */
  public void resetInteractionState() {
    this.pressed = false;
    this.hovered = false;
    this.leftButtonDownLastFrame = false;
  }

  /**
   * Returns the current backend-neutral visual state.
   *
   * <p>The pressed background is only shown while the cursor is still over the button, preserving
   * the previous visual behaviour.
   *
   * @return current visual state
   */
  public VisualState visualState() {
    if (!this.hovered) {
      return VisualState.IDLE;
    }

    return this.pressed ? VisualState.PRESSED : VisualState.HOVER;
  }

  /**
   * Returns the background asset path for the current visual state.
   *
   * <p>This can be consumed by any backend-specific renderer.
   *
   * @return button background asset path
   */
  public IPath backgroundTexturePath() {
    return switch (visualState()) {
      case PRESSED -> BUTTON_PRESS_PATH;
      case HOVER -> BUTTON_HOVER_PATH;
      case IDLE -> BUTTON_IDLE_PATH;
    };
  }

  protected boolean contains(int mouseX, int mouseY) {
    return mouseX >= this.x
      && mouseX <= this.x + this.width
      && mouseY >= this.y
      && mouseY <= this.y + this.height;
  }

  /**
   * @return true if the button is currently hovered according to the last update cycle
   */
  protected boolean isMouseOver() {
    return this.hovered;
  }

  /**
   * @return true if a press started on this button and has not yet been released
   */
  protected boolean isPressed() {
    return this.pressed;
  }

  /**
   * Get the x position of the button in stage coordinates.
   *
   * @return The x position
   */
  public int x() {
    return this.x;
  }

  /**
   * Set the x position of the button in stage coordinates.
   *
   * @param x The x position
   */
  public void x(int x) {
    this.x = x;
  }

  /**
   * Get the y position of the button in stage coordinates.
   *
   * @return The y position
   */
  public int y() {
    return this.y;
  }

  /**
   * Set the y position of the button in stage coordinates.
   *
   * @param y The y position
   */
  public void y(int y) {
    this.y = y;
  }

  /**
   * Get the width of the button.
   *
   * @return The width
   */
  public int width() {
    return this.width;
  }

  /**
   * Set the width of the button.
   *
   * @param width The width
   */
  public void width(int width) {
    this.width = width;
  }

  /**
   * Get the height of the button.
   *
   * @return The height
   */
  public int height() {
    return this.height;
  }

  /**
   * Set the height of the button.
   *
   * @param height The height
   */
  public void height(int height) {
    this.height = height;
  }
}
