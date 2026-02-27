package core.utils.components.draw;

import core.components.DrawComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.SimpleDirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;
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
    List<DrawInfoData.StateData> states =
        component.stateMachine().states().stream().map(DrawComponentFactory::toStateData).toList();

    return new DrawInfoData(
        texturePath,
        animation.getScaleX(),
        animation.getScaleY(),
        stateName,
        frameIndex,
        animationConfig,
        spritesheetConfig,
        states);
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

    List<DrawInfoData.StateData> stateDataList = info.states();
    if (stateDataList != null && !stateDataList.isEmpty()) {
      DrawComponent drawComponent = fromStateData(stateDataList);
      applyCurrentState(info, drawComponent);
      return drawComponent;
    }

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
    applyCurrentState(info, drawComponent);

    return drawComponent;
  }

  private static DrawComponent fromStateData(List<DrawInfoData.StateData> stateDataList) {
    List<State> states = new ArrayList<>();
    for (DrawInfoData.StateData stateData : stateDataList) {
      states.add(toState(stateData));
    }
    if (states.isEmpty()) {
      throw new IllegalArgumentException("DrawInfoData.states must not be empty.");
    }
    return new DrawComponent(states);
  }

  private static State toState(DrawInfoData.StateData stateData) {
    if (stateData == null) {
      throw new IllegalArgumentException("DrawInfoData.StateData is required.");
    }
    String stateName = stateData.stateName();
    if (stateName == null || stateName.isBlank()) {
      throw new IllegalArgumentException("DrawInfoData.StateData.stateName is required.");
    }

    DrawInfoData.StateType stateType =
        stateData.stateType() == null ? DrawInfoData.StateType.BASIC : stateData.stateType();
    return switch (stateType) {
      case BASIC -> new State(stateName, toAnimation(stateData.baseAnimation()));
      case SIMPLE_DIRECTIONAL ->
          new SimpleDirectionalState(stateName, toAnimation(stateData.baseAnimation()));
      case DIRECTIONAL ->
          new DirectionalState(
              stateName,
              toAnimation(stateData.baseAnimation()),
              toAnimation(stateData.leftAnimation()),
              toAnimation(stateData.upAnimation()),
              toAnimation(stateData.rightAnimation()));
    };
  }

  private static DrawInfoData.StateData toStateData(State state) {
    DrawInfoData.StateAnimationData baseAnimationData = toStateAnimationData(state.baseAnimation());
    if (state instanceof DirectionalState directionalState) {
      return new DrawInfoData.StateData(
          state.name,
          DrawInfoData.StateType.DIRECTIONAL,
          baseAnimationData,
          toStateAnimationData(directionalState.leftAnimation()),
          toStateAnimationData(directionalState.upAnimation()),
          toStateAnimationData(directionalState.rightAnimation()));
    }
    if (state instanceof SimpleDirectionalState) {
      return new DrawInfoData.StateData(
          state.name,
          DrawInfoData.StateType.SIMPLE_DIRECTIONAL,
          baseAnimationData,
          null,
          null,
          null);
    }
    return new DrawInfoData.StateData(
        state.name, DrawInfoData.StateType.BASIC, baseAnimationData, null, null, null);
  }

  private static DrawInfoData.StateAnimationData toStateAnimationData(Animation animation) {
    String texturePath =
        animation
            .sourcePath()
            .map(IPath::pathString)
            .orElseThrow(() -> new IllegalArgumentException("Animation source path is missing."));
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
    return new DrawInfoData.StateAnimationData(
        texturePath,
        animation.getScaleX(),
        animation.getScaleY(),
        animationConfig,
        spritesheetConfig);
  }

  private static Animation toAnimation(DrawInfoData.StateAnimationData animationData) {
    if (animationData == null) {
      throw new IllegalArgumentException("DrawInfoData.StateAnimationData is required.");
    }
    String texturePath = animationData.texturePath();
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException("State animation texturePath is required.");
    }
    DrawInfoData.AnimationConfigData animationConfig = animationData.animationConfig();
    if (animationConfig == null) {
      throw new IllegalArgumentException("State animation config is required.");
    }

    AnimationConfig config;
    if (animationData.spritesheetConfig() != null) {
      DrawInfoData.SpritesheetConfigData spritesheetConfig = animationData.spritesheetConfig();
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

    if (animationData.scaleX() != null) {
      config.scaleX(animationData.scaleX());
    }
    if (animationData.scaleY() != null) {
      config.scaleY(animationData.scaleY());
    }

    return new Animation(new SimpleIPath(texturePath), config);
  }

  private static void applyCurrentState(DrawInfoData info, DrawComponent drawComponent) {
    String animationName = info.animationName();
    if (animationName == null || animationName.isEmpty()) {
      return;
    }
    if (!drawComponent.hasState(animationName)) {
      return;
    }
    drawComponent.stateMachine().setState(animationName, null);
    Integer frameIndex = info.currentFrame();
    if (frameIndex != null) {
      Animation animation = drawComponent.currentAnimation();
      int framesPerSprite = animation.getConfig().framesPerSprite();
      int frameCount = Math.max(0, frameIndex) * Math.max(framesPerSprite, 1);
      animation.frameCount(frameCount);
    }
  }

  private static int currentFrameIndex(Animation animation) {
    int framesPerSprite = animation.getConfig().framesPerSprite();
    if (framesPerSprite <= 0) {
      return 0;
    }
    return animation.frameCount() / framesPerSprite;
  }
}
