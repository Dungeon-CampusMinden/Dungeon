package portal.laser.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.System;
import java.util.Optional;
import java.util.Set;

import portal.laser.LaserCube;
import portal.laser.LaserCubeStatus;
import portal.laser.components.LaserComponent;
import portal.laser.components.LaserCubeComponent;
import portal.laser.components.LaserPartComponent;

public class LaserCubeSystem extends System {

  public LaserCubeSystem() {
    super(LaserCubeComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .forEach(
            entity -> {
              LaserCubeComponent lc = entity.fetch(LaserCubeComponent.class).get();
              switch (lc.getCurrentStatus()) {
                case LaserCubeStatus.ENTER_CUBE -> {
                  getLaserEntity(entity)
                      .ifPresent(
                          laserEntity -> {
                            LaserCube.onEnterCube(
                                entity, laserEntity, laserEntity.fetch(LaserComponent.class).get());
                          });

                  lc.setCurrentStatus(LaserCubeStatus.NONE);
                }
                case LaserCubeStatus.LEAVE_CUBE -> {
                  entity.fetch(LaserCubeComponent.class).get().setActive(false);
                  entity.remove(LaserComponent.class);

                  getLaserEntity(entity)
                      .ifPresent(
                          laserEntity -> {
                            LaserCube.onLeaveCube(entity, laserEntity);
                            entity.remove(LaserPartComponent.class);
                          });
                  lc.setCurrentStatus(LaserCubeStatus.NONE);
                }
                case LaserCubeStatus.NONE -> {}
              }
            });
  }

  private Optional<Entity> getLaserEntity(Entity cube) {
    LaserPartComponent laserPartComponent = cube.fetch(LaserPartComponent.class).get();
    return Game.levelEntities(Set.of(LaserPartComponent.class))
        .filter(entity -> entity.fetch(LaserPartComponent.class).get().equals(laserPartComponent))
        .filter(entity -> !entity.isPresent(LaserCubeComponent.class))
        .filter(entity -> entity.isPresent(CollideComponent.class))
        .findFirst();
  }
}
