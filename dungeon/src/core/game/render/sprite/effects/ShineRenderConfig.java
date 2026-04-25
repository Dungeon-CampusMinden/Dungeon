package core.game.render.sprite.effects;

import java.awt.Color;

/**
 * Represents the configuration parameters for a shine rendering effect.
 *
 * <p>This configuration is immutable and encapsulates all the customizable
 * settings for rendering the shine effect. It is used as part of the
 * {@code ShineSpriteEffect} for applying animated shine overlays to sprites.
 *
 * <p>Instances of this class are created using its constructor and cannot
 * be modified after creation, ensuring safe sharing across different parts
 * of the rendering pipeline.
 *
 * @param padding the extra padding in pixels around the sprite for rendering the shine effect
 * @param sliceCount the number of individual shine slices for large sprites
 * @param gapSize the proportional size of the gap between adjacent shine slices, in the range [0, 1]
 * @param rotationSpeed the rotational speed of the shine effect, expressed in rotations per second
 * @param shineColor the {@code Color} used for the shine overlay
 */
record ShineRenderConfig(
  int padding,
  int sliceCount,
  float gapSize,
  float rotationSpeed,
  Color shineColor) {}
