package contrib.utils.components.skill;

import core.Entity;
import core.utils.Tuple;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents an abstract skill that can be used by an {@link Entity}.
 *
 * <p>A skill has a name, a cooldown period, and a set of resource costs (such as mana or energy)
 * required to execute it. Subclasses must implement the {@link #executeSkill(Entity)} method, which
 * defines the behavior of the skill when executed.
 *
 * <p>This class also provides a static {@link #NONE} instance that represents a no-op skill.
 */
public abstract class Skill {

  /** Random Instance to use for skills. */
  public static final Random RANDOM = new Random();

  /** Logger for skill-related events. */
  protected static final Logger LOGGER = Logger.getLogger(Skill.class.getSimpleName());

  /** A placeholder skill that does nothing when executed. */
  public static final Skill NONE =
      new Skill() {
        @Override
        protected void executeSkill(Entity caster) {}
      };

  /** The name of the skill. */
  protected String name;

  /** The cooldown duration in milliseconds. */
  private long cooldown;

  /** The last time the skill was used. */
  private Instant lastUsed;

  /** The next time at which this skill can be used again. */
  private Instant nextUsableAt = Instant.now();

  /** The resource cost required to execute this skill. */
  private Map<Resource, Integer> resourceCost;

  /**
   * Creates a new skill with the given parameters.
   *
   * @param name the name of the skill
   * @param cooldown the cooldown in milliseconds
   * @param resources the resources and their required amounts, provided as {@link Tuple}s
   */
  @SafeVarargs
  public Skill(String name, long cooldown, Tuple<Resource, Integer>... resources) {
    this.name = name;
    this.cooldown = cooldown;
    this.resourceCost =
        Arrays.stream(resources)
            .collect(
                Collectors.toMap(
                    Tuple::a, // Resource
                    Tuple::b // Integer
                    ));
  }

  /** Private constructor for creating special skills such as {@link #NONE}. */
  private Skill() {
    resourceCost = new HashMap<>();
  }

  /**
   * Defines the behavior of the skill when executed.
   *
   * <p>Subclasses must implement this method to specify what happens when the skill is successfully
   * triggered.
   *
   * @param caster the entity using the skill
   */
  protected abstract void executeSkill(Entity caster);

  /**
   * Attempts to execute the skill for the given entity.
   *
   * <p>The execution is successful if:
   *
   * <ul>
   *   <li>the skill is not on cooldown ({@link #canBeUsedAgain()} returns {@code true}), and
   *   <li>the entity has enough resources ({@link #checkResources(Entity)} returns {@code true})
   * </ul>
   *
   * <p>If successful, the skill behavior is executed, resources are consumed, and the cooldown is
   * activated.
   *
   * @param entity the entity attempting to use the skill
   * @return {@code true} if the skill was executed, {@code false} otherwise
   */
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

  /**
   * Checks whether the given entity has enough resources to use this skill.
   *
   * @param caster the entity attempting to use the skill
   * @return {@code true} if all resource requirements are met, {@code false} otherwise
   */
  private boolean checkResources(Entity caster) {
    for (Map.Entry<Resource, Integer> entry : resourceCost.entrySet()) {
      Resource resource = entry.getKey();
      int requiredAmount = entry.getValue();
      float currentAmount = resource.apply(caster);
      if (currentAmount < requiredAmount) {
        return false;
      }
    }
    return true;
  }

  /**
   * Consumes the required resources from the given entity when the skill is used.
   *
   * @param caster the entity using the skill
   */
  private void consumeResources(Entity caster) {
    for (Map.Entry<Resource, Integer> entry : resourceCost.entrySet()) {
      Resource resource = entry.getKey();
      int amount = entry.getValue();
      resource.consume(caster, amount);
    }
  }

  /**
   * Returns the name of the skill.
   *
   * @return the skill name
   */
  public String name() {
    return name;
  }

  /**
   * Checks whether the cooldown has passed and the skill can be used again.
   *
   * @return {@code true} if the cooldown period has elapsed
   */
  public boolean canBeUsedAgain() {
    return !(Duration.between(Instant.now(), nextUsableAt).toMillis() > 0);
  }

  /**
   * Sets the cooldown duration of this skill.
   *
   * @param newCoolDown the new cooldown in milliseconds
   */
  public void cooldown(long newCoolDown) {
    this.cooldown = newCoolDown;
  }

  /**
   * Returns the cooldown duration of this skill.
   *
   * @return the cooldown in milliseconds
   */
  public long cooldown() {
    return cooldown;
  }

  /** Activates the cooldown timer by setting the next usable time based on the last usage. */
  private void activateCoolDown() {
    nextUsableAt = lastUsed.plusMillis(cooldown);
  }

  /**
   * Sets the last used time of this skill to the current moment.
   *
   * <p>This effectively resets the cooldown.
   */
  public void setLastUsedToNow() {
    this.lastUsed = Instant.now();
    activateCoolDown();
  }

  /**
   * Returns a copy of the resource cost map for this skill.
   *
   * @return a copy of the resource requirements
   */
  public Map<Resource, Integer> resourceCost() {
    return new HashMap<>(resourceCost);
  }

  /**
   * Replaces the resource cost map of this skill with a new one.
   *
   * @param newResourceCost the new resource requirements
   */
  public void resourceCost(Map<Resource, Integer> newResourceCost) {
    if (newResourceCost == null) newResourceCost = new HashMap<>();
    this.resourceCost = new HashMap<>(newResourceCost);
  }

  /**
   * Updates the required amount of a specific resource, if it exists in the cost map.
   *
   * @param resource the resource to update
   * @param newAmount the new required amount
   */
  public void updateResourceCost(Resource resource, int newAmount) {
    if (resourceCost.containsKey(resource)) {
      resourceCost.put(resource, newAmount);
    }
  }

  /**
   * Adds a new resource requirement to this skill or replaces the amount if the resource is already
   * present.
   *
   * @param resource the resource to add
   * @param amount the required amount
   */
  public void addResource(Resource resource, int amount) {
    resourceCost.put(resource, amount);
  }

  /**
   * Removes a resource requirement from this skill.
   *
   * @param resource the resource to remove
   */
  public void removeResource(Resource resource) {
    resourceCost.remove(resource);
  }
}
