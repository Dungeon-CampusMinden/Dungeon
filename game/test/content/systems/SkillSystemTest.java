package content.systems;

import static org.junit.Assert.*;

import api.Entity;
import api.Game;
import api.utils.Constants;
import api.utils.componentUtils.skillComponent.ISkillFunction;
import api.utils.componentUtils.skillComponent.Skill;
import api.utils.controller.SystemController;
import content.component.SkillComponent;
import org.junit.Test;
import org.mockito.Mockito;

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
