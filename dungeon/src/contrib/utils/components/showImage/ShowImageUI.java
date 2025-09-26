package contrib.utils.components.showImage;

import com.badlogic.gdx.Gdx;
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
import core.Game;

/** UI element that displays an image with optional text, used through the ShowImageSystem. */
public class ShowImageUI extends Group {

  private static final float SCALE = 1f;
  private static final int ANIMATION_OFFSET_X = -5;
  private static final int ANIMATION_OFFSET_Y = -50;
  private static final float SHOW_TRANSITION_PROGRESS = 1 / 30f;

  private final ShowImageComponent component;

  private Image background;
  private String currentImagePath = null;
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
  }

  private void createActors() {
    this.setScale(SCALE);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    currentImagePath = component.imagePath();
    background = new Image(new Texture(Gdx.files.internal(currentImagePath)));
    background.setOrigin(Align.center);
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

    if (!currentImagePath.equals(component.imagePath())) {
      currentImagePath = component.imagePath();
      background.setDrawable(
          new TextureRegionDrawable(new Texture(Gdx.files.internal(currentImagePath))));
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
    animation = Math.min(1, animation + SHOW_TRANSITION_PROGRESS);

    super.draw(batch, parentAlpha);
  }

  private float animationOffsetX() {
    return Interpolation.smooth.apply(ANIMATION_OFFSET_X, 0, animation);
  }

  private float animationOffsetY() {
    return Interpolation.smooth.apply(ANIMATION_OFFSET_Y, 0, animation);
  }
}
