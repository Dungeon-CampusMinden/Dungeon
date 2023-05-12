package api.ecs.systems;

import static org.junit.Assert.*;

import api.controller.SystemController;
import api.ecs.components.skill.ISkillFunction;
import api.ecs.components.skill.Skill;
import api.ecs.components.skill.SkillComponent;
import api.ecs.entities.Entity;
import api.tools.Constants;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class SkillSystemTest {

    @Test
    public void update() {
        Game.getEntities().clear();
        Game.systems = new SystemController();
        SkillSystem system = new SkillSystem();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
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
