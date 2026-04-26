package core.game.render.level;

import core.game.render.effects.ToggleableEffect;
import core.game.render.effects.ToggleableEffectRegistry;

/**
 * Registry for managing {@link LevelEffect} instances used in the rendered level layer. Provides an
 * interface for adding, removing, retrieving, and modifying level effects with specific enablement
 * and priority rules.
 *
 * <p>This class extends {@link ToggleableEffectRegistry} and is configured to work specifically
 * with {@link LevelEffect} and its toggleable variant via {@link ToggleableEffect}.
 */
public final class LevelEffectRegistry extends ToggleableEffectRegistry<LevelEffect> {

  /** Creates a new level effect registry. */
  public LevelEffectRegistry() {
    super(LevelEffect::enabled);
  }
}
