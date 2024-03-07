package systems;

import core.Game;
import core.System;
import core.level.elements.ILevel;
import level.utils.ITickable;

public class LevelTickSystem extends System {

  ILevel currentLevel = null;

  @Override
  public void execute() {

    if (Game.currentLevel() instanceof ITickable tickable) {
      tickable.onTick(currentLevel != Game.currentLevel());
    }
    if (currentLevel != Game.currentLevel()) {
      currentLevel = Game.currentLevel();
    }
  }
}
