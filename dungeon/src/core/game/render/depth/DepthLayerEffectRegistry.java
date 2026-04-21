package core.game.render.depth;

import core.game.render.effects.EffectRegistryFacade;

/**
 * Registry for managing {@link DepthLayerEffect} instances, providing functionality to
 * organize, enable, disable, and mutate depth-layer effects specifically for rendering.
 *
 * <p>This class extends {@link EffectRegistryFacade} to manage effects of type
 * {@link DepthLayerEffect} with additional support for enabling and disabling effects
 * that implement {@link ToggleableDepthLayerEffect}.
 */
public final class DepthLayerEffectRegistry extends EffectRegistryFacade<DepthLayerEffect> {

  /** Creates a new depth-layer effect registry. */
  public DepthLayerEffectRegistry() {
    super(
      DepthLayerEffect::enabled,
      ToggleableDepthLayerEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableDepthLayerEffect) effect).enabled(enabled));
  }
}
