package portal.laser;

import core.System;

public class LaserEmitterSystem extends System {

  public LaserEmitterSystem() {
    super(LaserEmitterComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(entity -> {
      LaserComponent lc = entity.fetch(LaserComponent.class).get();
      switch (lc.getCurrentStatus()) {
        case ACTIVATE -> {
          LaserUtil.activate(entity);
          java.lang.System.out.println("Activated a Laser");
          lc.setCurrentStatus(LaserStatus.NONE);
        }
        case DEACTIVATE -> {
          LaserUtil.deactivate(entity);
          java.lang.System.out.println("Deactivated a Laser");
          lc.setCurrentStatus(LaserStatus.NONE);
        }
        case REACTIVATE -> {
          LaserUtil.deactivate(entity);
          LaserUtil.activate(entity);
          java.lang.System.out.println("Reactivated a Laser");
          lc.setCurrentStatus(LaserStatus.NONE);
        }
        case NONE -> {

        }
      }
    });
  }
}
