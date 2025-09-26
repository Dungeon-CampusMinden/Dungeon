package produsAdvanced.abstraction.portals.systems;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.TriConsumer;
import produsAdvanced.abstraction.portals.components.PortalComponent;
import produsAdvanced.abstraction.portals.components.PortalProjectileComponent;

import java.lang.reflect.Field;

public class PortalCollideSystem extends System   {
  @Override
  public void execute() {
    Game.allEntities().filter(entity -> entity.isPresent(ProjectileComponent.class))
      .forEach(entity -> {
        if (entity.isPresent(PortalProjectileComponent.class)) {
          return;
        }
        CollideComponent cc = entity.fetch(CollideComponent.class).get();

        TriConsumer<Entity, Entity, Direction> oldEnter = CollideComponent.DEFAULT_COLLIDER;
        try {
          Field f = CollideComponent.class.getDeclaredField("collideEnter");
          f.setAccessible(true);
          oldEnter = (TriConsumer<Entity, Entity, Direction>) f.get(cc);
        } catch (Exception e) {
          e.printStackTrace();
        }

        TriConsumer<Entity, Entity, Direction> finalOldEnter = oldEnter;
        cc.collideEnter((self, other, dir) -> {
          if (other.isPresent(PortalComponent.class)) {
            java.lang.System.out.println("Portal Collision: " + self.name() + " collided with " + other.name() + " at " + self.fetch(PositionComponent.class).get().position() );
            return;
          }
            java.lang.System.out.println("Collision: " + self.name() + " collided with " + other.name() + " at " + self.fetch(PositionComponent.class).get().position() );
            finalOldEnter.accept(self, other, dir);
        });

        java.lang.System.out.println("added PortalProjectileComponent to " + entity.name());
        entity.add(new PortalProjectileComponent());
      });
  }
}
