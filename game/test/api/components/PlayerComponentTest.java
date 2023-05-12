package api.components;

import static org.junit.Assert.*;

import api.Entity;
import api.utils.componentUtils.skillComponent.Skill;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PlayerComponentTest {

    private PlayerComponent playableComponent;

    @Before
    public void setup() {
        playableComponent = new PlayerComponent(new Entity());
    }

    @Test
    public void isPlayable() {
        assertTrue(playableComponent.isPlayable());
        playableComponent.setPlayable(false);
        assertFalse(playableComponent.isPlayable());
    }

    @Test
    public void setSkillSlot1() {
        Skill s = Mockito.mock(Skill.class);
        playableComponent.setSkillSlot1(s);
        assertEquals(s, playableComponent.getSkillSlot1().get());
        Skill s2 = Mockito.mock(Skill.class);
        playableComponent.setSkillSlot1(s2);
        assertEquals(s2, playableComponent.getSkillSlot1().get());
    }

    @Test
    public void setSkillSlot2() {
        Skill s = Mockito.mock(Skill.class);
        playableComponent.setSkillSlot2(s);
        assertEquals(s, playableComponent.getSkillSlot2().get());
        Skill s2 = Mockito.mock(Skill.class);
        playableComponent.setSkillSlot2(s2);
        assertEquals(s2, playableComponent.getSkillSlot2().get());
    }
}
