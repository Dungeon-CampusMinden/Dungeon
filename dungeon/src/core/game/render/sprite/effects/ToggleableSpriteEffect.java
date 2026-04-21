package core.game.render.sprite.effects;

/**
 * Optional helper contract for mutable sprite effects.
 */
public interface ToggleableSpriteEffect extends SpriteEffect {

  /**
   * Sets the enabled state of this effect.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for chaining
   */
  SpriteEffect enabled(boolean enabled);
}
