package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.scene.light.Light;

/**
 * The `LightComponent` class represents a component that holds a reference to a light object.
 * It extends the `Component` class and provides methods to get and set the light.
 */
public class LightComponent extends Component {

  private Light<?> light;

  /**
   * Constructs a new `LightComponent` with the specified light.
   *
   * @param light the light to be associated with this component
   */
  public LightComponent(Light<?> light) {
    super(true);
    this.light = light;
  }

  /**
   * Gets the light associated with this component.
   *
   * @return the light associated with this component
   */
  public Light<?> light() {
    return this.light;
  }

  /**
   * Sets the light associated with this component.
   *
   * @param light the light to be associated with this component
   * @return the updated `LightComponent` instance
   */
  public LightComponent light(Light<?> light) {
    this.light = light;
    return this;
  }

}
