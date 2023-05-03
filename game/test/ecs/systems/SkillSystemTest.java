package ecs.systems;

import static org.junit.Assert.*;

import trashcan.SystemController;
import component_tools.skills.ISkillFunction;
import component_tools.skills.Skill;
import components.SkillComponent;
import entities.Entity;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;
import systems.SkillSystem;
import component_tools.position.Constants;

public class SkillSystemTest {

    @Test
    public void update() {
        Game.getEntities().clear();
        Game.systems = new SystemController();
        SkillSystem system = new SkillSystem();
        Entity entity = new Entity();
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        ISkillFunction skillFunction = Mockito.mock(ISkillFunction.class);
        int coolDownInSeconds = 2;
        Skill testSkill = new Skill(skillFunction, coolDownInSeconds);
        Skill testSkill2 = new Skill(skillFunction, coolDownInSeconds);
        SkillComponent sc = new SkillComponent(entity);
        sc.addSkill(testSkill);
        sc.addSkill(testSkill2);

        assertFalse(testSkill.isOnCoolDown());
        assertFalse(testSkill2.isOnCoolDown());
        testSkill.execute(entity);
        testSkill2.execute(entity);

        for (int i = 0; i < coolDownInSeconds * Constants.FRAME_RATE; i++) {
            assertTrue(testSkill.isOnCoolDown());
            assertTrue(testSkill2.isOnCoolDown());
            system.update();
        }

        assertFalse(testSkill.isOnCoolDown());
        assertFalse(testSkill2.isOnCoolDown());
    }
}
