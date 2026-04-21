package core.game.render.level;

/**
 * Optional helper contract for mutable level-pass effects.
 */
public interface ToggleableLevelEffect extends LevelEffect {

  /**
   * Sets the enabled state of this effect.
   *
   * @param enabled true to enable the effect, false to disable it
   */
  void enabled(boolean enabled);
}
