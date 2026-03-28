package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;

/**
 * Marks an Entity as "spiky".
 *
 * <p>In combination with the {@link HealthComponent} and {@link CollideComponent}, the {@link
 * SpikyComponent} can be used to apply damage to an entity (like the player) on collision.
 *
 * <p>This component stores information about the {@link DamageType}, the damage amount, so you can
 * create a new {@link contrib.utils.components.health.Damage} object.
 *
 * <p>The cooldown is tracked in seconds so the behavior stays stable across different hosts and
 * frame rates.
 *
 * @see contrib.entities.EntityFactory
 */
public final class SpikyComponent implements Component {
  private final int damageAmount;
  private final DamageType damageType;
  private final float coolDownSeconds;
  private float currentCoolDownSeconds;
  private boolean active = true;

  /**
   * Create a new {@link SpikyComponent}.
   *
   * @param damageAmount The amount of damage that should be caused on collision.
   * @param damageType The type of damage to cause.
   * @param coolDownMs How long to wait before reapplying damage to an entity.
   */
  public SpikyComponent(int damageAmount, final DamageType damageType, long coolDownMs) {
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.coolDownSeconds = Math.max(0L, coolDownMs) / 1000f;
    this.currentCoolDownSeconds = coolDownSeconds;
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
   * Is the cool down expired?
   *
   * @return true if the cool down is expired, false if not.
   */
  public boolean isActive() {
    return this.active() && this.currentCoolDownSeconds <= 0f;
  }

  /** Set the current cool down to the cool down configured in the constructor. */
  public void activateCoolDown() {
    currentCoolDownSeconds = coolDownSeconds;
  }

  /**
   * Reduce the current cool down by the given elapsed time.
   *
   * @param deltaSeconds elapsed time in seconds since the last update
   */
  public void reduceCoolDown(float deltaSeconds) {
    if (deltaSeconds <= 0f) {
      return;
    }
    currentCoolDownSeconds = Math.max(0f, currentCoolDownSeconds - deltaSeconds);
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
