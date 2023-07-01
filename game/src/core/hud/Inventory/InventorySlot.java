package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

import contrib.utils.components.item.ItemNature;

public class InventorySlot extends Stack {
    private final ItemNature itemNature;

    /** Creates an InventorySlot */
    public InventorySlot() {
        this(ItemNature.UNDEFINED);
    }

    /** Creates an Inventory-Slot using the predefined path */
    public InventorySlot(ItemNature nature) {
        itemNature = nature;
        this.add(new Image(new Skin(), nature.slotBackground));
        this.addListener(new InventorySlotClickListener(this));
    }

    /**
     * Sets the inventory item of the inventory slot
     *
     * @param item the inventory item of the inventory slot
     */
    public void setInventoryItem(InventoryItem item) {
        if (item != null) this.add(item);
    }

    /** Removes the inventory item from the inventory slot */
    public void removeInventoryItem() {
        if (hasInventoryItem()) this.removeActor(this.getInventoryItem());
    }

    /**
     * Returns the inventory item of the inventory slot
     *
     * @return the inventory item of the inventory slot
     */
    public InventoryItem getInventoryItem() {
        if (!this.hasInventoryItem()) {
            return null;
        }
        return (InventoryItem) this.getChild(1);
    }

    /**
     * Returns true if the inventory slot has an inventory item
     *
     * @return true if the inventory slot has an inventory item
     */
    public boolean hasInventoryItem() {
        return this.getChildren().size > 1;
    }

    public ItemNature itemNature() {
        return itemNature;
    }
}
