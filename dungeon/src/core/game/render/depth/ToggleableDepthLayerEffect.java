package core.game.render.depth;

/**
 * Optional helper contract for mutable depth-layer effects.
 */
public interface ToggleableDepthLayerEffect extends DepthLayerEffect {

  /**
   * Sets the enabled state of this effect.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for chaining
   */
  DepthLayerEffect enabled(boolean enabled);
}
