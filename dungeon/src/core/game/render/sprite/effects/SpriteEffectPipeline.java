package core.game.render.sprite.effects;

import core.Entity;
import java.awt.image.BufferedImage;

/** Applies ordered LITIENGINE sprite effects to entity sprites before drawing. */
public final class SpriteEffectPipeline {

  private SpriteEffectPipeline() {}

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
