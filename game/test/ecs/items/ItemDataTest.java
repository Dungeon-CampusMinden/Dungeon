package ecs.items;

import static org.junit.Assert.*;

import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import starter.Game;
import tools.Point;

public class ItemDataTest {
    private static class ItemDataImpl extends ItemData {
        public ItemDataImpl(
                ItemType itemType,
                Animation inventoryTexture,
                Animation worldTexture,
                String itemName,
                String description) {
            super(itemType, inventoryTexture, worldTexture, itemName, description);
        }

        public ItemDataImpl() {}
    }

    @Test
    public void testDefaultConstructor() {
        ItemData itemData = new ItemDataImpl();
        assertEquals(ItemData.DEFAULT_NAME, itemData.getItemName());
        assertEquals(ItemData.DEFAULT_DESCRIPTION, itemData.getDescription());
        assertEquals(ItemData.DEFAULT_ITEM_TYPE, itemData.getItemType());
        assertEquals(ItemData.DEFAULT_WORLD_ANIMATION, itemData.getWorldTexture());
        assertEquals(ItemData.DEFAULT_INVENTORY_ANIMATION, itemData.getInventoryTexture());
    }

    @Test
    public void testParameterConstructor() {
        ItemType type = ItemType.Basic;
        String inventoryTexture = "InventoryTexture";
        String worldTexture = "WorldTexture";
        String item_name = "r Item Name";
        String item_description = "r Item Description";
        ItemData itemData =
                new ItemDataImpl(
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

    @Before
    public void before() {
        Game.getEntities().clear();
    }

    @Test
    public void onDropCheckEntity() {
        ItemData itemData = new ItemDataImpl();
        assertEquals(0, Game.getEntities().size());
        Point point = new Point(0, 0);
        itemData.onDrop(point);
        assertEquals(1, Game.getEntities().size());
        Entity e = Game.getEntities().iterator().next();
        PositionComponent pc =
                (PositionComponent) e.getComponent(PositionComponent.class).orElseThrow();
        assertEquals(point.x, pc.getPosition().x, 0.001);
        assertEquals(point.y, pc.getPosition().y, 0.001);
        AnimationComponent ac =
                (AnimationComponent) e.getComponent(AnimationComponent.class).orElseThrow();
        assertEquals(ItemData.DEFAULT_WORLD_ANIMATION, ac.getCurrentAnimation());

        HitboxComponent hc = (HitboxComponent) e.getComponent(HitboxComponent.class).orElseThrow();
    }
}
