package produsAdvanced.abstraction.portals.systems;

import contrib.components.ProjectileComponent;
import core.Game;
import core.System;
import core.components.PositionComponent;

public class TestingSystem extends System {
  @Override
  public void execute() {
    Game.allEntities().filter(entity -> entity.isPresent(PositionComponent.class))
      .forEach(entity -> {
        if (entity.isPresent(ProjectileComponent.class)) {
          PositionComponent pc = entity.fetch(PositionComponent.class).get();
          java.lang.System.out.println(entity.name() + " " +pc.position());
        }
      });
  }
}
