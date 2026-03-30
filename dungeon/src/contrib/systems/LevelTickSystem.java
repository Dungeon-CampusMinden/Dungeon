package contrib.systems;

import contrib.utils.level.ITickable;
import core.Game;
import core.System;
import core.level.elements.ILevel;

/**
 * The LevelTickSystem is responsible for ticking the current level.
 *
 * <p>It checks if the current level has changed and calls the {@code onTick} method of the current
 * level if it implements the {@link ITickable} interface.
 *
 * <p>The system now uses time-based ECS scheduling so the level tick cadence is derived from the
 * configured target frame duration instead of implicit per-frame execution.
 *
 * @see ITickable
 * @see core.level.DungeonLevel
 */
public class LevelTickSystem extends System {

  /** The current level of the game. */
  private ILevel currentLevel = null;

  /** Creates a new LevelTickSystem. */
  public LevelTickSystem() {
    super(AuthoritativeSide.SERVER, Game.targetFrameDurationSeconds());
  }

  @Override
  public void execute() {
    if (Game.currentLevel().orElse(null) instanceof ITickable tickable) {
      tickable.onTick(currentLevel != Game.currentLevel().orElse(null));
    }
    if (currentLevel != Game.currentLevel().orElse(null)) {
      this.currentLevel = Game.currentLevel().orElse(null);
    }
  }
}
