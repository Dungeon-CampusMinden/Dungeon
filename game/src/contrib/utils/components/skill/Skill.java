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

    private Consumer<Entity> skillFunction;
    private long coolDownInMilliSeconds;
    private Instant lastUsed;
    private Instant nextUsableAt = Instant.now();

    /**
     * @param skillFunction functionality of the skill
     * @param coolDownInMilliSeconds the time that needs to pass between use of the skill and the
     *     next possible use of the skill
     */
    public Skill(Consumer<Entity> skillFunction, long coolDownInMilliSeconds) {
        this.skillFunction = skillFunction;
        this.coolDownInMilliSeconds = coolDownInMilliSeconds;
    }

    /**
     * Execute the method of this skill, save the time the skill was last used and update when it
     * can be used again
     *
     * <p>If the skill was used, the cooldown will be set.
     *
     * @param entity entity which uses this skill
     */
    public void execute(Entity entity) {
        if (canBeUsedAgain()) {
            skillFunction.accept(entity);
            lastUsed = Instant.now();
            activateCoolDown();
        }
    }

    /**
     * check if the cool down has passed and the skill can be used again
     *
     * @return true if the specified time (coolDownInSeconds) has passed
     */
    public boolean canBeUsedAgain() {
        // check if the cooldown is active, return the negated result (this avoids some problems in
        // nano-sec range)
        return !(Duration.between(Instant.now(), nextUsableAt).toMillis() > 0);
    }

    /**
     * adds coolDownInSeconds to the time the skill was last used and updates when this skill can be
     * used again
     */
    private void activateCoolDown() {
        nextUsableAt = lastUsed.plusMillis(coolDownInMilliSeconds);
    }
}
