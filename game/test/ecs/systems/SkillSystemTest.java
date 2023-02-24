package ecs.systems;

import static org.junit.Assert.*;

import ecs.components.skill.ISkillFunction;
import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import ecs.entities.Entity;
import mydungeon.ECS;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Constants;

public class SkillSystemTest {

    @Test
    public void update() {
        ECS.entities.clear();
        ECS.systems = new SystemController();
        SkillSystem system = new SkillSystem();
        Entity entity = new Entity();
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
