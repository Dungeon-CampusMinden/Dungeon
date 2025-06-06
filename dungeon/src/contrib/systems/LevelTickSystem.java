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
 * @see level.DevDungeonLevel DevDungeonLevel
 */
public class LevelTickSystem extends System {

  /** The current level of the game. */
  private ILevel currentLevel = null;

  @Override
  public void execute() {
    if (Game.currentLevel() instanceof ITickable tickable) {
      tickable.onTick(currentLevel != Game.currentLevel());
    }
    if (currentLevel != Game.currentLevel()) {
      this.currentLevel = Game.currentLevel();
    }
  }
}
