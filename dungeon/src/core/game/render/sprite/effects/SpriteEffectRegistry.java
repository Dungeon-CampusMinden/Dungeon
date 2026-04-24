package core.game.render.sprite.effects;

import core.game.render.effects.ToggleableEffect;
import core.game.render.effects.ToggleableEffectRegistry;

/**
 * Registry for managing {@link SpriteEffect} instances, providing functionality to
 * organize, enable, disable, and mutate sprite effects specifically for rendering.
 *
 * <p>This class extends {@link ToggleableEffectRegistry} to manage effects of type
 * {@link SpriteEffect} with additional support for enabling and disabling effects
 * that implement {@link ToggleableEffect}.
 */
public final class SpriteEffectRegistry extends ToggleableEffectRegistry<SpriteEffect> {

  /** Creates a new sprite effect registry. */
  public SpriteEffectRegistry() {
    super(SpriteEffect::enabled);
  }
}
