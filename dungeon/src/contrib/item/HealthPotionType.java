package contrib.item;

/** The types of health potions that can be created. Each type has a different healing amount. */
public enum HealthPotionType {
  WEAK(7),
  NORMAL(15),
  GREATER(30);

  /** The amount of health that this type of potion restores when used. */
  private final int healAmount;

  /**
   * Constructs a new health potion type with the specified healing amount.
   *
   * @param healAmount The amount of health that this type of potion restores when used.
   */
  HealthPotionType(int healAmount) {
    this.healAmount = healAmount;
  }

  /**
   * Returns the amount of health that this type of potion restores when used.
   *
   * @return The healing amount of this potion type.
   */
  public int getHealAmount() {
    return this.healAmount;
  }

  /**
   * Returns the name of the health potion type. The name is formatted with the first letter in
   * uppercase and the rest in lowercase.
   *
   * @return The formatted name of the health potion type.
   */
  public String getName() {
    String name = this.name();
    return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
  }
}
