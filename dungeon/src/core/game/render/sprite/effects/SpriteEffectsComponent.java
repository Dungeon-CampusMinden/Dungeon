package core.game.render.sprite.effects;

import core.Component;
import java.util.Objects;

/**
 * A component that stores sprite effects for an entity.
 *
 * <p>This component holds a registry of sprite effects that can be applied to a sprite during rendering.
 * It is used to attach visual effects such as shine, hue remapping, or other sprite transformations
 * to game entities.
 *
 * @param effects the sprite effect registry
 */
public record SpriteEffectsComponent(SpriteEffectRegistry effects) implements Component {

  /**
   * Creates a sprite effects component with a new empty effect registry.
   */
  public SpriteEffectsComponent() {
    this(new SpriteEffectRegistry());
  }

  /**
   * Creates a sprite effects component with the specified effect registry.
   *
   * @param effects the sprite effect registry (must not be null)
   * @throws NullPointerException if effects are null
   */
  public SpriteEffectsComponent(SpriteEffectRegistry effects) {
    this.effects = Objects.requireNonNull(effects, "effects");
  }
}
