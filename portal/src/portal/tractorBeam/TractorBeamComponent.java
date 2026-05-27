package portal.tractorBeam;

import core.Component;
import core.Entity;
import core.utils.Vector2;
import java.util.HashMap;

/** Component represents a tractor beam that can be extended and trimmed. */
public class TractorBeamComponent implements Component {

  private boolean active = false;
  private boolean reversed = false;
  private Vector2 forceToApply = Vector2.ZERO;
  private Vector2 reversedForceToApply = Vector2.ZERO;

  /** Store old forces for the Entities. */
  public HashMap<Entity, Vector2> oldForces = new HashMap<>();

  /** Constructs a TractorBeamComponent so it can be extended and trimmed. */
  public TractorBeamComponent() {}

  /**
   * Shows the current status of the "active" status.
   *
   * @return whether the beam is currently active or not.
   */
  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Shows the current status of the "reversed" status.
   *
   * @return whether the beam is currently reversed or not.
   */
  public boolean isReversed() {
    return reversed;
  }

  public void setReversed(boolean reversed) {
    this.reversed = reversed;
  }

  /**
   * Returns the force that is applied to entities inside the beam when the beam operates in normal
   * mode.
   *
   * @return the force vector applied to entities in the beam
   */
  public Vector2 forceToApply() {
    return forceToApply;
  }

  /**
   * Sets the force that is applied to entities inside the beam when the beam operates in normal
   * mode.
   *
   * @param forceToApply the force vector to apply
   */
  public void forceToApply(Vector2 forceToApply) {
    this.forceToApply = forceToApply;
  }

  /**
   * Returns the force that is applied to entities inside the beam when the beam operates in
   * reversed mode.
   *
   * @return the reversed force vector applied to entities in the beam
   */
  public Vector2 reversedForceToApply() {
    return reversedForceToApply;
  }

  /**
   * Sets the force that is applied to entities inside the beam when the beam operates in reversed
   * mode.
   *
   * @param reversedForceToApply the force vector to apply in reversed mode
   */
  public void reversedForceToApply(Vector2 reversedForceToApply) {
    this.reversedForceToApply = reversedForceToApply;
  }
}
