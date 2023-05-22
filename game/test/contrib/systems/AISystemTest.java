package contrib.systems;

import static org.junit.Assert.assertEquals;

import contrib.components.AIComponent;
import contrib.utils.components.ai.ITransition;

import core.Entity;
import core.Game;

import org.junit.Before;
import org.junit.Test;

public class AISystemTest {

    private int updateCounter;
    private AISystem system;
    private Entity entity;

    @Before
    public void setup() {
        Game.removeAllEntities();
        Game.systems.clear();
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
        system.execute();
        assertEquals(1, updateCounter);
    }

    @Test
    public void updateWithoutAIComponent() {
        entity.removeComponent(AIComponent.class);
        system.execute();
        assertEquals(0, updateCounter);
    }
}
