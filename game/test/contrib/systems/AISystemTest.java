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
        entity.add(
                new AIComponent(
                        null,
                        e -> {},
                        entity -> {
                            updateCounter++;
                            return false;
                        }));
        Game.add(entity);

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
        system.execute();
        assertEquals(1, updateCounter);
    }
}
