package contrib.utils.components.skill;

import static org.junit.Assert.*;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SkillTest {

    private static int value = 0;

    private Consumer<Entity> skillFunction = entity -> value++;

    @After
    public void cleanup() {
        value = 0;
        Game.removeAllEntities();
    }

    @Test
    public void execute() {
        Entity entity = new Entity();
        final int baseCoolDownInSeconds = 2;
        Skill skill = new Skill(skillFunction, baseCoolDownInSeconds);

        assertTrue("Skill should be executable", skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals("Skill should have been executed once", 1, value);
        assertFalse("Skill is in cool down and can not be executable", skill.canBeUsedAgain());
    }

    @Test
    public void executeWhenCoolDownActive() {
        Entity entity = new Entity();
        final int baseCoolDownInSeconds = 2;
        Skill skill = new Skill(skillFunction, baseCoolDownInSeconds);

        skill.execute(entity);
        assertFalse("Skill should not be executable", skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals("Skill should have been executed once", 1, value);
    }

    @Test
    public void executeWhenCoolDownExpired() {
        Entity entity = new Entity();
        final long baseCoolDown = TimeUnit.MILLISECONDS.toSeconds(1);
        Skill skill = new Skill(skillFunction, baseCoolDown);

        skill.execute(entity);
        assertEquals("Skill should have been executed once", 1, value);
        assertTrue("Skill should be usable again", skill.canBeUsedAgain());

        skill.execute(entity);
        assertEquals("Skill should have been executed twice", 2, value);
    }
}
