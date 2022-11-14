package character.skills;

import collision.CharacterDirection;
import java.time.Instant;

public abstract class BaseSkill {
    public enum Target {
        hero,
        enemies,
        environment
    }

    public Target[] targets;
    public int cooldown;
    public Instant lastCast = Instant.MIN;

    public BaseSkillEffect cast(CharacterDirection direction) {
        if (Instant.now().isAfter(lastCast.plusMillis(cooldown))) {
            return spawn(direction);
        }
        return null;
    }

    protected abstract BaseSkillEffect spawn(CharacterDirection direction);
}
