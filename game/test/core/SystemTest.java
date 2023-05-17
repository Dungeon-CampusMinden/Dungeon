package core;

import static junit.framework.TestCase.assertFalse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import core.utils.controller.SystemController;

import org.junit.Before;
import org.junit.Test;

public class SystemTest {

    private System testSystem;
    private int updates;

    @Before
    public void setup() {
        updates = 0;
        Game.systems = new SystemController();
        testSystem =
                new System() {
                    @Override
                    public boolean accept(Entity entity) {
                        return true;
                    }

                    @Override
                    public void systemUpdate() {
                        updates++;
                    }
                };
    }

    @Test
    public void cTor() {
        assertTrue(Game.systems.contains(testSystem));
    }

    @Test
    public void cTor_existingEntities() {
        new Entity();
        System ts =
                new System() {
                    @Override
                    protected void systemUpdate() {}

                    @Override
                    protected boolean accept(Entity entity) {
                        return true;
                    }
                };
        ts.update();
        assertEquals(1, ts.getEntityStream().count());
        Game.removeAllEntities();
        Game.systems.clear();
    }

    @Test
    public void add() {
        testSystem =
                new System() {
                    @Override
                    public boolean accept(Entity entity) {
                        return false;
                    }

                    @Override
                    public void systemUpdate() {
                        updates++;
                    }
                };
        Entity e = new Entity();
        testSystem.showEntity(e);
        testSystem.update();
        assertFalse(testSystem.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void add_notAccepted() {
        Entity e = new Entity();
        testSystem.showEntity(e);
        testSystem.update();
        assertTrue(testSystem.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void remove() {
        Entity e = new Entity();
        testSystem.showEntity(e);
        testSystem.update();
        testSystem.removeEntity(e);
        testSystem.update();
        assertFalse(testSystem.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void clearEntities() {
        testSystem.showEntity(new Entity());
        testSystem.showEntity(new Entity());
        testSystem.showEntity(new Entity());
        testSystem.update();
        assertEquals(3, testSystem.getEntityStream().count());
        testSystem.clearEntities();
        assertEquals(0, testSystem.getEntityStream().count());
        Game.removeAllEntities();
    }

    @Test
    public void pause() {
        assertEquals(0, updates);
        Game.systems.update();
        assertEquals(1, updates);
        testSystem.toggleRun();
        Game.systems.update();
        assertEquals(1, updates);
        testSystem.toggleRun();
        Game.systems.update();
        assertEquals(2, updates);
    }
}
