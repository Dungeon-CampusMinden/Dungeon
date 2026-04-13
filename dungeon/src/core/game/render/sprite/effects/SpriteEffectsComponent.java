package core.game.render.sprite.effects;

import core.Component;
import java.util.Objects;

/**
 * Component that stores ordered LITIENGINE-only sprite effects for an entity.
 */
public record SpriteEffectsComponent(SpriteEffectRegistry effects) implements Component {

  public SpriteEffectsComponent() {
    this(new SpriteEffectRegistry());
  }

  public SpriteEffectsComponent(SpriteEffectRegistry effects) {
    this.effects = Objects.requireNonNull(effects, "effects");
  }
}
