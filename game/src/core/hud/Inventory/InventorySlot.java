package core.hud.Inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

public class InventorySlot extends Stack {

    /** Creates an InventorySlot */
    public InventorySlot() {
        this.add(new Image(new Texture("animation/inventorySlot.png")));
    }

    /**
     * Sets the inventory item of the inventory slot
     *
     * @param item the inventory item of the inventory slot
     */
    public void setInventoryItem(InventoryItem item) {
        if (item != null) this.add(item);
    }

    public void removeInventoryItem() {
        if (hasInventoryItem())
            this.removeActor(this.getInventoryItem());
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

    public boolean hasInventoryItem() {
        return this.getChildren().size > 1;
    }
}
