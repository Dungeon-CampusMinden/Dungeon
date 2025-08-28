package contrib.components;

import core.Component;

public class ManaComponent implements Component {

  private float maxAmount;
  private float currentAmount;
  private float restorePerSecond;

  public ManaComponent(float maxAmount, float currentAmount, float restorePerSecond) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
    this.restorePerSecond = restorePerSecond;
  }

  // Getters
  public float getMaxAmount() {
    return maxAmount;
  }

  public float getCurrentAmount() {
    return currentAmount;
  }

  // Setters
  public void setMaxAmount(float maxAmount) {
    this.maxAmount = maxAmount;
    // Ensure currentAmount does not exceed new max
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  public void setCurrentAmount(float currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  // Increase/decrease max mana
  public void increaseMaxAmount(float amount) {
    setMaxAmount(maxAmount + amount);
  }

  public void decreaseMaxAmount(float amount) {
    setMaxAmount(Math.max(0, maxAmount - amount));
  }

  // Consume/restore current mana
  public boolean consume(float amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true; // Successfully consumed
    }
    return false; // Not enough mana
  }

  public void restore(float amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }

  public float getRestorePerSecond() {
    return restorePerSecond;
  }

  public void setRestorePerSecond(float restorePerSecond) {
    this.restorePerSecond = restorePerSecond;
  }
}
