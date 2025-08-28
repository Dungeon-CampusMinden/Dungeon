package contrib.utils.components.skill;

import core.Entity;
import core.utils.Tuple;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

  public Skill(String name, long cooldown, Tuple<Resource, Integer>... resources) {
    this.name = name;
    this.cooldown = cooldown;
    // Convert the tuple array into a HashMap
    this.resourceCost =
        Arrays.stream(resources)
            .collect(
                Collectors.toMap(
                    tuple -> tuple.a(), // Resource
                    tuple -> tuple.b() // Integer
                    ));
    System.out.println(resourceCost);
  }

  private Skill() {}

  protected abstract void executeSkill(Entity caster);

  public final boolean execute(final Entity entity) {
    if (canBeUsedAgain() && checkResources(entity)) {
      executeSkill(entity);
      consumeResources(entity);
      lastUsed = Instant.now();
      activateCoolDown();
      return true;
    }
    return false;
  }

  private boolean checkResources(Entity caster) {
    // For each required resource, check if the entity has enough
    for (Map.Entry<Resource, Integer> entry : resourceCost.entrySet()) {
      Resource resource = entry.getKey();
      int requiredAmount = entry.getValue();
      float currentAmount = resource.apply(caster);
      if (currentAmount < requiredAmount) {
        return false; // not enough resource
      }
    }
    return true;
  }

  private void consumeResources(Entity caster) {
    for (Map.Entry<Resource, Integer> entry : resourceCost.entrySet()) {
      Resource resource = entry.getKey();
      int amount = entry.getValue();
      resource.consume(caster, amount);
    }
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

  public Map<Resource, Integer> getResourceCost() {
    return new HashMap<>(resourceCost);
  }

  public void setResourceCost(Map<Resource, Integer> newResourceCost) {
    this.resourceCost = new HashMap<>(newResourceCost);
  }

  public void updateResourceCost(Resource resource, int newAmount) {
    if (resourceCost.containsKey(resource)) {
      resourceCost.put(resource, newAmount);
    }
  }

  public void addResource(Resource resource, int amount) {
    resourceCost.put(resource, amount);
  }

  public void removeResource(Resource resource) {
    resourceCost.remove(resource);
  }
}
