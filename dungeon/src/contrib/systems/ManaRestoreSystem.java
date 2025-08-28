package contrib.systems;

import contrib.components.ManaComponent;
import core.Game;
import core.System;

public class ManaRestoreSystem extends System {

  public ManaRestoreSystem() {
    super(ManaComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .flatMap(e -> e.fetch(ManaComponent.class).stream())
        .forEach(m -> m.restore(m.getRestorePerSecond() / Game.frameRate()));
  }
}
