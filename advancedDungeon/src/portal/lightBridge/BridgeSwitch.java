package portal.lightBridge;

import core.Entity;

public abstract class BridgeSwitch {

  public abstract void activate(Entity bridge);

  public abstract void deactivate(Entity bridge);
}
