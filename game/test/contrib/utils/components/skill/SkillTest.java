package contrib.utils.components.skill;

import static org.junit.Assert.*;

import core.Entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.function.Consumer;

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
        assertTrue(skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals(1, value);
    }

    @Test
    public void executeWhenCoolDownActive() {
        skill.execute(entity);
        assertFalse(skill.canBeUsedAgain());
        assertEquals(1, value);
    }

    @Test
    public void executeWhenCoolDownExpired() {
        skill.execute(entity);

        Instant afterCoolDown = Instant.now().plusSeconds(baseCoolDownInSeconds);

        while (Instant.now().isBefore(afterCoolDown)) {
            assertFalse(skill.canBeUsedAgain());
            assertEquals(1, value);
        }

        assertTrue(skill.canBeUsedAgain());
        skill.execute(entity);
        assertEquals(2, value);
    }
}
