package systems;

import core.Game;
import core.System;
import core.level.elements.ILevel;
import level.utils.ITickable;

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
  ILevel currentLevel = null;

  @Override
  public void execute() {

    if (Game.currentLevel() instanceof ITickable tickable) {
      tickable.onTick(this.currentLevel != Game.currentLevel());
    }
    if (this.currentLevel != Game.currentLevel()) {
      this.currentLevel = Game.currentLevel();
    }
  }
}
