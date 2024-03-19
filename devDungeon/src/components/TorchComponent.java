package components;

import core.Component;
import level.devlevel.TorchRiddleLevel;

/** This class represents a TorchComponent. It has a state of being lit or not. */
public class TorchComponent implements Component {
  /** The state of the torch. True if the torch is lit, false otherwise. */
  private boolean lit;

  private int value; // This is used for level 1

  /**
   * Constructor for the TorchComponent class.
   *
   * @param lit The initial state of the torch. True if the torch should be lit, false otherwise.
   */
  public TorchComponent(boolean lit) {
    this.lit = lit;
  }

  /**
   * Constructor for the TorchComponent class.
   *
   * @param lit The initial state of the torch. True if the torch should be lit, false otherwise.
   * @param value The value of the torch. (Used for {@link TorchRiddleLevel Level 1}).
   */
  public TorchComponent(boolean lit, int value) {
    this.lit = lit;
    this.value = value;
  }

  /**
   * Toggles the state of the torch. If the torch is lit, it will be turned off. If it is off, it
   * will be turned on.
   */
  public void toggle() {
    this.lit(!this.lit);
  }

  /**
   * Checks if the torch is lit.
   *
   * @return True if the torch is lit, false otherwise.
   */
  public boolean lit() {
    return this.lit;
  }

  /**
   * Sets the state of the torch.
   *
   * @param lit The state to set. True to light the torch, false to turn it off.
   */
  public void lit(boolean lit) {
    this.lit = lit;
  }

  /**
   * Gets the value of the torch.
   *
   * @return The value of the torch.
   */
  public int value() {
    return this.value;
  }

  /**
   * Sets the value of the torch.
   *
   * @param value The value to set.
   */
  public void value(int value) {
    this.value = value;
  }
}
