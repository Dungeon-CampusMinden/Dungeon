package core.game.render.sprite.effects;

import core.Entity;
import java.awt.image.BufferedImage;

/**
 * The SpriteEffectPipeline class provides a static method for applying a
 * series of sprite effects to an image based on the state of a given entity.
 * These effects are processed in a sorted and enabled order as defined
 * within the entity's {@link SpriteEffectsComponent}.
 *
 * <p>This utility is designed to process graphical transformations or effects
 * sequentially on a sprite image, enabling dynamic and runtime-customizable
 * rendering effects for entities.
 *
 * <p>This class cannot be instantiated.
 */
public final class SpriteEffectPipeline {

  private SpriteEffectPipeline() {}

  /**
   * Applies a series of enabled and sorted sprite effects to the given sprite image
   * based on the state of the specified entity. If the entity or sprite is null,
   * or if no effects are available, returns the original sprite image. The method
   * processes the effects sequentially and ensures that each applied effect does not
   * return a null image.
   *
   * @param entity the entity containing the {@code SpriteEffectsComponent}, which
   *               defines the set of enabled sprite effects to be applied
   * @param sprite the {@code BufferedImage} representing the sprite image to which
   *               the effects will be applied
   * @param nowMs  the current time in milliseconds, used as a reference for applying
   *               time-based sprite effects
   * @return the {@code BufferedImage} resulting from the applied sprite effects, or
   *         the original sprite if no effects are available or applicable
   * @throws IllegalStateException if a sprite effect returns a null sprite image during processing
   */
  public static BufferedImage apply(Entity entity, BufferedImage sprite, long nowMs) {
    if (entity == null || sprite == null) {
      return sprite;
    }

    SpriteEffectsComponent component =
      entity.fetch(SpriteEffectsComponent.class).orElse(null);
    if (component == null || component.effects().isEmpty()) {
      return sprite;
    }

    BufferedImage current = sprite;
    for (SpriteEffect effect : component.effects().getEnabledSorted()) {
      current = effect.apply(current, nowMs);
      if (current == null) {
        throw new IllegalStateException(
          effect.getClass().getSimpleName() + " returned null sprite image.");
      }
    }

    return current;
  }
}
