package portal.laser;

import core.System;

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
                case ENTER_CUBE -> {
                  LaserCube.onEnterCube(
                      lc.getOnEnterCube(),
                      lc.getOnEnterLaser(),
                      lc.getOnEnterLaser().fetch(LaserComponent.class).get());

                  lc.setOnEnterCube(null);
                  lc.setOnEnterLaser(null);
                  java.lang.System.out.println("OnCubeEnter");
                  lc.setCurrentStatus(LaserCubeStatus.NONE);
                }
                case LEAVE_CUBE -> {
                  LaserCube.onLeaveCube(lc.getOnLeaveCube(), lc.getOnLeaveLaser());

                  lc.setOnLeaveCube(null);
                  lc.setOnLeaveLaser(null);
                  java.lang.System.out.println("OnCubeLeave");
                  lc.setCurrentStatus(LaserCubeStatus.NONE);
                }
                case NONE -> {}
              }
            });
  }
}
