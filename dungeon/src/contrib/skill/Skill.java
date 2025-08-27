package contrib.skill;

import core.Entity;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Skill {
  protected static final Logger LOGGER = Logger.getLogger(Skill.class.getSimpleName());

  public static final Skill NONE =
      new Skill() {
        protected void executeSkill(Entity caster) {}
      };

  private String name;
  private long cooldown;
  private Instant lastUsed;
  private Instant nextUsableAt = Instant.now();

  private Map<Resource, Integer> resourceCost;

  public Skill(String name, long cooldown) {
    this.name = name;
    this.cooldown = cooldown;
  }

  private Skill() {}

  protected abstract void executeSkill(Entity caster);

  public final boolean execute(final Entity entity) {
    if (canBeUsedAgain() && checkRessources(entity)) {
      executeSkill(entity);
      consumeResoruces(entity);
      lastUsed = Instant.now();
      activateCoolDown();
      return true;
    }
    return false;
  }

  private boolean checkRessources(Entity caster) {
    // TODO
    return true;
  }

  private void consumeResoruces(Entity caster) {
    // TODO
  }

  public String name() {
    return name;
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
    this.cooldown = newCoolDown;
  }

  /**
   * Returns the cooldown of this skill.
   *
   * @return int The cooldown in milliseconds.
   */
  public long cooldown() {
    return cooldown;
  }

  /**
   * Adds coolDownInMilliSeconds to the time the skill was last used and updates when this skill can
   * be used again.
   */
  private void activateCoolDown() {
    nextUsableAt = lastUsed.plusMillis(cooldown);
  }

  /**
   * Sets the last used time to now.
   *
   * <p>This method is used to reset the cool down of the skill.
   */
  public void setLastUsedToNow() {
    this.lastUsed = Instant.now();
    activateCoolDown();
  }
}
