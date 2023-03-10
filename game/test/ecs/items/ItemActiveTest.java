package ecs.items;

import static org.junit.Assert.*;

import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import org.junit.Test;
import org.mockito.Mockito;

public class ItemActiveTest {

    /** Tests if set callback is called. */
    @Test
    public void testUseCallback() {
        IItemUse callback = Mockito.mock(IItemUse.class);
        ItemActive item =
                Mockito.mock(
                        ItemActive.class,
                        Mockito.withSettings()
                                .useConstructor("name", "description", callback)
                                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Entity entity = new Entity();
        item.use(entity);
        Mockito.verify(callback).onUse(entity, item);
    }

    /** Tests if no exception is thrown when callback is null. */
    @Test
    public void testUseNullCallback() {
        ItemActive item =
                Mockito.mock(
                        ItemActive.class,
                        Mockito.withSettings()
                                .useConstructor("name", "description", null)
                                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Entity entity = new Entity();
        item.use(entity);
    }

    /** Tests if item is removed from inventory after use. */
    @Test
    public void testItemRemovedAfterUseWithDefaultCallback() {
        ItemActive item = new ItemActive() {};
        Entity entity = new Entity();
        InventoryComponent inventoryComponent = new InventoryComponent(entity, 2);
        inventoryComponent.addItem(item);
        assertTrue(inventoryComponent.getItems().contains(item));
        item.use(entity);
        assertFalse(
                "Item was not removed from inventory after use.",
                inventoryComponent.getItems().contains(item));
    }
}
