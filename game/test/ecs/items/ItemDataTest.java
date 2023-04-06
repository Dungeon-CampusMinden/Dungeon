package ecs.items;

import static org.junit.Assert.*;

import configuration.ItemConfig;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.InventoryComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;
import tools.Point;

public class ItemDataTest {
    @Before
    public void before() {
        Game.getEntitiesToAdd().clear();
    }

    @Test
    public void testDefaultConstructor() {
        ItemData itemData = new ItemData();
        assertEquals(ItemConfig.NAME.get(), itemData.getItemName());
        assertEquals(ItemConfig.DESCRIPTION.get(), itemData.getDescription());
        assertEquals(ItemConfig.TYPE.get(), itemData.getItemType());
        // assertEquals(ItemData.DEFAULT_WORLD_ANIMATION, itemData.getWorldTexture());
        // assertEquals(ItemData.DEFAULT_INVENTORY_ANIMATION, itemData.getInventoryTexture());
    }

    @Test
    public void testParameterConstructor() {
        ItemType type = ItemType.Basic;
        String inventoryTexture = "InventoryTexture";
        String worldTexture = "WorldTexture";
        String item_name = "r Item Name";
        String item_description = "r Item Description";
        ItemData itemData =
                new ItemData(
                        type,
                        new Animation(List.of(inventoryTexture), 1),
                        new Animation(List.of(worldTexture), 1),
                        item_name,
                        item_description);

        assertEquals(type, itemData.getItemType());
        assertEquals(
                inventoryTexture, itemData.getInventoryTexture().getNextAnimationTexturePath());
        assertEquals(worldTexture, itemData.getWorldTexture().getNextAnimationTexturePath());
        assertEquals(item_name, itemData.getItemName());
        assertEquals(item_description, itemData.getDescription());
    }

    @Test
    public void onDropCheckEntity() {

        ItemData itemData = new ItemData();
        assertEquals(0, Game.getEntitiesToAdd().size());
        Point point = new Point(0, 0);
        itemData.triggerDrop(null, point);
        assertEquals(1, Game.getEntitiesToAdd().size());
        Entity e = Game.getEntitiesToAdd().iterator().next();
        PositionComponent pc =
                (PositionComponent) e.getComponent(PositionComponent.class).orElseThrow();
        assertEquals(point.x, pc.getPosition().x, 0.001);
        assertEquals(point.y, pc.getPosition().y, 0.001);
        AnimationComponent ac =
                (AnimationComponent) e.getComponent(AnimationComponent.class).orElseThrow();
        // assertEquals(ItemData.DEFAULT_WORLD_ANIMATION, ac.getCurrentAnimation());

        HitboxComponent hc = (HitboxComponent) e.getComponent(HitboxComponent.class).orElseThrow();
    }

    // active
    /** Tests if set callback is called. */
    @Test
    public void testUseCallback() {
        IOnUse callback = Mockito.mock(IOnUse.class);
        ItemData item = new ItemData();
        item.setOnUse(callback);
        Entity entity = new Entity();
        item.triggerUse(entity);
        Mockito.verify(callback).onUse(entity, item);
    }

    /** Tests if no exception is thrown when callback is null. */
    @Test
    public void testUseNullCallback() {
        ItemData item = new ItemData();
        item.setOnUse(null);
        Entity entity = new Entity();
        item.triggerUse(entity);
    }

    /** Tests if item is removed from inventory after use. */
    @Test
    public void testItemRemovedAfterUseWithDefaultCallback() {
        ItemData item = new ItemData();
        Entity entity = new Entity();
        InventoryComponent inventoryComponent = new InventoryComponent(entity, 2);
        inventoryComponent.addItem(item);
        assertTrue(
                "ItemActive needs to be in entities inventory.",
                inventoryComponent.getItems().contains(item));
        item.triggerUse(entity);
        assertFalse(
                "Item was not removed from inventory after use.",
                inventoryComponent.getItems().contains(item));
    }
}
