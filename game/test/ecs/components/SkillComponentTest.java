package ecs.components;

import static org.junit.Assert.*;

import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import ecs.entities.Entity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class SkillComponentTest {

    private final Skill testSkill = Mockito.mock(Skill.class);
    private SkillComponent skillComponent;

    @Before
    public void setup() {
        skillComponent = new SkillComponent(new Entity());
    }

    @After
    public void cleanup() {
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.setHero(null);
        Game.currentLevel = null;
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
