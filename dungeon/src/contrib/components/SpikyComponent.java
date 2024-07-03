package contrib.components;

import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Component;

/**
 * Marks an Entity as "spiky".
 *
 * <p>In combination with the {@link HealthComponent} and {@link CollideComponent}, the {@link
 * SpikyComponent} can be used to apply damage to an entity (like the hero) on collision.
 *
 * <p>This component stores information about the {@link DamageType}, the damage amount, so you can
 * create a new {@link contrib.utils.components.health.Damage} object.
 *
 * <p>This component also stores a cooldown (in frames) so you can prevent continuous damage. The
 * cooldown will be reduced by the {@link contrib.systems.SpikeSystem}.
 *
 * <p>To apply damage on collision, first create an entity (like a monster) with a {@link
 * CollideComponent} and {@link SpikyComponent}. Also create an entity (like the hero) that has a
 * {@link CollideComponent} and {@link HealthComponent}. Now implement the damage calculation. In
 * the hero's collision method, check whether the other entity (the monster) implements the {@link
 * SpikyComponent} and whether the cooldown has expired ({@link #isActive()}). If so, use {@link
 * HealthComponent#receiveHit(Damage)} to deal damage to the hero. Remember to activate the cooldown
 * of this component using {@link #activateCoolDown()}.
 *
 * <p>Use {@link #damageAmount} and {@link #damageType} to get the damage information.
 *
 * @see contrib.entities.EntityFactory
 */
public final class SpikyComponent implements Component {
  private final int damageAmount;
  private final DamageType damageType;
  private final int coolDown;
  private int currentCoolDown;
  private boolean active = true;

  /**
   * Create a new {@link SpikyComponent}.
   *
   * @param damageAmount The amount of damage that should be caused on collision.
   * @param damageType The type of damage to cause.
   * @param coolDown How many frames to wait before reapplying damage to an entity.
   */
  public SpikyComponent(int damageAmount, final DamageType damageType, int coolDown) {
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.coolDown = coolDown;
    this.currentCoolDown = coolDown;
  }

  /**
   * Amount of damage to cause.
   *
   * @return amount of damage.
   */
  public int damageAmount() {
    return damageAmount;
  }

  /**
   * Type of damage to cause.
   *
   * @return the type of damage.
   */
  public DamageType damageType() {
    return damageType;
  }

  /**
   * Is the cool down 0?
   *
   * @return true if the cool down is 0, false if not.
   */
  public boolean isActive() {
    return this.active() && this.currentCoolDown == 0;
  }

  /** Set the current cool down to the cool down configured in the constructor. */
  public void activateCoolDown() {
    currentCoolDown = coolDown;
  }

  /** Reduce the current cool down by one. */
  public void reduceCoolDown() {
    currentCoolDown = Math.max(0, currentCoolDown - 1);
  }

  /**
   * Is the spiky component active?
   *
   * @return true if the spiky component is active, false if not.
   */
  public boolean active() {
    return this.active;
  }

  /**
   * Set the active state of the spiky component.
   *
   * @param active The new active state.
   */
  public void active(boolean active) {
    this.active = active;
  }
}
