package contrib.components;

import core.Component;

public class ManaComponent implements Component {

  private int maxAmount;
  private int currentAmount;

  public ManaComponent(int maxAmount, int currentAmount) {
    this.maxAmount = maxAmount;
    this.currentAmount = Math.min(currentAmount, maxAmount);
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
    // Ensure currentAmount does not exceed new max
    if (currentAmount > maxAmount) {
      currentAmount = maxAmount;
    }
  }

  public void setCurrentAmount(int currentAmount) {
    this.currentAmount = Math.min(currentAmount, maxAmount);
  }

  // Increase/decrease max mana
  public void increaseMaxAmount(int amount) {
    setMaxAmount(maxAmount + amount);
  }

  public void decreaseMaxAmount(int amount) {
    setMaxAmount(Math.max(0, maxAmount - amount));
  }

  // Consume/restore current mana
  public boolean consume(int amount) {
    if (amount <= currentAmount) {
      currentAmount -= amount;
      return true; // Successfully consumed
    }
    return false; // Not enough mana
  }

  public void restore(int amount) {
    currentAmount = Math.min(currentAmount + amount, maxAmount);
  }
}
