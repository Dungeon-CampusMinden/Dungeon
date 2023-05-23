package contrib.systems;

import static org.junit.Assert.*;

import contrib.components.SkillComponent;
import contrib.utils.components.skill.ISkillFunction;
import contrib.utils.components.skill.Skill;

import core.Entity;
import core.Game;
import core.utils.Constants;

import org.junit.Test;
import org.mockito.Mockito;

public class SkillSystemTest {

    @Test
    public void update() {
        Game.removeAllEntities();
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

        system.showEntity(entity);
        for (int i = 0; i < coolDownInSeconds * Constants.FRAME_RATE; i++) {
            assertTrue(testSkill.isOnCoolDown());
            assertTrue(testSkill2.isOnCoolDown());
            system.execute();
        }

        assertFalse(testSkill.isOnCoolDown());
        assertFalse(testSkill2.isOnCoolDown());
    }
}
