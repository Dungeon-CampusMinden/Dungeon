package contrib.components;

import core.Component;

/**
 * A component representing a mana pool for an entity.
 *
 * <p>This component stores and manages mana values, including the maximum amount, current amount,
 * and the mana restoration rate per second. It provides methods for consuming, restoring, and
 * modifying mana.
 */
public class ManaComponent implements Component {

  /** The maximum amount of mana the entity can have. */
  private float maxAmount;

  /** The current amount of mana available to the entity. */
  private float currentAmount;

  /** The mana restored per second. */
  private float restorePerSecond;

  /**
   * Creates a new {@code ManaComponent} with the given maximum mana, initial mana, and mana
   * restoration rate.
   *
   * @param maxAmount the maximum mana capacity
   * @param currentAmount the initial mana amount (capped at {@code maxAmount})
   * @param restorePerSecond the amount of mana restored per second
   */
  public ManaComponent(float maxAmount, float currentAmount, float restorePerSecond) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
    this.restorePerSecond = restorePerSecond;
  }

  /**
   * Returns the maximum mana capacity.
   *
   * @return the maximum amount of mana
   */
  public float maxAmount() {
    return maxAmount;
  }

  /**
   * Returns the current mana amount.
   *
   * @return the current mana value
   */
  public float currentAmount() {
    return currentAmount;
  }

  /**
   * Sets a new maximum mana capacity.
   *
   * <p>If the current mana exceeds the new maximum, it will be reduced to match the maximum.
   *
   * @param maxAmount the new maximum mana
   */
  public void maxAmount(float maxAmount) {
    this.maxAmount = maxAmount;
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  /**
   * Sets the current mana amount.
   *
   * <p>The value is capped so that it does not exceed the maximum mana.
   *
   * @param currentAmount the new current mana
   */
  public void currentAmount(float currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  /**
   * Increases the maximum mana capacity by the given amount.
   *
   * @param amount the amount to increase
   */
  public void increaseMaxAmount(float amount) {
    maxAmount(maxAmount + amount);
  }

  /**
   * Decreases the maximum mana capacity by the given amount.
   *
   * <p>The new maximum will not fall below zero.
   *
   * @param amount the amount to decrease
   */
  public void decreaseMaxAmount(float amount) {
    maxAmount(Math.max(0, maxAmount - amount));
  }

  /**
   * Consumes a given amount of mana if enough is available.
   *
   * @param amount the amount of mana to consume
   * @return {@code true} if the mana was successfully consumed, {@code false} if there was not
   *     enough mana
   */
  public boolean consume(float amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true;
    }
    return false;
  }

  /**
   * Restores a given amount of mana to the pool.
   *
   * <p>The current mana will not exceed the maximum mana capacity.
   *
   * @param amount the amount of mana to restore
   */
  public void restore(float amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }

  /**
   * Returns the mana restored per second.
   *
   * @return the restoration rate per second
   */
  public float restorePerSecond() {
    return restorePerSecond;
  }

  /**
   * Sets the mana restored per second.
   *
   * @param restorePerSecond the new restoration rate
   */
  public void restorePerSecond(float restorePerSecond) {
    this.restorePerSecond = restorePerSecond;
  }
}
