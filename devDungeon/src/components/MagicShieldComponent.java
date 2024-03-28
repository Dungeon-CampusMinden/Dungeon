package components;

import core.Component;

/**
 * MagicShieldComponent is a class that implements the Component interface. It is responsible for
 * managing the magic shield of entities.
 *
 * <p>A magic shield is a shield that can absorb a certain amount of damage before it gets depleted.
 * Once the shield is depleted, it needs to recharge before it can be used again. The
 * MagicShieldComponent checks if the shield is depleted and recharges it if it can.
 *
 * @see systems.MagicShieldSystem MagicShieldSystem
 */
public class MagicShieldComponent implements Component {
  private static final int DEFAULT_SHIELD_STRENGTH = 2; // The default shield strength
  private static final long DEFAULT_COOLDOWN = 15 * 1000; // in seconds
  private final int shieldStrength; // The amount of damage the shield can absorb
  private final long cooldown; // The time it takes for the shield to recharge
  private int currentStrength; // The current strength of the shield
  private long depletionAt; // The time at which the shield got depleted

  /**
   * Constructor for the MagicShieldComponent class. It initializes the shield with the default
   * shield strength and cooldown.
   *
   * @see MagicShieldComponent#DEFAULT_SHIELD_STRENGTH
   * @see MagicShieldComponent#DEFAULT_COOLDOWN
   */
  public MagicShieldComponent() {
    this(DEFAULT_SHIELD_STRENGTH, DEFAULT_COOLDOWN);
  }

  /**
   * Constructor for the MagicShieldComponent class. It initializes the shield with the given shield
   * strength and cooldown.
   *
   * @param shieldStrength The amount of damage the shield can absorb.
   * @param cooldown The time it takes for the shield to recharge.
   */
  public MagicShieldComponent(int shieldStrength, long cooldown) {
    this.shieldStrength = shieldStrength;
    this.cooldown = cooldown;
    this.currentStrength = shieldStrength;
    this.depletionAt = 0;
  }

  /**
   * Method to check if the shield is depleted.
   *
   * @return True if the shield is depleted, false otherwise.
   */
  public boolean isDepleted() {
    return this.currentStrength <= 0;
  }

  /**
   * Method to hit the shield with a certain amount of damage. If the shield is not depleted, the
   * damage is subtracted from the shield strength. If the shield is depleted, the depletion time is
   * set to the current time. If the damage is negative, an IllegalArgumentException is thrown.
   *
   * @param damage The amount of damage to hit the shield with.
   * @throws IllegalArgumentException If the damage is negative.
   */
  public void hit(int damage) {
    if (this.isDepleted()) {
      return;
    }
    if (damage < 0) {
      throw new IllegalArgumentException("Damage cannot be negative.");
    }

    this.currentStrength -= damage;
    if (this.isDepleted()) {
      this.depletionAt = System.currentTimeMillis();
    }
  }

  /**
   * Method to check if the shield can be recharged. The shield can be recharged if it is depleted
   * and the time since it got depleted is greater than the cooldown time.
   *
   * @return True if the shield can be recharged, false otherwise.
   */
  public boolean canBeRecharged() {
    return this.isDepleted() && System.currentTimeMillis() - this.depletionAt >= this.cooldown;
  }

  /**
   * Method to recharge the shield. It sets the current strength of the shield to the shield
   * strength.
   */
  public void recharge() {
    if (this.canBeRecharged()) {
      this.currentStrength = this.shieldStrength;
    }
  }
}
