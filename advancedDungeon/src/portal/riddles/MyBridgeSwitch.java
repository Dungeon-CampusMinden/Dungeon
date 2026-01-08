package portal.riddles;

import core.Entity;
import portal.lightBridge.BridgeSwitch;
import portal.lightBridge.LightBridgeFactory;

public class MyBridgeSwitch extends BridgeSwitch {
  @Override
  public void activate(Entity emitter) {
    LightBridgeFactory.activate(emitter);
  }

  @Override
  public void deactivate(Entity emitter) {
    LightBridgeFactory.deactivate(emitter);
  }
}
