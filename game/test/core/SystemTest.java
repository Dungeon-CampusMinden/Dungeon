package core;

import static junit.framework.TestCase.assertFalse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.function.Consumer;

public class SystemTest {

    @Test
    public void add() {
        System ts =
                new System(DummyComponent.class) {
                    @Override
                    public void execute() {}
                };

        final boolean[] onShow = {false};
        final boolean[] onAdd = {false};
        ts.onEntityShow =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onShow[0] = true;
                    }
                };
        ts.onEntityAdd =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onAdd[0] = true;
                    }
                };

        Entity e = new Entity();
        e.addComponent(new DummyComponent(e));
        ts.showEntity(e);
        assertTrue(onShow[0]);
        assertTrue(onAdd[0]);
        ts.execute();
        assertTrue(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void add_notAccepted() {
        Entity e = new Entity();
        System ts =
                new System(DummyComponent.class) {
                    @Override
                    public void execute() {}
                };
        final boolean[] onShow = {false};
        final boolean[] onAdd = {false};
        ts.onEntityShow =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onShow[0] = true;
                    }
                };
        ts.onEntityAdd =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onAdd[0] = true;
                    }
                };
        ts.showEntity(e);
        assertTrue(onShow[0]);
        assertFalse(onAdd[0]);
        ts.execute();
        assertFalse(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void remove() {
        System ts =
                new System(DummyComponent.class) {
                    @Override
                    public void execute() {}
                };
        final boolean[] onShow = {false};
        final boolean[] onRemove = {false};
        ts.onEntityShow =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onShow[0] = true;
                    }
                };
        ts.onEntityRemove =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onRemove[0] = true;
                    }
                };
        Entity e = new Entity();
        e.addComponent(new DummyComponent(e));
        ts.showEntity(e);
        assertTrue(onShow[0]);
        ts.execute();
        ts.removeEntity(e);
        assertTrue(onRemove[0]);
        ts.execute();
        assertFalse(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void remove_notExisting() {
        System ts =
                new System(DummyComponent.class) {
                    @Override
                    public void execute() {}
                };
        final boolean[] onShow = {false};
        final boolean[] onRemove = {false};
        ts.onEntityShow =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onShow[0] = true;
                    }
                };
        ts.onEntityRemove =
                new Consumer<Entity>() {
                    @Override
                    public void accept(Entity entity) {
                        onRemove[0] = true;
                    }
                };
        Entity e = new Entity();
        ts.showEntity(e);
        assertTrue(onShow[0]);
        ts.execute();
        ts.removeEntity(e);
        assertFalse(onRemove[0]);
        ts.execute();
        assertFalse(ts.getEntityStream().anyMatch(en -> e == en));
        Game.removeAllEntities();
    }

    @Test
    public void clearEntities() {
        System ts =
                new System(DummyComponent.class) {
                    @Override
                    public void execute() {}
                };
        Entity e1 = new Entity();
        Entity e2 = new Entity();
        Entity e3 = new Entity();
        e1.addComponent(new DummyComponent(e1));
        e1.addComponent(new DummyComponent(e2));
        e1.addComponent(new DummyComponent(e3));
        ts.showEntity(e1);
        ts.showEntity(e2);
        ts.showEntity(e3);
        ts.execute();
        assertEquals(3, ts.getEntityStream().count());
        ts.clearEntities();
        assertEquals(0, ts.getEntityStream().count());
        Game.removeAllEntities();
    }

    private class DummyComponent extends Component {

        /**
         * Create a new component and add it to the associated entity
         *
         * @param entity associated entity
         */
        public DummyComponent(Entity entity) {
            super(entity);
        }
    }
}
