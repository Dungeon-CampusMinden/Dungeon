package portal.laser.systems;

import core.System;
import portal.laser.LaserStatus;
import portal.laser.LaserUtil;
import portal.laser.components.LaserComponent;
import portal.laser.components.LaserEmitterComponent;

public class LaserEmitterSystem extends System {

  public LaserEmitterSystem() {
    super(LaserEmitterComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .forEach(
            entity -> {
              LaserComponent lc = entity.fetch(LaserComponent.class).get();
              switch (lc.getCurrentStatus()) {
                case ACTIVATE -> {
                  LaserUtil.activate(entity);
                  lc.setCurrentStatus(LaserStatus.NONE);
                }
                case DEACTIVATE -> {
                  LaserUtil.deactivate(entity);
                  lc.setCurrentStatus(LaserStatus.NONE);
                }
                case REACTIVATE -> {
                  LaserUtil.deactivate(entity);
                  LaserUtil.activate(entity);
                  lc.setCurrentStatus(LaserStatus.NONE);
                }
                case NONE -> {}
              }
            });
  }
}
