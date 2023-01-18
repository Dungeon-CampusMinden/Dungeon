package ecs.components;

import static org.junit.Assert.*;

import ecs.components.skill.Skill;
import graphic.Animation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SkillTest {

    private static int value = 0;

    private Skill skill;
    private Method method;
    private Animation animation = Mockito.mock(Animation.class);

    @Before
    public void setup() throws NoSuchMethodException {
        method = SkillTest.class.getMethod("TestMethode");
        skill = new Skill(method, animation);
    }

    @Test
    public void testExecute_active() throws InvocationTargetException, IllegalAccessException {
        value = 0;
        skill.execute();
        assertEquals(1, value);
    }

    @Test
    public void testExecute_inactive() throws InvocationTargetException, IllegalAccessException {
        value = 0;
        skill.toggleActive();
        assertFalse(skill.getActive());
        skill.execute();
        assertEquals(0, value);
    }

    @Test
    public void testToogle() {
        assertTrue(skill.getActive());
        skill.toggleActive();
        ;
        assertFalse(skill.getActive());
        skill.toggleActive();
        ;
        assertTrue(skill.getActive());
    }

    @Test
    public void testGetAnimation() {
        assertEquals(animation, skill.getAnimation());
    }

    public static void TestMethode() {
        value++;
    }
}
