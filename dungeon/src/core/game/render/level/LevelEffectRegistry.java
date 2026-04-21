package core.game.render.level;

import core.game.render.effects.EffectRegistryFacade;

/**
 * Registry for managing {@link LevelEffect} instances used in the rendered level layer.
 * Provides an interface for adding, removing, retrieving, and modifying level effects
 * with specific enablement and priority rules.
 *
 * <p>This class extends {@link EffectRegistryFacade} and is configured to work specifically
 * with {@link LevelEffect} and its toggleable variant {@link ToggleableLevelEffect}.
 */
public final class LevelEffectRegistry extends EffectRegistryFacade<LevelEffect> {

  /** Creates a new level effect registry. */
  public LevelEffectRegistry() {
    super(
      LevelEffect::enabled,
      ToggleableLevelEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableLevelEffect) effect).enabled(enabled));
  }
}
