package contrib.systems;

import static org.junit.Assert.assertEquals;

import contrib.components.AIComponent;

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
                entity -> {
                    updateCounter++;
                    return false;
                });
        updateCounter = 0;
    }

    @Test
    public void update() {
        system.showEntity(entity);

        system.execute();

        assertEquals(1, updateCounter);
    }

    @Test
    public void updateWithoutAIComponent() {
        entity.removeComponent(AIComponent.class);
        system.showEntity(entity);

        system.execute();

        assertEquals(0, updateCounter);
    }
}
