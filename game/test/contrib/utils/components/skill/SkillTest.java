package contrib.utils.components.skill;

import static org.junit.Assert.*;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

public class SkillTest {

    private static int value = 0;
    private Entity entity;
    private Skill skill;
    private final int baseCoolDownInMilliSeconds = 2000;
    private Consumer<Entity> skillFunction = entity -> value++;

    @Before
    public void setup() {
        entity = new Entity();
        skill = new Skill(skillFunction, baseCoolDownInMilliSeconds);
    }

    @After
    public void cleanup() {
        value = 0;
        Game.removeAllEntities();
    }

    @Test
    public void execute() {
        assertTrue("Skill should be executable", skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals("Skill should have been executed once", 1, value);
        assertFalse("Skill is in cool down and can not be executable", skill.canBeUsedAgain());
    }

    @Test
    public void executeWhenCoolDownActive() {
        skill.execute(entity);
        assertFalse("Skill should not be executable", skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals("Skill should have been executed once", 1, value);
    }

    @Test
    public void executeWhenCoolDownExpired() throws InterruptedException {
        final long baseCoolDown = 1;
        skill = new Skill(skillFunction, baseCoolDown);
        skill.execute(entity);
        assertEquals("Skill should have been executed once", 1, value);
        assertTrue("Skill should be usable again", skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals("Skill should have been executed twice", 2, value);
    }
}
