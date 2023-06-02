package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.utils.components.item.ItemData;

import core.hud.ScreenImage;
import core.utils.Point;

public class InventorySlot extends ScreenImage {

    private final DragAndDrop.Target target;
    private InventoryItem inventoryItem;

    /**
     * Creates an InventorySlot with the given imagePosition
     *
     * @param imagePosition the position of the InventorySlot
     */
    public InventorySlot(Point imagePosition) {
        super("animation/inventorySlot.png", imagePosition, 1);
        this.target =
                new DragAndDrop.Target(this) {
                    @Override
                    public boolean drag(
                            DragAndDrop.Source source,
                            DragAndDrop.Payload payload,
                            float x,
                            float y,
                            int pointer) {
                        return true;
                    }

                    /**
                     * Sets the position of the dragged item to the starting position of the item if
                     * the inventory slot is not empty If the inventory slot is empty, the
                     * InventoryItem is moved from one InventorySlot to another
                     *
                     * @param source the source of the drag
                     * @param payload the payload of the drag
                     * @param x the x position of the drag
                     * @param y the y position of the drag
                     * @param pointer the pointer of the drag
                     */
                    @Override
                    public void drop(
                            DragAndDrop.Source source,
                            DragAndDrop.Payload payload,
                            float x,
                            float y,
                            int pointer) {
                        InventoryItem itempayload = (InventoryItem) payload.getDragActor();
                        InventorySlot itemSlotpayload = (InventorySlot) payload.getObject();
                        if (inventoryItem != null) {
                            source.getActor()
                                    .setPosition(
                                            itempayload.getStartingPosition().x,
                                            itempayload.getStartingPosition().y);
                        } else {
                            source.getActor().setPosition(imagePosition.x, imagePosition.y);
                            inventoryItem = itempayload;
                            itemSlotpayload.setInventoryItem(null);
                        }
                    }
                };
    }

    /**
     * Sets the inventory item of the inventory slot
     *
     * @param item the inventory item of the inventory slot
     */
    public void setInventoryItem(InventoryItem item) {
        this.inventoryItem = item;
    }

    /**
     * Returns the inventory item of the inventory slot
     *
     * @return the inventory item of the inventory slot
     */
    public InventoryItem getInventoryItem() {
        return this.inventoryItem;
    }

    /**
     * Returns the item of the inventory slot
     *
     * @return the item of the inventory slot
     */
    public ItemData getItem() {
        return this.inventoryItem.getItem();
    }

    /**
     * Returns the target of the inventory slot
     *
     * @return the target of the inventory slot
     */
    public DragAndDrop.Target getTarget() {
        return this.target;
    }
}
