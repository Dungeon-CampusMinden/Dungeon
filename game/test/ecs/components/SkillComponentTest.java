package ecs.components;

import static org.junit.Assert.*;

import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SkillComponentTest {

    private Entity entity;
    private SkillComponent component;

    private Skill testSkill = Mockito.mock(Skill.class);

    @Before
    public void setup() {
        entity = new Entity();
        component = new SkillComponent(entity);
        entity.addComponent(SkillComponent.name, component);
    }

    @Test
    public void addSkill() {
        component.addSkill(testSkill);
        assertTrue(component.getSkillSet().contains(testSkill));
    }

    @Test
    public void removeSkill() {
        component.addSkill(testSkill);
        assertTrue(component.getSkillSet().contains(testSkill));
        component.removeSkill(testSkill);
        assertFalse(component.getSkillSet().contains(testSkill));
    }
}
