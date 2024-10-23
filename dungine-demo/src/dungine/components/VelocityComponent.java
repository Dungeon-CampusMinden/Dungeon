package dungine.components;

import de.fwatermann.dungine.ecs.Component;
import org.joml.Vector3f;

public class VelocityComponent extends Component {

  public Vector3f force = new Vector3f();
  public Vector3f currentVelocity = new Vector3f();
  public float maxSpeed = 3.0f;

  public VelocityComponent() {
    super(false);
  }
}
