package contrib.hud.image;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import contrib.components.ShowImageComponent;
import contrib.hud.UIUtils;
import contrib.utils.components.showImage.TransitionSpeed;
import core.Game;
import core.ui.gdx.GdxUiAssetLoader;
import core.utils.components.path.SimpleIPath;
import java.util.Objects;

/** UI element that displays an image with optional text, used through the ShowImageSystem. */
public class ShowImageUI extends Group {

  private static final float SCALE = 1f;
  private static final int ANIMATION_OFFSET_X = -5;
  private static final int ANIMATION_OFFSET_Y = -50;

  private final ShowImageComponent component;

  private Image background;
  private String currentImagePath = null;
  private Texture currentTexture;
  private float animation;

  /**
   * Creates a new ShowImageUI for the given entity.
   *
   * @param sic the ShowImageComponent containing the image and text configuration
   */
  public ShowImageUI(ShowImageComponent sic) {
    this.component = sic;
    createActors();
    animation = 0;
    if (sic.transitionSpeed() == TransitionSpeed.DISABLED) {
      animation = 1;
    }
  }

  private void createActors() {
    this.setScale(SCALE);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    currentImagePath = component.imagePath();
    background = new Image();
    background.setOrigin(Align.center);
    updateBackgroundTexture(currentImagePath);
    this.addActor(background);

    if (component.textConfig() != null) {
      Table table = new Table();
      table.setFillParent(true);
      Label label = new Label(component.textConfig().text(), UIUtils.defaultSkin());
      label.setFontScale(component.textConfig().scale());
      label.setColor(component.textConfig().color());
      table.add(label);
      this.addActor(table);
    }
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    this.setScale(SCALE);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    if (!Objects.equals(currentImagePath, component.imagePath())) {
      currentImagePath = component.imagePath();
      updateBackgroundTexture(currentImagePath);
    }

    float imageWidth = background.getImageWidth();
    float imageHeight = background.getImageHeight();
    float maxWidth = Game.windowWidth() * component.maxSize();
    float maxHeight = Game.windowHeight() * component.maxSize();

    float scaleX = maxWidth / imageWidth;
    float scaleY = maxHeight / imageHeight;

    float minScale = Math.min(scaleX, scaleY);
    background.setScale(minScale);
    background.setPosition(getX(Align.center), getY(Align.center), Align.center);

    this.setPosition(animationOffsetX(), animationOffsetY());
    this.setColor(1, 1, 1, animation);
    if (animation < 1) {
      animation =
        Math.min(1, animation + (1f / component.transitionSpeed().framesToComplete));
    }

    super.draw(batch, parentAlpha);
  }

  @Override
  public boolean remove() {
    disposeCurrentTexture();
    return super.remove();
  }

  private void updateBackgroundTexture(String imagePath) {
    disposeCurrentTexture();
    currentTexture = GdxUiAssetLoader.loadTexture(new SimpleIPath(imagePath));
    background.setDrawable(new TextureRegionDrawable(currentTexture));
  }

  private void disposeCurrentTexture() {
    if (currentTexture != null) {
      currentTexture.dispose();
      currentTexture = null;
    }
  }

  private float animationOffsetX() {
    return Interpolation.smooth.apply(ANIMATION_OFFSET_X, 0, animation);
  }

  private float animationOffsetY() {
    return Interpolation.smooth.apply(ANIMATION_OFFSET_Y, 0, animation);
  }
}
