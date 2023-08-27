package contrib.utils.components.item;

import static org.junit.Assert.*;

import contrib.components.InventoryComponent;
import contrib.item.Items;

import core.Entity;
import core.Game;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class ItemDataTest {
    @Before
    public void before() {
        Game.removeAllEntities();
    }

    @Test
    public void testDefaultConstructor() {
        ItemData itemData = new ItemData();
        assertEquals(Items.DEFAULT_ITEM, itemData.item());
    }

    @Test
    public void testParameterConstructor() {
        ItemData itemData = new ItemData(Items.RESOURCE_FLOWER_RED);
        assertEquals(Items.RESOURCE_FLOWER_RED, itemData.item());
    }

    // <p> Since we cant update the {@link Game#entities} from outside the gameloop, this is
    // testcase cant be tested.</p>

    /*    @Test
        public void onDropCheckEntity() {

            ItemData itemData = new ItemData();
            Point point = new Point(0, 0);
            itemData.triggerDrop(null, point);
            Entity e = Game.getEntitiesStream().iterator().next();
            PositionComponent pc =
                    (PositionComponent) e.getComponent(PositionComponent.class).orElseThrow();
            assertEquals(point.x, pc.getPosition().x, 0.001);
            assertEquals(point.y, pc.getPosition().y, 0.001);
            DrawComponent ac = (DrawComponent) e.getComponent(DrawComponent.class).orElseThrow();
            // assertEquals(ItemData.DEFAULT_WORLD_ANIMATION, ac.getCurrentAnimation());

            CollideComponent hc =
                    (CollideComponent) e.getComponent(CollideComponent.class).orElseThrow();
        }
    */
    // active
    /** Tests if set callback is called. */
    @Test
    public void testUseCallback() {
        BiConsumer<Entity, ItemData> callback = Mockito.mock(BiConsumer.class);
        ItemData item = new ItemData();
        item.onUse(callback);
        Entity entity = new Entity();
        item.triggerUse(entity);
        Mockito.verify(callback).accept(entity, item);
    }

    /** Tests if no exception is thrown when callback is null. */
    @Test
    public void testUseNullCallback() {
        ItemData item = new ItemData();
        item.onUse(null);
        Entity entity = new Entity();
        item.triggerUse(entity);
    }

    /** Tests if item is removed from inventory after use. */
    @Test
    public void testItemRemovedAfterUseWithDefaultCallback() {
        ItemData item = new ItemData();
        Entity entity = new Entity();
        InventoryComponent inventoryComponent = new InventoryComponent(2);
        entity.addComponent(inventoryComponent);
        inventoryComponent.add(item);
        assertTrue(
                "ItemActive needs to be in entities inventory.",
                Arrays.asList(inventoryComponent.items()).contains(item));
        item.triggerUse(entity);
        assertFalse(
                "Item was not removed from inventory after use.",
                Arrays.asList(inventoryComponent.items()).contains(item));
    }
}
