package core.utils.components.draw;

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
 */
public record DrawInfoData(
    String texturePath,
    Float scaleX,
    Float scaleY,
    String animationName,
    Integer currentFrame,
    AnimationConfigData animationConfig,
    SpritesheetConfigData spritesheetConfig) {

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

  // TODO: Replace texture paths with asset IDs/atlas references once an asset registry exists.
}
