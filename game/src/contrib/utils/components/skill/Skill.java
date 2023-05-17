package contrib.utils.components.skill;

import core.Entity;

import java.time.Instant;

public class Skill {

    private ISkillFunction skillFunction;
    private long coolDownInSeconds;
    private Instant lastUsed;
    private Instant nextUsableAt = Instant.now();

    /**
     * @param skillFunction Function of this skill
     */
    public Skill(ISkillFunction skillFunction, long coolDownInSeconds) {
        this.skillFunction = skillFunction;
        this.coolDownInSeconds = coolDownInSeconds;
    }

    /**
     * Execute the method of this skill
     *
     * @param entity entity which uses the skill
     */
    public void execute(Entity entity) {
        if (canBeUsedAgain()) {
            skillFunction.execute(entity);
            lastUsed = Instant.now();
            activateCoolDown();
        }
    }

    /**
     * @return true if cool down is not 0, else false
     */
    public boolean canBeUsedAgain() {
        return Instant.now().isAfter(nextUsableAt) || Instant.now().equals(nextUsableAt);
    }

    /** activate cool down */
    public void activateCoolDown() {
        nextUsableAt = lastUsed.plusSeconds(coolDownInSeconds);
    }
}
