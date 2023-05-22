package core;

import static junit.framework.TestCase.assertFalse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SystemTest {

    @Test
    public void add() {
        System ts =
                new System() {
                    @Override
                    public void execute() {}

                    @Override
                    protected boolean accept(Entity entity) {
                        return false;
                    }
                };
        Entity e = new Entity();
        ts.showEntity(e);
        ts.execute();
        assertFalse(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void add_notAccepted() {
        Entity e = new Entity();
        System ts =
                new System() {
                    @Override
                    public void execute() {}

                    @Override
                    protected boolean accept(Entity entity) {
                        return true;
                    }
                };
        ts.showEntity(e);
        ts.execute();
        assertTrue(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void remove() {
        System ts =
                new System() {
                    @Override
                    public void execute() {}

                    @Override
                    protected boolean accept(Entity entity) {
                        return true;
                    }
                };
        Entity e = new Entity();
        ts.showEntity(e);
        ts.execute();
        ts.removeEntity(e);
        ts.execute();
        assertFalse(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void clearEntities() {
        System ts =
                new System() {
                    @Override
                    public void execute() {}

                    @Override
                    protected boolean accept(Entity entity) {
                        return true;
                    }
                };
        ts.showEntity(new Entity());
        ts.showEntity(new Entity());
        ts.showEntity(new Entity());
        ts.execute();
        assertEquals(3, ts.getEntityStream().count());
        ts.clearEntities();
        assertEquals(0, ts.getEntityStream().count());
        Game.removeAllEntities();
    }
}
