package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.scene.light.Light;

public class LightComponent extends Component {

  private Light<?> light;

  public LightComponent(Light<?> light) {
    super(true);
    this.light = light;
  }

  public Light<?> light() {
    return this.light;
  }

  public LightComponent light(Light<?> light) {
    this.light = light;
    return this;
  }

}
