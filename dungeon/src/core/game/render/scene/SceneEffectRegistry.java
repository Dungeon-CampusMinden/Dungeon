package core.game.render.scene;

import core.game.render.effects.EffectRegistryFacade;

/**
 * Registry for managing {@link SceneEffect} instances with support for enabling, disabling,
 * and prioritizing effects. This class acts as a specialized implementation of
 * {@link EffectRegistryFacade} tailored to handle scene effects.
 *
 * <p>This class extends {@link EffectRegistryFacade} and is configured to manage effects of type
 * {@link SceneEffect} and its toggleable variant {@link ToggleableSceneEffect}, providing
 * a structured way to organize and manipulate scene effects within the rendering system.
 */
public final class SceneEffectRegistry extends EffectRegistryFacade<SceneEffect> {

  /** Creates a new scene effect registry. */
  public SceneEffectRegistry() {
    super(
      SceneEffect::enabled,
      ToggleableSceneEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableSceneEffect) effect).enabled(enabled));
  }
}
