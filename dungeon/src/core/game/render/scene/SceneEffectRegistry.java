package core.game.render.scene;

import core.game.render.effects.ToggleableEffect;
import core.game.render.effects.ToggleableEffectRegistry;

/**
 * Registry for managing {@link SceneEffect} instances with support for enabling, disabling, and
 * prioritizing effects. This class acts as a specialized implementation of {@link
 * ToggleableEffectRegistry} tailored to handle scene effects.
 *
 * <p>This class extends {@link ToggleableEffectRegistry} and is configured to manage effects of
 * type {@link SceneEffect} and toggleable implementations via {@link ToggleableEffect}, providing a
 * structured way to organize and manipulate scene effects within the rendering system.
 */
public final class SceneEffectRegistry extends ToggleableEffectRegistry<SceneEffect> {

  /** Creates a new scene effect registry. */
  public SceneEffectRegistry() {
    super(SceneEffect::enabled);
  }
}
