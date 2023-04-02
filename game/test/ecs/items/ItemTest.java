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

public class ItemTest {
    private static class ItemImpl extends Item {
        public ItemImpl(
                ItemType itemType,
                Animation inventoryTexture,
                Animation worldTexture,
                String itemName,
                String description) {
            super(itemType, inventoryTexture, worldTexture, itemName, description);
        }

        public ItemImpl() {}
    }

    @Test
    public void testDefaultConstructor() {
        Item item = new ItemImpl();
        assertEquals(Item.DEFAULT_NAME, item.getItemName());
        assertEquals(Item.DEFAULT_DESCRIPTION, item.getDescription());
        assertEquals(Item.DEFAULT_ITEM_TYPE, item.getItemType());
        assertEquals(Item.DEFAULT_WORLD_ANIMATION, item.getWorldTexture());
        assertEquals(Item.DEFAULT_INVENTORY_ANIMATION, item.getInventoryTexture());
    }

    @Test
    public void testParameterConstructor() {
        ItemType type = ItemType.Basic;
        String inventoryTexture = "InventoryTexture";
        String worldTexture = "WorldTexture";
        String item_name = "r Item Name";
        String item_description = "r Item Description";
        Item item =
                new ItemImpl(
                        type,
                        new Animation(List.of(inventoryTexture), 1),
                        new Animation(List.of(worldTexture), 1),
                        item_name,
                        item_description);

        assertEquals(type, item.getItemType());
        assertEquals(inventoryTexture, item.getInventoryTexture().getNextAnimationTexturePath());
        assertEquals(worldTexture, item.getWorldTexture().getNextAnimationTexturePath());
        assertEquals(item_name, item.getItemName());
        assertEquals(item_description, item.getDescription());
    }

    @Before
    public void before() {
        Game.getEntities().clear();
    }

    @Test
    public void onDropCheckEntity() {
        Item item = new ItemImpl();
        assertEquals(0, Game.getEntities().size());
        Point point = new Point(0, 0);
        item.onDrop(point);
        assertEquals(1, Game.getEntities().size());
        Entity e = Game.getEntities().iterator().next();
        PositionComponent pc =
                (PositionComponent) e.getComponent(PositionComponent.class).orElseThrow();
        assertEquals(point.x, pc.getPosition().x, 0.001);
        assertEquals(point.y, pc.getPosition().y, 0.001);
        AnimationComponent ac =
                (AnimationComponent) e.getComponent(AnimationComponent.class).orElseThrow();
        assertEquals(Item.DEFAULT_WORLD_ANIMATION, ac.getCurrentAnimation());

        HitboxComponent hc = (HitboxComponent) e.getComponent(HitboxComponent.class).orElseThrow();
    }
}
