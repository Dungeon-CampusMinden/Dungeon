package core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import core.level.elements.ILevel;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class GameTest {

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.removeAllSystems();
        Game.currentLevel(null);
    }

    @Test
    public void allEntites() {
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        ILevel level = Mockito.mock(ILevel.class);
        Game.currentLevel(level);
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        assertEquals(8, Game.allEntities().count());
    }

    @Test
    public void removeAllEntites() {
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        ILevel level = Mockito.mock(ILevel.class);
        Game.currentLevel(level);
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        Game.add(new Entity());
        assertEquals(8, Game.allEntities().count());
        Game.removeAllEntities();
        assertEquals(0, Game.allEntities().count());
    }

    @Test
    public void find_exisiting() {
        Entity e = new Entity();
        DummyComponent dc = new DummyComponent();
        e.add(dc);
        Game.add(e);
        assertEquals(e, Game.find(dc).get());
        // load ne level to check if it still works
        ILevel level = Mockito.mock(ILevel.class);
        assertEquals(e, Game.find(dc).get());
    }

    @Test
    public void find_nonExisting() {
        DummyComponent dc = new DummyComponent();
        assertTrue(Game.find(dc).isEmpty());
    }

    private class DummyComponent implements Component {}
}
