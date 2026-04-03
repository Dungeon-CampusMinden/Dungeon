package contrib.hud.elements;

import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

/**
 * Represents a button with backend-neutral interaction state.
 *
 * <p>The button owns its bounds, click semantics and visual state calculation. Concrete backends
 * can render the button using {@link #backgroundTexturePath()}.
 */
public class Button {

  private static final IPath BUTTON_IDLE_PATH = new SimpleIPath("hud/button/button_idle.png");
  private static final IPath BUTTON_HOVER_PATH = new SimpleIPath("hud/button/button_hover.png");
  private static final IPath BUTTON_PRESS_PATH = new SimpleIPath("hud/button/button_press.png");

  /**
   * Kept for call-site compatibility and layout context.
   *
   * <p>The button no longer uses the parent for backend-specific rendering.
   */
  protected final CombinableGUI parent;

  protected int x, y, width, height;

  private boolean pressed = false;
  private boolean hovered = false;
  private boolean leftButtonDownLastFrame = false;
  private Consumer<Button> onClick = ignored -> {};

  /** Visual state of the button background. */
  public enum VisualState {
    IDLE,
    HOVER,
    PRESSED
  }

  /**
   * Create a new button.
   *
   * @param parent The parent gui
   * @param x The x position in global stage coordinates
   * @param y The y position in global stage coordinates
   * @param width The width of the button
   * @param height The height of the button
   */
  public Button(final CombinableGUI parent, int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.parent = parent;
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
   * <p>This keeps existing call sites simple while also allowing non-libGDX backends to drive the
   * same state machine via {@link #update(int, int, boolean)}.
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
