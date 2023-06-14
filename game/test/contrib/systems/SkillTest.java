package contrib.systems;

import static org.junit.Assert.*;

import contrib.utils.components.skill.Skill;

import core.Entity;

import org.junit.After;
import org.junit.Test;

import java.util.function.Consumer;

public class SkillTest {

    private static int value = 0;
    private final int baseCoolDownInSeconds = 2;

    private Entity entity;
    private Skill skill;
    private Consumer<Entity> skillFunction = entity -> value++;

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
        skill.execute(entity);
        assertEquals(1, value);

        // should not execute on cool down
        skill.execute(entity);
        assertEquals(1, value);
    }
}
