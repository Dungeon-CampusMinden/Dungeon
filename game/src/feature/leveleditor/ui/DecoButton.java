package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import engine.components.DrawComponent;
import feature.entities.deco.Deco;

/** An image button showing a decoration preview. */
public class DecoButton extends ImageButton {

  private static final String STYLE_DEFAULT = "default";
  private static final String STYLE_SELECTED = "blue-outline";
  private static final float IMAGE_SCALE = 0.68f;

  private Deco deco;
  private final String selectedStyle;

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
   * @param deco decoration represented by this button.
   * @param size button size.
   * @param imageSize preview image size.
   * @param selected whether the button is initially selected.
   */
  public DecoButton(Deco deco, float size, float imageSize, boolean selected) {
    super(style(deco, selected ? STYLE_SELECTED : STYLE_DEFAULT));
    this.deco = deco;
    this.selectedStyle = STYLE_SELECTED;
    getImageCell().size(imageSize);
    getImage().setScaling(Scaling.fit);
    setSize(size, size);
  }

  /**
   * Sets whether this button is highlighted as selected.
   *
   * @param selected whether the button should be highlighted.
   */
  public void selected(boolean selected) {
    applyBackground(selected ? selectedStyle : STYLE_DEFAULT);
  }

  /**
   * Changes the decoration preview shown by this button.
   *
   * @param deco decoration represented by this button.
   */
  public void setDeco(Deco deco) {
    this.deco = deco;
    ImageButton.ImageButtonStyle style = getStyle();
    DrawComponent draw = new DrawComponent(deco.path(), deco.config());
    style.imageUp = new TextureRegionDrawable(new TextureRegion(draw.getSprite()));
    setStyle(style);
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

  private void applyBackground(String styleName) {
    ImageButton.ImageButtonStyle style = getStyle();
    background(style, styleName);
    setStyle(style);
  }

  private static void background(ImageButton.ImageButtonStyle style, String styleName) {
    com.badlogic.gdx.graphics.Color border =
        STYLE_SELECTED.equals(styleName) ? EditorTheme.SELECTION : null;
    style.up = EditorTheme.rounded(EditorTheme.FIELD, border, 5);
    style.over =
        EditorTheme.rounded(EditorTheme.BUTTON, border == null ? EditorTheme.LINE : border, 5);
    style.down = EditorTheme.rounded(EditorTheme.BUTTON_DOWN, border, 5);
  }

  private static ImageButton.ImageButtonStyle style(Deco deco, String styleName) {
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    background(style, styleName);
    DrawComponent draw = new DrawComponent(deco.path(), deco.config());
    style.imageUp = new TextureRegionDrawable(new TextureRegion(draw.getSprite()));
    return style;
  }
}
