package ecs.components;

import static org.junit.Assert.*;

import ecs.components.skill.ISkillFunction;
import ecs.components.skill.Skill;
import ecs.entities.Entity;
import org.junit.After;
import org.junit.Test;
import tools.Constants;

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
