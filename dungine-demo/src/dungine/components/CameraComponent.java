package dungine.components;

import de.fwatermann.dungine.ecs.Component;

/** A component that marks an entity as the focus point of the camera. */
public class CameraComponent extends Component {

  /** Constructs a new CameraComponent object. */
  public CameraComponent() {
    super(false);
  }
}
