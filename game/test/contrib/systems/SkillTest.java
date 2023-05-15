package contrib.systems;

import static org.junit.Assert.*;

import contrib.utils.components.skill.ISkillFunction;
import contrib.utils.components.skill.Skill;

import core.Entity;
import core.utils.Constants;

import org.junit.After;
import org.junit.Test;

public class SkillTest {

    private static int value = 0;
    private final int baseCoolDownInSeconds = 2;

    private Entity entity;
    private Skill skill;
    private ISkillFunction skillFunction = entity -> value++;

    @After
    public void cleanup() {
        value = 0;
    }

    @Test
    public void execute() {
        // setup
        entity = new Entity();
        skill = new Skill(skillFunction, baseCoolDownInSeconds);

        // test first execution
        assertFalse(skill.isOnCoolDown());
        skill.execute(entity);
        assertEquals(1, value);

        // should not execute on cool down
        assertTrue(skill.isOnCoolDown());
        skill.execute(entity);
        assertEquals(1, value);

        // reduce cool down to 0
        for (int i = 0; i < (baseCoolDownInSeconds * Constants.FRAME_RATE); i++) {
            assertTrue(skill.isOnCoolDown());
            skill.reduceCoolDown();
        }

        // execution after cool down is over
        assertFalse(skill.isOnCoolDown());
        skill.execute(entity);
        assertEquals(2, value);
    }
}
