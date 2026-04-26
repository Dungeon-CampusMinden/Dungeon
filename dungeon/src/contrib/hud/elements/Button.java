package contrib.hud.elements;

import java.util.function.Consumer;

/**
 * Represents a UI button that can handle interactions such as hover, click, and press events.
 *
 * <p>The button supports backend-neutral input handling and provides functionality to update its
 * state, retrieve its visual state, and react to user interactions via a customizable consumer
 * callback.
 */
public class Button {

  protected int x, y, width, height;

  private boolean pressed = false;
  private boolean hovered = false;
  private boolean leftButtonDownLastFrame = false;
  private Consumer<Button> onClick = ignored -> {};

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
   * Returns whether this button is currently pressed.
   *
   * @return true if the button was pressed and has not been released yet
   */
  public boolean isPressed() {
    return this.pressed;
  }

  protected boolean contains(int mouseX, int mouseY) {
    return mouseX >= this.x
        && mouseX <= this.x + this.width
        && mouseY >= this.y
        && mouseY <= this.y + this.height;
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
