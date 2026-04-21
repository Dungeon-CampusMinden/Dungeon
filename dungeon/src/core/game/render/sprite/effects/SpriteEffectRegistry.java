package core.game.render.sprite.effects;

import core.game.render.effects.EffectRegistryFacade;

/**
 * Registry for managing {@link SpriteEffect} instances, providing functionality to
 * organize, enable, disable, and mutate sprite effects specifically for rendering.
 *
 * <p>This class extends {@link EffectRegistryFacade} to manage effects of type
 * {@link SpriteEffect} with additional support for enabling and disabling effects
 * that implement {@link ToggleableSpriteEffect}.
 */
public final class SpriteEffectRegistry extends EffectRegistryFacade<SpriteEffect> {

  /** Creates a new sprite effect registry. */
  public SpriteEffectRegistry() {
    super(
      SpriteEffect::enabled,
      ToggleableSpriteEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableSpriteEffect) effect).enabled(enabled));
  }
}
