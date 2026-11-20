package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import engine.components.DrawComponent;
import engine.utils.Cursors;
import feature.entities.deco.Deco;

/** An image button showing a decoration preview, or a cross for no decoration. */
public class DecoButton extends ImageButton {

  private static final float IMAGE_SCALE = 0.68f;
  // The cross shown for no decoration is smaller than decoration images.
  private static final float NONE_SCALE = 0.4f;

  private final float imageSize;

  /**
   * Creates a decoration preview button.
   *
   * @param deco decoration represented by this button.
   * @param size button size.
   * @param selected whether the button is initially selected.
   */
  public DecoButton(Deco deco, float size, boolean selected) {
    this(deco, size, size * IMAGE_SCALE, selected);
  }

  /**
   * Creates a decoration preview button with a custom image size.
   *
   * @param deco decoration represented by this button, or {@code null} for no decoration.
   * @param size button size.
   * @param imageSize preview image size.
   * @param selected whether the button is initially selected.
   */
  public DecoButton(Deco deco, float size, float imageSize, boolean selected) {
    super(style(deco, selected));
    this.imageSize = imageSize;
    getImageCell().size(deco == null ? imageSize * NONE_SCALE : imageSize);
    getImage().setScaling(Scaling.fit);
    setSize(size, size);
    setUserObject(Cursors.INTERACT);
  }

  /**
   * Sets whether this button is highlighted as selected.
   *
   * @param selected whether the button should be highlighted.
   */
  public void selected(boolean selected) {
    ImageButton.ImageButtonStyle style = getStyle();
    EditorTheme.tileStyle(style, selected ? EditorTheme.SELECTION : null);
    setStyle(style);
  }

  /**
   * Changes the decoration preview shown by this button.
   *
   * @param deco decoration represented by this button, or {@code null} for no decoration.
   */
  public void setDeco(Deco deco) {
    ImageButton.ImageButtonStyle style = getStyle();
    style.imageUp = image(deco);
    setStyle(style);
    getImageCell().size(deco == null ? imageSize * NONE_SCALE : imageSize);
    invalidate();
  }

  /**
   * Adds a left-click selection callback.
   *
   * @param callback callback invoked when the button is clicked.
   */
  public void onClick(Runnable callback) {
    addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            callback.run();
          }
        });
  }

  private static ImageButton.ImageButtonStyle style(Deco deco, boolean selected) {
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    EditorTheme.tileStyle(style, selected ? EditorTheme.SELECTION : null);
    style.imageUp = image(deco);
    return style;
  }

  private static Drawable image(Deco deco) {
    if (deco == null) return EditorTheme.icon("close", EditorTheme.TEXT_DISABLED);
    DrawComponent draw = new DrawComponent(deco.path(), deco.config());
    return new TextureRegionDrawable(new TextureRegion(draw.getSprite()));
  }
}
