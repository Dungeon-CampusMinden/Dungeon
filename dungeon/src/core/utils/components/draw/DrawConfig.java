package core.utils.components.draw;

import core.utils.Vector2;

/**
 * Configuration for drawing textures.
 *
 * @param offset The texture's position offset relative to the entity's position.
 * @param size The scaling factors for the texture (e.g., width and height scale).
 * @param scale The uniform scale factor applied on top of 'size'. Defaults to Vector2.ONE.
 * @param tintColor The color to tint the texture with. -1 means no tint color.
 * @param mirrored Whether the texture should be mirrored (flipped).
 * @param rotation The rotation of the texture in degrees.
 */
public record DrawConfig(
    Vector2 offset, Vector2 size, Vector2 scale, int tintColor, boolean mirrored, float rotation) {

  // --- Primary Canonical Constructor ---
  // The canonical constructor is implicitly generated for all fields.
  // We add a custom one to provide robust defaults and cleaning.
  public DrawConfig {
    // Ensure non-null defaults for complex types
    scale = (scale != null) ? scale : Vector2.ONE;
    offset = (offset != null) ? offset : Vector2.ZERO;
    size = (size != null) ? size : Vector2.ONE;
    // tintColor, mirrored, and rotation have good default values from the constructor signature
  }

  // --- Unified Helper Constructors ---

  /** Creates a minimal DrawConfig. */
  public DrawConfig() {
    this(Vector2.ZERO, Vector2.ONE, Vector2.ONE, -1, false, 0f);
  }

  /**
   * Creates a DrawConfig with specified offset and tint color, using default size and scale.
   *
   * @param xOffset The texture's x-axis offset.
   * @param yOffset The texture's y-axis offset.
   * @param tintColor The color to tint the texture with.
   */
  public DrawConfig(float xOffset, float yOffset, int tintColor) {
    this(Vector2.of(xOffset, yOffset), Vector2.ONE, Vector2.ONE, tintColor, false, 0f);
  }

  /**
   * Creates a DrawConfig with all core offset, size, and tint properties specified.
   *
   * @param xOffset The x offset.
   * @param yOffset The y offset.
   * @param xSize The x scaling factor.
   * @param ySize The y scaling factor.
   * @param tintColor The tint color.
   */
  public DrawConfig(float xOffset, float yOffset, float xSize, float ySize, int tintColor) {
    this(Vector2.of(xOffset, yOffset), Vector2.of(xSize, ySize), Vector2.ONE, tintColor, false, 0f);
  }

  // --- Fluent Setter-like Methods (for creating new, modified instances) ---

  /**
   * Creates a new DrawConfig instance with the specified scale.
   *
   * @param scale The new scale as a {@link Vector2}.
   * @return A new DrawConfig instance.
   */
  public DrawConfig withScale(Vector2 scale) {
    return new DrawConfig(offset, size, scale, tintColor, mirrored, rotation);
  }

  /**
   * Creates a new DrawConfig instance with the specified tint color.
   *
   * @param tintColor The new tint color.
   * @return A new DrawConfig instance.
   */
  public DrawConfig withTintColor(int tintColor) {
    return new DrawConfig(offset, size, scale, tintColor, mirrored, rotation);
  }

  /**
   * Creates a new DrawConfig instance with the specified mirrored state.
   *
   * @param mirrored True to mirror the texture, false otherwise.
   * @return A new DrawConfig instance.
   */
  public DrawConfig withMirrored(boolean mirrored) {
    return new DrawConfig(offset, size, scale, tintColor, mirrored, rotation);
  }

  /**
   * Creates a new DrawConfig instance with the specified rotation.
   *
   * @param rotation The new rotation in degrees.
   * @return A new DrawConfig instance.
   */
  public DrawConfig withRotation(float rotation) {
    return new DrawConfig(offset, size, scale, tintColor, mirrored, rotation);
  }
}
