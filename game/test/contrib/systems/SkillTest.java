package contrib.systems;

import static org.junit.Assert.*;

import contrib.utils.components.skill.Skill;

import core.Entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;
import java.time.Instant;

public class SkillTest {

    private static int value = 0;
    private final int baseCoolDownInSeconds = 5;

    private Entity entity;
    private Skill skill;
    private Consumer<Entity> skillFunction = entity -> value++;

    @Before
    public void setup() {
        entity = new Entity();
        skill = new Skill(skillFunction, baseCoolDownInSeconds);
    }

    @After
    public void cleanup() {
        value = 0;
    }

    @Test
    public void execute() {

        // test first execution
        assertTrue(skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals(1, value);

        // should not execute on cool down
        assertFalse(skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals(1, value);

        // reduce cool down to 0
        Instant afterCoolDown = Instant.now().plusSeconds(baseCoolDownInSeconds);
        while (Instant.now().isBefore(afterCoolDown)) {
            assertFalse(skill.canBeUsedAgain());
        }

        // execution after cool down is over
        assertTrue(skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals(2, value);
    }
}
