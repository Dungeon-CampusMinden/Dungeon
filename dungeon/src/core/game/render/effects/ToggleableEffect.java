package core.game.render.effects;

/**
 * Shared helper contract for mutable render effects with fluent enablement toggles.
 *
 * @param <E> effect type returned by the fluent toggle method
 */
public interface ToggleableEffect<E> {

  /**
   * Sets the enabled state of this effect.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for chaining
   */
  E enabled(boolean enabled);
}
