package components;

import core.Component;

/**
 * Allow an associated entity to collect and spend ammunition.
 *
 * <p>If an entity has this component, it can use skills that require ammunition.
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
    this.currentAmmunition = 2;
  }

  /** Pick up one Ammunition. */
  public void collectAmmo() {
    currentAmmunition++;
  }

  /** Spend one Ammunition. */
  public void spendAmmo() {
    currentAmmunition--;
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
   * Get the current amount of ammunition.
   *
   * @return Current amount of ammunition
   */
  public int getCurrentAmmunition() {
    return currentAmmunition;
  }

  /**
   * Set the desired amount of ammunition.
   *
   * @param ammunitionAmount New amount of current ammunition
   */
  public void setCurrentAmmunition(int ammunitionAmount) {
    this.currentAmmunition = ammunitionAmount;
  }
}
