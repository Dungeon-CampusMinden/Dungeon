package contrib.components;

import core.Component;

/**
 * A component representing an stamina pool for an entity.
 *
 * <p>This component manages stamina values, including the maximum amount, current amount, and the
 * natural restoration rate per second. It provides methods for consuming, restoring, and modifying
 * energy.
 */
public class StaminaComponent implements Component {

  /** The maximum amount of stamina the entity can have. */
  private float maxAmount;

  /** The current amount of stamina available to the entity. */
  private float currentAmount;

  /** The amount of stamina restored per second. */
  private float restorePerSecond;

  /**
   * Creates a new {@code EnergyComponent} with the given maximum stamina, initial stamina, and
   * restoration rate.
   *
   * @param maxAmount the maximum stamina capacity
   * @param currentAmount the initial stamina amount (capped at {@code maxAmount})
   * @param restorePerSecond the amount of stamina restored per second
   */
  public StaminaComponent(float maxAmount, float currentAmount, float restorePerSecond) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
    this.restorePerSecond = restorePerSecond;
  }

  /**
   * Returns the maximum stamina capacity.
   *
   * @return the maximum amount of stamina
   */
  public float maxAmount() {
    return maxAmount;
  }

  /**
   * Returns the current stamina amount.
   *
   * @return the current stamina value
   */
  public float currentAmount() {
    return currentAmount;
  }

  /**
   * Sets a new maximum stamina capacity.
   *
   * <p>If the current stamina exceeds the new maximum, it will be reduced to match the maximum.
   *
   * @param maxAmount the new maximum stamina
   */
  public void maxAmount(float maxAmount) {
    this.maxAmount = maxAmount;
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  /**
   * Sets the current stamina amount.
   *
   * <p>The value is capped so that it does not exceed the maximum stamina.
   *
   * @param currentAmount the new current stamina
   */
  public void currentAmount(float currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  /**
   * Increases the maximum stamina capacity by the given amount.
   *
   * @param amount the amount to increase
   */
  public void increaseMaxAmount(float amount) {
    maxAmount(maxAmount + amount);
  }

  /**
   * Decreases the maximum stamina capacity by the given amount.
   *
   * <p>The new maximum will not fall below zero.
   *
   * @param amount the amount to decrease
   */
  public void decreaseMaxAmount(float amount) {
    maxAmount(Math.max(0, maxAmount - amount));
  }

  /**
   * Consumes a given amount of stamina if enough is available.
   *
   * @param amount the amount of stamina to consume
   * @return {@code true} if the stamina was successfully consumed, {@code false} if there was not
   *     enough stamina
   */
  public boolean consume(float amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true;
    }
    return false;
  }

  /**
   * Restores a given amount of stamina to the pool.
   *
   * <p>The current stamina will not exceed the maximum stamina capacity.
   *
   * @param amount the amount of stamina to restore
   */
  public void restore(float amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }

  /**
   * Returns the stamina restored per second.
   *
   * @return the restoration rate per second
   */
  public float restorePerSecond() {
    return restorePerSecond;
  }

  /**
   * Sets the stamina restored per second.
   *
   * @param restorePerSecond the new restoration rate
   */
  public void restorePerSecond(float restorePerSecond) {
    this.restorePerSecond = restorePerSecond;
  }
}
