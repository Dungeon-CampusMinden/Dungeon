package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import engine.components.DrawComponent;
import feature.entities.deco.Deco;
import feature.hud.UIUtils;

/** An image button showing a decoration preview. */
public class DecoButton extends ImageButton {

  private static final String STYLE_DEFAULT = "default";
  private static final String STYLE_SELECTED = "blue-outline";

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
    super(style(deco, selected ? STYLE_SELECTED : STYLE_DEFAULT));
    this.deco = deco;
    this.selectedStyle = STYLE_SELECTED;
    getImageCell().size(size * 0.68f);
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
    TextButton.TextButtonStyle base =
        UIUtils.defaultSkin().get(styleName, TextButton.TextButtonStyle.class);
    ImageButton.ImageButtonStyle style = getStyle();
    style.up = base.up;
    style.down = base.down;
    style.over = base.over;
    setStyle(style);
  }

  private static ImageButton.ImageButtonStyle style(Deco deco, String styleName) {
    TextButton.TextButtonStyle base =
        UIUtils.defaultSkin().get(styleName, TextButton.TextButtonStyle.class);
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = base.up;
    style.down = base.down;
    style.over = base.over;
    DrawComponent draw = new DrawComponent(deco.path(), deco.config());
    style.imageUp = new TextureRegionDrawable(new TextureRegion(draw.getSprite()));
    return style;
  }
}
