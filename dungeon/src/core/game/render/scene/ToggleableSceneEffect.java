package core.game.render.scene;

/**
 * Optional helper contract for mutable scene effects.
 */
public interface ToggleableSceneEffect extends SceneEffect {

  /**
   * Sets the enabled state of this effect.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for chaining
   */
  SceneEffect enabled(boolean enabled);
}
