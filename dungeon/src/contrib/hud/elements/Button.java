package contrib.hud.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import core.Game;
import core.input.MouseButtons;
import core.platform.gdx.render.TextureMap;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

/**
 * Represents a button in the GUI.
 *
 * <p>This class defines a button with a specified position (x, y) and size (width, height)
 * within the root stage, accessible via {@link Game#stage()}.
 *
 * <p>Input handling is deliberately polling-based via the engine-agnostic {@link InputManager}
 * and {@link StageHandle}. This removes the former dependency on a Scene2D {@code InputListener}
 * attached to the parent {@link CombinableGUI}.
 */
public class Button {

  private static final Texture TEXTURE_BUTTON;
  private static final Texture TEXTURE_BUTTON_HOVER;
  private static final Texture TEXTURE_BUTTON_PRESS;

  static {
    if (Game.isHeadless()) {
      TEXTURE_BUTTON = null;
      TEXTURE_BUTTON_HOVER = null;
      TEXTURE_BUTTON_PRESS = null;
    } else {
      TEXTURE_BUTTON =
        TextureMap.instance().textureAt(new SimpleIPath("hud/button/button_idle.png"));
      TEXTURE_BUTTON_HOVER =
        TextureMap.instance().textureAt(new SimpleIPath("hud/button/button_hover.png"));
      TEXTURE_BUTTON_PRESS =
        TextureMap.instance().textureAt(new SimpleIPath("hud/button/button_press.png"));
    }
  }

  /**
   * Kept for call-site compatibility and layout context.
   *
   * <p>The button no longer uses the parent for Scene2D input registration.
   */
  protected final CombinableGUI parent;

  protected int x, y, width, height;

  private boolean pressed = false;
  private boolean leftButtonDownLastFrame = false;
  private Consumer<Button> onClick = ignored -> {};

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
   * Draw the button.
   *
   * @param batch The batch to draw on
   */
  public void draw(Batch batch) {
    updateInteractionState();

    boolean hovered = isMouseOver();
    Texture textureToDraw =
      hovered
        ? (this.pressed ? TEXTURE_BUTTON_PRESS : TEXTURE_BUTTON_HOVER)
        : TEXTURE_BUTTON;

    batch.draw(textureToDraw, this.x, this.y, this.width, this.height);
  }

  private void updateInteractionState() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      this.pressed = false;
      this.leftButtonDownLastFrame = false;
      return;
    }

    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);
    boolean hovered = contains(stage.mouseX(), toHudY(stage));

    if (leftButtonDown && !leftButtonDownLastFrame && hovered) {
      this.pressed = true;
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      boolean clickReleasedOnSameButton = this.pressed && hovered;
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

  protected boolean contains(int mouseX, int mouseY) {
    return mouseX >= this.x
      && mouseX <= this.x + this.width
      && mouseY >= this.y
      && mouseY <= this.y + this.height;
  }

  protected boolean isMouseOver() {
    StageHandle stage = Game.stage().orElse(null);
    return stage != null && contains(stage.mouseX(), toHudY(stage));
  }

  private int toHudY(StageHandle stage) {
    return Math.round(stage.getHeight()) - stage.mouseY();
  }

  /**
   * Get the x position of the button in {@link Game#stage() Stage} coordinates.
   *
   * @return The x position
   */
  public int x() {
    return this.x;
  }

  /**
   * Set the x position of the button in {@link Game#stage() Stage} coordinates.
   *
   * @param x The x position
   */
  public void x(int x) {
    this.x = x;
  }

  /**
   * Get the y position of the button in {@link Game#stage() Stage} coordinates.
   *
   * @return The y position
   */
  public int y() {
    return this.y;
  }

  /**
   * Set the y position of the button in {@link Game#stage() Stage} coordinates.
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
