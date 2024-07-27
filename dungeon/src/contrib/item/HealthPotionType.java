package contrib.item;

/** The types of health potions that can be created. Each type has a different healing amount. */
public enum HealthPotionType {
  /** A weak health potion that restores a small amount of health. */
  WEAK(7),
  /** A normal health potion that restores a moderate amount of health. */
  NORMAL(15),
  /** A greater health potion that restores a large amount of health. */
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

  /**
   * Generates a random health potion type based on the following weights.
   *
   * <ul>
   *   <li>75%: HealthPotionType.WEAK
   *   <li>20%: HealthPotionType.NORMAL
   *   <li>5%: HealthPotionType.GREATER
   * </ul>
   *
   * @return A randomly selected health potion type
   */
  public static HealthPotionType randomType() {
    float randomValue = Item.RANDOM.nextFloat();
    if (randomValue < 0.75f) return WEAK;
    if (randomValue < 0.95f) return NORMAL;
    return GREATER;
  }
}
