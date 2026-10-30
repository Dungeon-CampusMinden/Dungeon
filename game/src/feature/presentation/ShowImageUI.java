package feature.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;

/**
 * UI element that displays an image, used as the renderer of {@link
 * feature.hud.dialogs.DialogType.DefaultTypes#IMAGE} dialogs.
 */
public class ShowImageUI extends Group {

  private static final float SCALE = 1f;
  private static final int ANIMATION_OFFSET_X = -5;
  private static final int ANIMATION_OFFSET_Y = -50;

  /** Maximum share of the screen the image occupies in its biggest axis. */
  private static final float MAX_SIZE = 0.85f;

  private final TransitionSpeed transitionSpeed;

  private Image background;
  private float animation;

  private ShowImageUI(String imagePath, TransitionSpeed transitionSpeed) {
    this.transitionSpeed = transitionSpeed;
    createActors(imagePath);
    animation = transitionSpeed == TransitionSpeed.DISABLED ? 1 : 0;
  }

  private void createActors(String imagePath) {
    this.setScale(SCALE);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    String localizedPath = Game.localization().asset(imagePath);
    background = new Image(TextureMap.instance().textureAt(new SimpleIPath(localizedPath)));
    background.setOrigin(Align.center);
    this.addActor(background);
  }

  /**
   * Builds a ShowImageUI from the given DialogContext.
   *
   * <p>Reads the image path from {@link DialogContextKeys#IMAGE} and the optional transition speed
   * from {@link DialogContextKeys#IMAGE_TRANSITION_SPEED}, which defaults to {@link
   * TransitionSpeed#MEDIUM}.
   *
   * @param ctx the DialogContext containing the necessary attributes
   * @return a new ShowImageUI instance
   */
  public static Group build(DialogContext ctx) {
    String imagePath = ctx.require(DialogContextKeys.IMAGE, String.class);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    TransitionSpeed speed =
        ctx.find(DialogContextKeys.IMAGE_TRANSITION_SPEED, TransitionSpeed.class)
            .orElse(TransitionSpeed.MEDIUM);
    return new ShowImageUI(imagePath, speed);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    this.setScale(SCALE);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    float imageWidth = background.getImageWidth();
    float imageHeight = background.getImageHeight();
    float maxWidth = Game.windowWidth() * MAX_SIZE;
    float maxHeight = Game.windowHeight() * MAX_SIZE;

    float scaleX = maxWidth / imageWidth;
    float scaleY = maxHeight / imageHeight;

    float minScale = Math.min(scaleX, scaleY);
    background.setScale(minScale);
    background.setPosition(getX(Align.center), getY(Align.center), Align.center);

    this.setPosition(animationOffsetX(), animationOffsetY());
    this.setColor(1, 1, 1, animation);
    if (animation < 1) {
      animation = Math.min(1, animation + (1f / transitionSpeed.framesToComplete));
    }

    Color prev = batch.getColor();
    float pr = prev.r;
    float pg = prev.g;
    float pb = prev.b;
    float pa = prev.a;

    super.draw(batch, parentAlpha);

    batch.setColor(pr, pg, pb, pa);
  }

  private float animationOffsetX() {
    return Interpolation.smooth.apply(ANIMATION_OFFSET_X, 0, animation);
  }

  private float animationOffsetY() {
    return Interpolation.smooth.apply(ANIMATION_OFFSET_Y, 0, animation);
  }
}
