package character.skills;

import collision.CharacterDirection;
import java.time.Instant;

public abstract class BaseSkill {
    public enum Target {
        hero,
        enemies,
        environment
    }

    public BaseSkill(int cooldown) {
        this.cooldown = cooldown;
    }

    public Target[] targets;
    public int cooldown;
    public Instant lastCast = Instant.MIN;

    public BaseSkillEffect cast(CharacterDirection direction) {
        Instant a = Instant.now();
        if (a.isAfter(lastCast.plusMillis(cooldown))) {
            lastCast = a.plusMillis(cooldown);
            return spawn(direction);
        }
        return null;
    }

    protected abstract BaseSkillEffect spawn(CharacterDirection direction);
}
