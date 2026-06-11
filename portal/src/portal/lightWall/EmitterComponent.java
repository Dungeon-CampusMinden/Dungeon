package portal.lightWall;

import core.Component;

/** Component representing an emitter. */
public class EmitterComponent implements Component {

  private boolean active;

  /** Creates a new emitter for light walls. */
  public EmitterComponent() {}

  /**
   * Sets the active state of the Emitter.
   *
   * @param active true if active, otherwise false.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Returns the active state of the Emitter.
   *
   * @return current state of the emitter.
   */
  public boolean isActive() {
    return active;
  }
}
