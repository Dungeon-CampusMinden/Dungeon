package dungine.components;

import de.fwatermann.dungine.ecs.Component;
import org.joml.Vector3f;

/**
 * The `VelocityComponent` class represents the velocity and force applied to an entity in the game.
 * It manages the current velocity, the force applied, and the maximum speed the entity can reach.
 *
 * <p>Key functionalities include:
 *
 * <ul>
 *   <li>Storing the force applied to the entity.
 *   <li>Tracking the current velocity of the entity.
 *   <li>Defining the maximum speed the entity can achieve.
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * VelocityComponent velocityComponent = new VelocityComponent();
 * velocityComponent.force.set(1.0f, 0.0f, 0.0f);
 * velocityComponent.currentVelocity.set(0.5f, 0.0f, 0.0f);
 * }</pre>
 */
public class VelocityComponent extends Component {

  /**
   * The force that is applied in the next iteration of the {@link dungine.systems.VelocitySystem}.
   */
  public Vector3f force = new Vector3f();

  /** The current velocity of the entity. */
  public Vector3f currentVelocity = new Vector3f();

  /** The maximum speed of the entity. */
  public float maxSpeed = 3.0f;

  /** Create a new VelocityComponent. */
  public VelocityComponent() {
    super(false);
  }
}
