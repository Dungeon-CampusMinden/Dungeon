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
 */
public record DrawInfoData(
    String texturePath, Float scaleX, Float scaleY, String animationName, Integer currentFrame) {

  // TODO: Replace texture paths with asset IDs/atlas references once an asset registry exists.
}
