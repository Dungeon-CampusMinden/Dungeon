package components;

import core.Component;

/**
 * The LeverComponent class implements the Component interface. It represents a lever that can be
 * either on or off.
 */
public class LeverComponent implements Component {
  private boolean isOn;

  /**
   * Constructor for the LeverComponent class.
   *
   * @param isOn A boolean that sets the initial state of the lever.
   */
  public LeverComponent(boolean isOn) {
    this.isOn = isOn;
  }

  /**
   * This method returns the current state of the lever.
   *
   * @return A boolean representing the state of the lever. True if the lever is on, false
   *     otherwise.
   */
  public boolean isOn() {
    return this.isOn;
  }

  /**
   * This method toggles the state of the lever. If the lever is on, it turns it off. If it's off,
   * it turns it on.
   */
  public void toggle() {
    this.isOn = !this.isOn;
  }

  @Override
  public String toString() {
    return "LeverComponent{" + "isOn=" + this.isOn + '}';
  }
}
