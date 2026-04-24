package core.game.render.depth;

import core.game.render.effects.ToggleableEffectRegistry;
import core.game.render.effects.ToggleableEffect;

/**
 * Registry for managing {@link DepthLayerEffect} instances, providing functionality to
 * organize, enable, disable, and mutate depth-layer effects specifically for rendering.
 *
 * <p>This class extends {@link ToggleableEffectRegistry} to manage effects of type
 * {@link DepthLayerEffect} with additional support for enabling and disabling effects
 * that implement {@link ToggleableEffect}.
 */
public final class DepthLayerEffectRegistry extends ToggleableEffectRegistry<DepthLayerEffect> {

  /** Creates a new depth-layer effect registry. */
  public DepthLayerEffectRegistry() {
    super(DepthLayerEffect::enabled);
  }
}
