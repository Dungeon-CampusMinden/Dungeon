package contrib.systems;

import static org.junit.Assert.assertEquals;

import contrib.components.AIComponent;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AISystemTest {

    private int updateCounter;
    private AISystem system;
    private Entity entity;

    @Before
    public void setup() {
        Game.removeAllEntities();
        Game.removeAllSystems();
        system = new AISystem();
        Game.add(system);
        entity = new Entity();
        new AIComponent(
                entity,
                null,
                e -> {},
                entity -> {
                    updateCounter++;
                    return false;
                });

        updateCounter = 0;
    }

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.currentLevel(null);
        Game.removeAllSystems();
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
