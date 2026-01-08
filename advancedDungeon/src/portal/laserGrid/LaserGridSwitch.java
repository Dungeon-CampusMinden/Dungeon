package portal.laserGrid;

import core.Entity;

public abstract class LaserGridSwitch {

  public abstract void activate(Entity[] grid);

  public abstract void deactivate(Entity[] grid);
}
