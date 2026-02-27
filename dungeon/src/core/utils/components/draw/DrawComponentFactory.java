package core.utils.components.draw;

import core.components.DrawComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Objects;

/** Factory for building draw components and data payloads for networking. */
public final class DrawComponentFactory {

  private DrawComponentFactory() {}

  /**
   * Extracts draw information from a {@link DrawComponent} without touching GPU resources.
   *
   * @param component the source draw component
   * @return data-only draw info
   */
  public static DrawInfoData toDrawInfo(DrawComponent component) {
    Objects.requireNonNull(component, "component");
    Animation animation = component.currentAnimation();
    String texturePath =
        animation
            .sourcePath()
            .map(IPath::pathString)
            .orElseThrow(() -> new IllegalArgumentException("DrawComponent path is missing."));

    String stateName = component.currentStateName();
    Integer frameIndex = null;
    if (stateName != null && !stateName.isEmpty()) {
      frameIndex = currentFrameIndex(animation);
    }

    AnimationConfig config = animation.getConfig();
    DrawInfoData.AnimationConfigData animationConfig =
        new DrawInfoData.AnimationConfigData(
            config.framesPerSprite(), config.isLooping(), config.centered(), config.mirrored());
    DrawInfoData.SpritesheetConfigData spritesheetConfig =
        config
            .config()
            .map(
                value ->
                    new DrawInfoData.SpritesheetConfigData(
                        value.spriteWidth(),
                        value.spriteHeight(),
                        value.x(),
                        value.y(),
                        value.rows(),
                        value.columns()))
            .orElse(null);

    return new DrawInfoData(
        texturePath,
        animation.getScaleX(),
        animation.getScaleY(),
        stateName,
        frameIndex,
        animationConfig,
        spritesheetConfig);
  }

  /**
   * Builds a {@link DrawComponent} from the provided draw info.
   *
   * <p>Call this on the render thread, since it can trigger texture loading.
   *
   * @param info the data-only draw info
   * @return a fully initialized draw component
   */
  public static DrawComponent fromDrawInfo(DrawInfoData info) {
    Objects.requireNonNull(info, "info");
    String texturePath = info.texturePath();
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException("DrawInfoData.texturePath is required.");
    }

    DrawInfoData.AnimationConfigData animationConfig =
        Objects.requireNonNull(info.animationConfig(), "DrawInfoData.animationConfig is required.");

    AnimationConfig config;
    if (info.spritesheetConfig() != null) {
      DrawInfoData.SpritesheetConfigData spritesheetConfig = info.spritesheetConfig();
      SpritesheetConfig sourceConfig =
          new SpritesheetConfig(
              spritesheetConfig.offsetX(),
              spritesheetConfig.offsetY(),
              spritesheetConfig.rows(),
              spritesheetConfig.columns(),
              spritesheetConfig.spriteWidth(),
              spritesheetConfig.spriteHeight());
      config = new AnimationConfig(sourceConfig);
    } else {
      config = new AnimationConfig();
    }

    config.framesPerSprite(Math.max(1, animationConfig.framesPerSprite()));
    config.isLooping(animationConfig.looping());
    config.centered(animationConfig.centered());
    config.mirrored(animationConfig.mirrored());

    if (info.scaleX() != null) {
      config.scaleX(info.scaleX());
    }
    if (info.scaleY() != null) {
      config.scaleY(info.scaleY());
    }

    // TODO: Centralize texture/atlas resolution via AssetManager once available.
    DrawComponent drawComponent = new DrawComponent(new SimpleIPath(texturePath), config);

    String animationName = info.animationName();
    if (animationName != null
        && !animationName.isEmpty()
        && drawComponent.hasState(animationName)) {
      drawComponent.stateMachine().setState(animationName, null);
      Integer frameIndex = info.currentFrame();
      if (frameIndex != null) {
        Animation animation = drawComponent.currentAnimation();
        int framesPerSprite = animation.getConfig().framesPerSprite();
        int frameCount = Math.max(0, frameIndex) * Math.max(framesPerSprite, 1);
        animation.frameCount(frameCount);
      }
    }

    return drawComponent;
  }

  private static int currentFrameIndex(Animation animation) {
    int framesPerSprite = animation.getConfig().framesPerSprite();
    if (framesPerSprite <= 0) {
      return 0;
    }
    return animation.frameCount() / framesPerSprite;
  }
}
