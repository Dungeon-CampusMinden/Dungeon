package components;

import core.Component;

/**
 * Allow an associated entity to collect and spend ammunition.
 *
 * <p>Use this component to store and manage ammunition, represented by a simple integer counter.
 */
public final class AmmunitionComponent implements Component {
  private int currentAmmunition;

  /**
   * Create a new AmmunitionComponent.
   *
   * @param currentAmmunition Current amount of ammunition
   */
  public AmmunitionComponent(int currentAmmunition) {
    this.currentAmmunition = currentAmmunition;
  }

  /** Create a AmmunitionComponent with default values. */
  public AmmunitionComponent() {
    this.currentAmmunition = 0;
  }

  /**
   * Pick up one Ammunition.
   *
   * @return current ammunition amount after increase.
   */
  public int collectAmmo() {
    currentAmmunition++;
    return currentAmmunition;
  }

  /** Spend one Ammunition. */
  public void spendAmmo() {
    currentAmmunition = Math.max(0, currentAmmunition - 1);
  }

  /**
   * Check for remaining ammunition.
   *
   * @return True if there is any ammunition left
   */
  public boolean checkAmmunition() {
    return currentAmmunition > 0;
  }

  /**
   * Reset the current amount of ammunition to zero.
   *
   * @return the new ammunition value (0)
   */
  public int resetCurrentAmmunition() {
    return currentAmmunition(0);
  }

  /**
   * Get the current amount of ammunition.
   *
   * @return Current amount of ammunition
   */
  public int currentAmmunition() {
    return currentAmmunition;
  }

  /**
   * Set the desired amount of ammunition.
   *
   * @param ammunitionAmount New amount of current ammunition
   * @return the new ammunition value
   */
  public int currentAmmunition(int ammunitionAmount) {
    currentAmmunition = ammunitionAmount;
    return currentAmmunition;
  }
}
