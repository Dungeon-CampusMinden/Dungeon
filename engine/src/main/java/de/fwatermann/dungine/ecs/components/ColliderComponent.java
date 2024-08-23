package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.physics.Collider;

public class ColliderComponent extends Component {

  private Collider collider;

  public ColliderComponent() {
    super(false);
  }

  public Collider collider() {
    return this.collider;
  }

  public ColliderComponent collider(Collider collider) {
    this.collider = collider;
    return this;
  }

}
