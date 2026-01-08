package portal.lightWall;

import core.Entity;

public abstract class LightWallSwitch {

  public abstract void activate(Entity wall);

  public abstract void deactivate(Entity wall);
}
