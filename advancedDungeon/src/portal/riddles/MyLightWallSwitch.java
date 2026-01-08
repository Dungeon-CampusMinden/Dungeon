package portal.riddles;

import core.Entity;
import portal.lightWall.LightWallFactory;
import portal.lightWall.LightWallSwitch;

public class MyLightWallSwitch extends LightWallSwitch {
  @Override
  public void activate(Entity emitter) {
    LightWallFactory.activate(emitter);
  }

  @Override
  public void deactivate(Entity emitter) {
    LightWallFactory.deactivate(emitter);
  }
}
