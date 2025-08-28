package contrib.components;

import core.Component;

/**
 * A component representing an energy pool for an entity.
 *
 * <p>This component manages energy values, including the maximum amount, current amount, and the
 * natural restoration rate per second. It provides methods for consuming, restoring, and modifying
 * energy.
 */
public class EnergyComponent implements Component {

  /** The maximum amount of energy the entity can have. */
  private float maxAmount;

  /** The current amount of energy available to the entity. */
  private float currentAmount;

  /** The amount of energy restored per second. */
  private float restorePerSecond;

  /**
   * Creates a new {@code EnergyComponent} with the given maximum energy, initial energy, and
   * restoration rate.
   *
   * @param maxAmount the maximum energy capacity
   * @param currentAmount the initial energy amount (capped at {@code maxAmount})
   * @param restorePerSecond the amount of energy restored per second
   */
  public EnergyComponent(float maxAmount, float currentAmount, float restorePerSecond) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
    this.restorePerSecond = restorePerSecond;
  }

  /**
   * Returns the maximum energy capacity.
   *
   * @return the maximum amount of energy
   */
  public float maxAmount() {
    return maxAmount;
  }

  /**
   * Returns the current energy amount.
   *
   * @return the current energy value
   */
  public float currentAmount() {
    return currentAmount;
  }

  /**
   * Sets a new maximum energy capacity.
   *
   * <p>If the current energy exceeds the new maximum, it will be reduced to match the maximum.
   *
   * @param maxAmount the new maximum energy
   */
  public void maxAmount(float maxAmount) {
    this.maxAmount = maxAmount;
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  /**
   * Sets the current energy amount.
   *
   * <p>The value is capped so that it does not exceed the maximum energy.
   *
   * @param currentAmount the new current energy
   */
  public void currentAmount(float currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  /**
   * Increases the maximum energy capacity by the given amount.
   *
   * @param amount the amount to increase
   */
  public void increaseMaxAmount(float amount) {
    maxAmount(maxAmount + amount);
  }

  /**
   * Decreases the maximum energy capacity by the given amount.
   *
   * <p>The new maximum will not fall below zero.
   *
   * @param amount the amount to decrease
   */
  public void decreaseMaxAmount(float amount) {
    maxAmount(Math.max(0, maxAmount - amount));
  }

  /**
   * Consumes a given amount of energy if enough is available.
   *
   * @param amount the amount of energy to consume
   * @return {@code true} if the energy was successfully consumed, {@code false} if there was not
   *     enough energy
   */
  public boolean consume(float amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true;
    }
    return false;
  }

  /**
   * Restores a given amount of energy to the pool.
   *
   * <p>The current energy will not exceed the maximum energy capacity.
   *
   * @param amount the amount of energy to restore
   */
  public void restore(float amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }

  /**
   * Returns the energy restored per second.
   *
   * @return the restoration rate per second
   */
  public float restorePerSecond() {
    return restorePerSecond;
  }

  /**
   * Sets the energy restored per second.
   *
   * @param restorePerSecond the new restoration rate
   */
  public void restorePerSecond(float restorePerSecond) {
    this.restorePerSecond = restorePerSecond;
  }
}
