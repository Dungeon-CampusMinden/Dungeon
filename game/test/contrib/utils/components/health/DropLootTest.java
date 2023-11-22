package contrib.utils.components.health;

import static org.junit.Assert.*;

import contrib.components.InventoryComponent;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import org.junit.Before;
import org.junit.Test;

public class DropLootTest {
    /** Checks the handling when the InventoryComponent is missing on the entity */
    @Before
    public void setup() {
        Game.removeAllEntities();
    }

    @Test
    public void entityMissingInventoryComponent() {
        DropLoot dropLoot = new DropLoot();
        MissingComponentException mce =
                assertThrows(MissingComponentException.class, () -> dropLoot.accept(new Entity()));
        assertTrue(mce.getMessage().contains(InventoryComponent.class.getName()));
    }

    /** Checks the handling when the PositionComponent is missing on the entity */
    @Test
    public void entityMissingPositionComponent() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        entity.add(new InventoryComponent(10));
        MissingComponentException mce =
                assertThrows(MissingComponentException.class, () -> dropLoot.accept(entity));
        assertTrue(mce.getMessage().contains(PositionComponent.class.getName()));
    }

    /** Checks the handling when the InventoryComponent has no Items */
    @Test
    public void entityInventoryComponentEmpty() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        entity.add(new PositionComponent(new Point(1, 2)));
        entity.add(new InventoryComponent(10));
        dropLoot.accept(entity);
        Game.remove(entity);
        assertEquals(0, Game.entityStream().count());
    }

    /**
     * Checks the handling when the InventoryComponent has exactly one Item
     *
     * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
     * cant be tested.
     */
    /* @Test
    public void entityInventoryComponentOneItem() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        Point entityPosition = new Point(1, 2);
        new PositionComponent(entity, entityPosition);
        InventoryComponent inventoryComponent = new InventoryComponent(entity, 10);
        inventoryComponent.addItem(new ItemData());
        Game.removeAllEntities();
        dropLoot.onDeath(entity);
        assertEquals(1, Game.getEntitiesStream().count());
        assertTrue(
                Game.getEntitiesStream()
                        .allMatch(
                                x ->
                                        x.getComponent(PositionComponent.class)
                                                .map(
                                                        component ->
                                                                isPointEqual(
                                                                        entityPosition,
                                                                        (PositionComponent)
                                                                                component))
                                                .orElse(false)));
    }*/

    /**
     * Checks the handling when the InventoryComponent has more than one Item
     *
     * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
     * cant be tested.
     */
    /*@Test
        public void entityInventoryComponentMultipleItems() {
            DropLoot dropLoot = new DropLoot();
            Entity entity = new Entity();
            Point entityPosition = new Point(1, 2);
            new PositionComponent(entity, entityPosition);
            InventoryComponent inventoryComponent = new InventoryComponent(entity, 10);
            inventoryComponent.addItem(new ItemData());
            inventoryComponent.addItem(new ItemData());

            Game.removeAllEntities();
            dropLoot.onDeath(entity);

            assertEquals(2, Game.getEntitiesStream().count());
            assertTrue(
                    Game.getEntitiesStream()
                            .allMatch(
                                    x ->
                                            x.getComponent(PositionComponent.class)
                                                    .map(
                                                            component ->
                                                                    isPointEqual(
                                                                            entityPosition,
                                                                            (PositionComponent)
                                                                                    component))
                                                    .orElse(false)));
        }
    */
    /**
     * Helpermethod, checks Points for same values for x and y
     *
     * @param entityPosition the Position of the Entity itself
     * @return a function which returns true when the Points are equal, otherwise false
     */
    private static boolean isPointEqual(Point entityPosition, PositionComponent component) {
        Point a = component.position();
        return a.x == entityPosition.x && a.y == entityPosition.y;
    }
}
