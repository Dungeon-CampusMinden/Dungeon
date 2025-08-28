package contrib.systems;

import contrib.components.EnergyComponent;
import core.Game;
import core.System;

public class EnergyRestoreSystem extends System {

  public EnergyRestoreSystem() {
    super(EnergyComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .flatMap(e -> e.fetch(EnergyComponent.class).stream())
        .forEach(c -> c.restore(c.getRestorePerSecond() / Game.frameRate()));
  }
}
