package produsAdvanced.abstraction.portals.components;

import core.Component;

/** Holder component for the laser receiver. */
public class LaserReceiverComponent implements Component {

  private boolean active = false;

  /**
   * Sets the state of the laser receiver.
   *
   * @param active true if the receiver receives laser, otherwise false.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Returns the current state of the laser receiver.
   *
   * @return true if active, otherwise false.
   */
  public boolean isActive() {
    return active;
  }
}
