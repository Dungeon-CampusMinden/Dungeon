package contrib.hud.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

/**
 * Represents a button in the GUI.
 *
 * <p>This class defines a button with a specified position (x, y) and size (width, height) within
 * the root stage, accessible via {@link Game#stage()}.
 *
 * <p>The button automatically registers its own {@link InputListener} on its parent's actor to
 * detect clicks.
 */
public class Button {

  private static final Texture TEXTURE_BUTTON;
  private static final Texture TEXTURE_BUTTON_HOVER;
  private static final Texture TEXTURE_BUTTON_PRESS;

  static {
    TEXTURE_BUTTON = TextureMap.instance().textureAt(new SimpleIPath("hud/button/button_idle.png"));
    TEXTURE_BUTTON_HOVER =
        TextureMap.instance().textureAt(new SimpleIPath("hud/button/button_hover.png"));
    TEXTURE_BUTTON_PRESS =
        TextureMap.instance().textureAt(new SimpleIPath("hud/button/button_press.png"));
  }

  protected final CombinableGUI parent;
  protected int x, y, width, height;
  private boolean pressed = false;
  private Consumer<Button> onClick;

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
    this.init();
  }

  // Init button by registering an input listener on the parent actor for detecting clicks.
  private void init() {
    this.parent
        .actor()
        .addListener(
            new InputListener() {
              @Override
              public boolean touchDown(
                  InputEvent event, float x, float y, int pointer, int button) {
                if (Button.this.x() <= (x + Button.this.parent.x())
                    && (Button.this.x() + Button.this.width()) >= (x + Button.this.parent.x())
                    && Button.this.y() <= (y + Button.this.parent.y())
                    && (Button.this.y() + Button.this.height()) >= (y + Button.this.parent.y())) {
                  Button.this.pressed = true;
                  Button.this.onClick.accept(Button.this);
                  return true;
                }
                return false;
              }

              @Override
              public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Button.this.pressed = false;
              }
            });
  }

  /**
   * Set the onClick consumer.
   *
   * @param onClick The consumer to be called when the button is clicked
   */
  public void onClick(Consumer<Button> onClick) {
    this.onClick = onClick;
  }

  /**
   * Draw the button.
   *
   * @param batch The batch to draw on
   */
  public void draw(Batch batch) {
    int mouseX = Gdx.input.getX();
    int mouseY = Math.round(Game.stage().orElseThrow().getHeight()) - Gdx.input.getY();
    if (mouseX >= this.x
        && mouseX <= this.x + this.width
        && mouseY >= this.y
        && mouseY <= this.y + this.height) {
      batch.draw(
          this.pressed ? TEXTURE_BUTTON_PRESS : TEXTURE_BUTTON_HOVER,
          this.x,
          this.y,
          this.width,
          this.height);
    } else {
      batch.draw(TEXTURE_BUTTON, this.x, this.y, this.width, this.height);
    }
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
