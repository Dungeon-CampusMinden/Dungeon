package dungine.state.ingame.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.camera.Camera;
import org.joml.Vector3f;

public class CameraComponent extends Component {

  private Camera<?> camera;
  private final Vector3f offset = new Vector3f();
  private float offsetLength = 5.0f;

  public CameraComponent(Camera<?> camera, Vector3f offset) {
    super(false);
    this.camera = camera;
    this.offset.set(offset).normalize();
  }

  public Camera<?> camera() {
    return this.camera;
  }

  public CameraComponent camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }

  public Vector3f offset() {
    return this.offset;
  }

  public float offsetLength() {
    return this.offsetLength;
  }

  public CameraComponent offsetLength(float offsetLength) {
    this.offsetLength = offsetLength;
    return this;
  }

}
