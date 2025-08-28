package contrib.components;

import core.Component;

public class EnergyComponent implements Component {

  private float maxAmount;
  private float currentAmount;
  private float restorePerSecond;

  public EnergyComponent(float maxAmount, float currentAmount, float restorePerSecond) {
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
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  public void setCurrentAmount(float currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  // Increase/decrease max energy
  public void increaseMaxAmount(float amount) {
    setMaxAmount(maxAmount + amount);
  }

  public void decreaseMaxAmount(float amount) {
    setMaxAmount(Math.max(0, maxAmount - amount));
  }

  // Consume/restore current energy
  public boolean consume(float amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true;
    }
    return false;
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
