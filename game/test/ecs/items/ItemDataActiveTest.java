package ecs.items;

import static org.junit.Assert.*;

import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import org.junit.Test;
import org.mockito.Mockito;

public class ItemDataActiveTest {

    /** Tests if set callback is called. */
    @Test
    public void testUseCallback() {
        IItemUse callback = Mockito.mock(IItemUse.class);
        ItemDataActive item =
                Mockito.mock(
                        ItemDataActive.class,
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
        ItemDataActive item =
                Mockito.mock(
                        ItemDataActive.class,
                        Mockito.withSettings()
                                .useConstructor("name", "description", null)
                                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Entity entity = new Entity();
        item.use(entity);
    }

    /** Tests if item is removed from inventory after use. */
    @Test
    public void testItemRemovedAfterUseWithDefaultCallback() {
        ItemDataActive item = new ItemDataActive() {};
        Entity entity = new Entity();
        InventoryComponent inventoryComponent = new InventoryComponent(entity, 2);
        inventoryComponent.addItem(item);
        assertTrue(
                "ItemActive needs to be in entities inventory.",
                inventoryComponent.getItems().contains(item));
        item.use(entity);
        assertFalse(
                "Item was not removed from inventory after use.",
                inventoryComponent.getItems().contains(item));
    }
}
