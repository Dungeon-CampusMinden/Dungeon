package ecs.components;

import static org.junit.Assert.*;

import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SkillComponentTest {

    private final Skill testSkill = Mockito.mock(Skill.class);
    private SkillComponent skillComponent;

    @Before
    public void setup() {
        skillComponent = new SkillComponent(Mockito.mock(Entity.class));
    }

    @Test
    public void addSkill() {
        skillComponent.addSkill(testSkill);
        assertTrue(skillComponent.getSkillSet().contains(testSkill));
    }

    @Test
    public void removeSkill() {
        skillComponent.addSkill(testSkill);
        assertTrue(skillComponent.getSkillSet().contains(testSkill));
        skillComponent.removeSkill(testSkill);
        assertFalse(skillComponent.getSkillSet().contains(testSkill));
    }
}
