package contrib.systems;

import contrib.utils.level.ITickable;
import core.Game;
import core.System;
import core.level.elements.ILevel;

/**
 * The LevelTickSystem is responsible for ticking the current level. It checks if the current level
 * has changed and calls the onTick method of the current level if it implements the ITickable
 * interface.
 *
 * @see ITickable
 * @see core.level.DungeonLevel DungeonLevel
 */
public class LevelTickSystem extends System {

  /** Creates a new LevelTickSystem. */
  public LevelTickSystem() {
    super(AuthoritativeSide.SERVER);
  }

  /** The current level of the game. */
  private ILevel currentLevel = null;

  @Override
  public void execute() {
    if (Game.currentLevel().orElse(null) instanceof ITickable tickable) {
      tickable.onTick(currentLevel != Game.currentLevel().orElse(null));
    }
    if (currentLevel != Game.currentLevel().orElse(null)) {
      this.currentLevel = Game.currentLevel().orElse(null);
    }
  }

  @Override
  public void stop() {
    // Cant be stopped
  }
}
