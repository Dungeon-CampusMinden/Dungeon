package core.hud.Inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import contrib.utils.components.item.ItemData;

public class InventoryItem extends Image {
    private ItemData item;

    /**
     * Creates an inventory item with the given texture path and item data.
     *
     * @param texturePath The path to the texture of the item.
     * @param item The item data of the item.
     */
    public InventoryItem(String texturePath, ItemData item) {
        super(new Texture(texturePath));
        this.item = item;
    }

    /**
     * Sets the item of the inventory item
     *
     * @param item The item to set
     */
    public void setItem(ItemData item) {
        this.item = item;
    }

    /**
     * Gets the item of the inventory item
     *
     * @return The item of the inventory item
     */
    public ItemData getItem() {
        return this.item;
    }
}
