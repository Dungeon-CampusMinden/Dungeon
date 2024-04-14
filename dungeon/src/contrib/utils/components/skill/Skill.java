package contrib.utils.components.skill;

import core.Entity;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Skill implements the base functionality of every skill.
 *
 * <p>The base functionality consists of checking if the cool down expired, executing the specific
 * functionality of the skill, saving the time when the skill was last used and (re)activate the
 * cool down timer.
 *
 * <p>{@link #canBeUsedAgain()} checks if the time between the last use and now is enough to use the
 * skill again.
 *
 * <p>The {@link #activateCoolDown}-Method adds the specified cool down time to the time the skill
 * was last used. While the cool down is active, the skill can not be used again.
 */
public class Skill {

  private final Consumer<Entity> skillFunction;
  private long coolDownInMilliSeconds;
  private Instant lastUsed;
  private Instant nextUsableAt = Instant.now();

  /**
   * Create a new {@link Skill}.
   *
   * @param skillFunction Functionality of the skill.
   * @param coolDownInMilliSeconds The time that needs to pass between use of the skill and the next
   *     possible use of the skill.
   */
  public Skill(final Consumer<Entity> skillFunction, final long coolDownInMilliSeconds) {
    this.skillFunction = skillFunction;
    this.coolDownInMilliSeconds = coolDownInMilliSeconds;
  }

  /**
   * Executes the method of this skill, saves the time the skill was last used and updates when it
   * can be used again.
   *
   * <p>If the skill was used, the cool down will be set.
   *
   * @param entity The entity which uses this skill.
   */
  public void execute(final Entity entity) {
    if (canBeUsedAgain()) {
      skillFunction.accept(entity);
      lastUsed = Instant.now();
      activateCoolDown();
    }
  }

  /**
   * Checks if the cool down has passed and the skill can be used again.
   *
   * @return true if the specified time (coolDownInSeconds) has passed.
   */
  public boolean canBeUsedAgain() {
    // check if the cool down is active, return the negated result (this avoids some problems in
    // nano-sec range)
    return !(Duration.between(Instant.now(), nextUsableAt).toMillis() > 0);
  }

  /**
   * Sets the cooldown of this skill.
   *
   * @param newCoolDown The new cooldown in milliseconds.
   */
  public void cooldown(long newCoolDown) {
    this.coolDownInMilliSeconds = newCoolDown;
  }

  /**
   * Returns the cooldown of this skill.
   *
   * @return int The cooldown in milliseconds.
   */
  public long cooldown() {
    return this.coolDownInMilliSeconds;
  }

  /**
   * Adds coolDownInMilliSeconds to the time the skill was last used and updates when this skill can
   * be used again.
   */
  private void activateCoolDown() {
    nextUsableAt = lastUsed.plusMillis(coolDownInMilliSeconds);
  }

  /**
   * Sets the last used time to now.
   *
   * <p>This method is used to reset the cool down of the skill.
   */
  public void setLastUsedToNow() {
    this.lastUsed = Instant.now();
    this.activateCoolDown();
  }
}
