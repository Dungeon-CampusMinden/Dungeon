package core.hud.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

import contrib.utils.components.item.ItemData;
import contrib.utils.components.item.ItemNature;
import core.utils.Constants;

public class InventorySlot extends Stack {
    private ItemNature itemNature;

    /** Creates an InventorySlot */
    public InventorySlot() {
        itemNature = ItemNature.UNDEFINED;
        this.add(new Image(new Texture(Gdx.files.internal(Constants.INVENTORYSLOT_PATH))));
        this.addListener(new InventorySlotClickListener(this));
    }

    /** Creates an Inventory-Slot using the predefined path
     * @param path: path for pictute of inventory slot
     */
    public InventorySlot(String path, ItemNature nature) {
        itemNature = nature;
        this.add(new Image(new Texture(Gdx.files.internal(path))));
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

    public ItemNature getItemNature() {
        return itemNature;
    }
}
