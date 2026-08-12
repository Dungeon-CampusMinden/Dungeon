package feature.systems;

import engine.Game;
import engine.System;
import engine.level.elements.ILevel;
import feature.level.ITickable;

/**
 * The LevelTickSystem is responsible for ticking the current level. It checks if the current level
 * has changed and calls the onTick method of the current level if it implements the ITickable
 * interface.
 *
 * @see ITickable
 * @see engine.level.DungeonLevel DungeonLevel
 */
public class LevelTickSystem extends System {

  /** Creates a new LevelTickSystem. */
  public LevelTickSystem() {
    super(AuthoritativeSide.BOTH);
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
