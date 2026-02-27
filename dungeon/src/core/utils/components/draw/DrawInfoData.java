package core.utils.components.draw;

import java.util.List;

/**
 * Data-only representation of draw state for network synchronization.
 *
 * <p>This class is safe to construct on non-render threads and contains no libGDX references.
 *
 * @param texturePath asset path used for rendering
 * @param scaleX optional X scale override (null = default)
 * @param scaleY optional Y scale override (null = default)
 * @param animationName optional current animation state name
 * @param currentFrame optional current animation frame index
 * @param animationConfig animation timing/behavior configuration
 * @param spritesheetConfig optional spritesheet geometry configuration
 * @param states optional explicit state machine definition for reconstructing custom states
 */
public record DrawInfoData(
    String texturePath,
    Float scaleX,
    Float scaleY,
    String animationName,
    Integer currentFrame,
    AnimationConfigData animationConfig,
    SpritesheetConfigData spritesheetConfig,
    List<StateData> states) {

  /**
   * Network-level animation config hints.
   *
   * @param framesPerSprite number of update ticks each sprite frame is displayed
   * @param looping whether the animation loops
   * @param centered whether sprites are drawn centered
   * @param mirrored whether sprites are drawn horizontally mirrored
   */
  public record AnimationConfigData(
      int framesPerSprite, boolean looping, boolean centered, boolean mirrored) {}

  /**
   * Network-level spritesheet geometry hints.
   *
   * @param spriteWidth width of one sprite frame in pixels
   * @param spriteHeight height of one sprite frame in pixels
   * @param offsetX x offset of the first sprite in the sheet
   * @param offsetY y offset of the first sprite in the sheet
   * @param rows number of rows in the spritesheet grid
   * @param columns number of columns in the spritesheet grid
   */
  public record SpritesheetConfigData(
      int spriteWidth, int spriteHeight, int offsetX, int offsetY, int rows, int columns) {}

  /** Behavior classification for state reconstruction. */
  public enum StateType {
    /** Regular {@code State}. */
    BASIC,
    /** {@code SimpleDirectionalState} (single animation, mirrored left/right). */
    SIMPLE_DIRECTIONAL,
    /** {@code DirectionalState} (distinct directional animations). */
    DIRECTIONAL
  }

  /**
   * Network-level animation source data for one state variant.
   *
   * @param texturePath asset path used for rendering
   * @param scaleX optional X scale override (null = default)
   * @param scaleY optional Y scale override (null = default)
   * @param animationConfig animation timing/behavior configuration
   * @param spritesheetConfig optional spritesheet geometry configuration
   */
  public record StateAnimationData(
      String texturePath,
      Float scaleX,
      Float scaleY,
      AnimationConfigData animationConfig,
      SpritesheetConfigData spritesheetConfig) {}

  /**
   * Network-level state definition for reconstructing custom state machines.
   *
   * @param stateName the state name as used by {@code StateMachine#setState}
   * @param stateType the state behavior type
   * @param baseAnimation animation used as default (and DOWN for directional states)
   * @param leftAnimation optional LEFT-direction animation (directional states only)
   * @param upAnimation optional UP-direction animation (directional states only)
   * @param rightAnimation optional RIGHT-direction animation (directional states only)
   */
  public record StateData(
      String stateName,
      StateType stateType,
      StateAnimationData baseAnimation,
      StateAnimationData leftAnimation,
      StateAnimationData upAnimation,
      StateAnimationData rightAnimation) {}

  // TODO: Replace texture paths with asset IDs/atlas references once an asset registry exists.
}
