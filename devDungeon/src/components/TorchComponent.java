package components;

import core.Component;

/** This class represents a TorchComponent. It has a state of being lit or not. */
public class TorchComponent implements Component {
  /** The state of the torch. True if the torch is lit, false otherwise. */
  private boolean lit;

  /**
   * Constructor for the TorchComponent class. Initializes the state of the torch with the provided
   * value.
   *
   * @param lit The initial state of the torch. True if the torch should be lit, false otherwise.
   */
  public TorchComponent(boolean lit) {
    this.lit = lit;
  }

  /**
   * Toggles the state of the torch. If the torch is lit, it will be turned off. If it is off, it
   * will be turned on.
   */
  public void toggle() {
    lit = !lit;
  }

  /**
   * Checks if the torch is lit.
   *
   * @return True if the torch is lit, false otherwise.
   */
  public boolean lit() {
    return lit;
  }

  /**
   * Sets the state of the torch.
   *
   * @param lit The state to set. True to light the torch, false to turn it off.
   */
  public void lit(boolean lit) {
    this.lit = lit;
  }
}
