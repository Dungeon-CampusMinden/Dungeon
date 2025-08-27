package contrib.components;

import core.Component;

public class EnergyComponent implements Component {

  private int maxAmount;
  private int currentAmount;
  private float restorePerSecond;

  public EnergyComponent(int maxAmount, int currentAmount, float restorePerSecond) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
    this.restorePerSecond=restorePerSecond;
  }

  // Getters
  public int getMaxAmount() {
    return maxAmount;
  }

  public int getCurrentAmount() {
    return currentAmount;
  }

  // Setters
  public void setMaxAmount(int maxAmount) {
    this.maxAmount = maxAmount;
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  public void setCurrentAmount(int currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  // Increase/decrease max energy
  public void increaseMaxAmount(int amount) {
    setMaxAmount(maxAmount + amount);
  }

  public void decreaseMaxAmount(int amount) {
    setMaxAmount(Math.max(0, maxAmount - amount));
  }

  // Consume/restore current energy
  public boolean consume(int amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true;
    }
    return false;
  }

  public void restore(int amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }

  public float getRestorePerSecond() {
    return restorePerSecond;
  }
  public void setRestorePerSecond(float restorePerSecond) {
    this.restorePerSecond = restorePerSecond;
  }
}
