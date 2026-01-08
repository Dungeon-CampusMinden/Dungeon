package portal.riddles;

import core.Entity;
import portal.laserGrid.LaserGridSwitch;
import portal.util.Tools;

public class MyLaserGridSwitch extends LaserGridSwitch {
  @Override
  public void activate(Entity[] grid) {
    for (Entity laser : grid) Tools.getLaserGridComponent(laser).activate();
  }

  @Override
  public void deactivate(Entity[] grid) {
    for (Entity laser : grid) Tools.getLaserGridComponent(laser).deactivate();
  }
}
