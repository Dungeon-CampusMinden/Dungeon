package ecs.components;

import static org.junit.Assert.*;

import ecs.components.skill.ISkillFunction;
import ecs.components.skill.Skill;
import ecs.entities.Entity;
import graphic.Animation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SkillTest {

    private static int value = 0;

    private Entity entity;
    private Skill skill;
    private ISkillFunction skillFunction = entity -> value++;
    private Animation animation = Mockito.mock(Animation.class);

    @Before
    public void setup() {
        entity = new Entity();
        skill = new Skill(animation, skillFunction);
    }

    @Test
    public void execute_active() {
        value = 0;
        skill.execute(entity);
        assertEquals(1, value);
    }

    @Test
    public void execute_inactive() {
        value = 0;
        skill.toggleActive();
        assertFalse(skill.getActive());
        skill.execute(entity);
        assertEquals(0, value);
    }

    @Test
    public void toogle() {
        assertTrue(skill.getActive());
        skill.toggleActive();
        ;
        assertFalse(skill.getActive());
        skill.toggleActive();
        ;
        assertTrue(skill.getActive());
    }

    @Test
    public void getAnimation() {
        assertEquals(animation, skill.getAnimation());
    }
}
