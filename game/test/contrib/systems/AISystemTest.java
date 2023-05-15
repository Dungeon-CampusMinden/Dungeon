package contrib.systems;

import static org.junit.Assert.assertEquals;

import contrib.components.AIComponent;
import contrib.utils.components.ai.ITransition;

import core.Entity;
import core.Game;
import core.utils.controller.SystemController;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AISystemTest {

    private int updateCounter;
    private AISystem system;
    private Entity entity;

    @Before
    public void setup() {
        Game.systems = Mockito.mock(SystemController.class);
        Game.getDelayedEntitySet().removeAll(Game.getEntities());
        Game.getDelayedEntitySet().update();
        system = new AISystem();
        entity = new Entity();
        AIComponent component = new AIComponent(entity);
        component.setTransitionAI(
                new ITransition() {
                    @Override
                    public boolean isInFightMode(Entity entity) {
                        updateCounter++;
                        return false;
                    }
                });
        updateCounter = 0;
    }

    @Test
    public void update() {
        Game.getDelayedEntitySet().update();
        system.update();
        assertEquals(1, updateCounter);
    }

    @Test
    public void updateWithoutAIComponent() {
        entity.removeComponent(AIComponent.class);
        system.update();
        assertEquals(0, updateCounter);
    }
}
