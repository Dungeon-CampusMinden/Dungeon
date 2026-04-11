package core.platform.litiengine.render.effects;

import core.Entity;
import java.awt.image.BufferedImage;

/** Applies ordered LITIENGINE sprite effects to entity sprites before drawing. */
public final class LitiengineSpriteEffectsRenderer {

  private LitiengineSpriteEffectsRenderer() {}

  public static BufferedImage apply(Entity entity, BufferedImage sprite, long nowMs) {
    if (entity == null || sprite == null) {
      return sprite;
    }

    LitiengineSpriteEffectsComponent component =
      entity.fetch(LitiengineSpriteEffectsComponent.class).orElse(null);
    if (component == null || component.effects().isEmpty()) {
      return sprite;
    }

    BufferedImage current = sprite;
    for (LitiengineSpriteEffect effect : component.effects().getEnabledSorted()) {
      current = effect.apply(current, nowMs);
      if (current == null) {
        throw new IllegalStateException(
          effect.getClass().getSimpleName() + " returned null sprite image.");
      }
    }

    return current;
  }
}
