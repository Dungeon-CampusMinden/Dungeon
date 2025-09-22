package contrib.components;

import core.Component;

/**
 * Component that tracks whether a projectile has already ignored its first collision with a wall.
 *
 * <p>Used to allow projectiles to pass through a wall on their first hit if the caster permits it
 * (e.g., {@code canEnterWalls} is true).
 */
public class WallHitStateComponent implements Component {
  private boolean firstWallHitIgnored = false;

  /**
   * Returns whether the first wall hit has already been ignored.
   *
   * @return true if the first wall hit was ignored, false otherwise
   */
  public boolean firstWallHitIgnored() {
    return firstWallHitIgnored;
  }

  /** Marks that the first wall hit has been ignored. */
  public void markFirstWallHitIgnored() {
    this.firstWallHitIgnored = true;
  }
}
