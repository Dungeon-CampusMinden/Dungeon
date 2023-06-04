package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class InventoryDescription extends Label {

    /**
     * Creates a new InventoryDescription Window with the given Skin
     *
     * @param skin the Skin to use
     */
    public InventoryDescription(Skin skin) {
        super("", skin);
        this.pack();
        this.setVisible(false);
    }

    /**
     * Sets the visibility of the InventoryDescription Window
     *
     * @param slot the InventorySlot to get the visibility from
     * @param visible the visibility to set
     */
    public void setVisible(InventorySlot slot, boolean visible) {
        super.setVisible(visible);
        if (slot == null) {
            return;
        }

        if (slot.getInventoryItem() == null) {
            super.setVisible(false);
        }
    }

    /**
     * Updates the description Label with the given InventorySlot
     *
     * @param slot the InventorySlot to get the description from
     */
    public void updateDescription(InventorySlot slot) {
        if (slot.getInventoryItem() == null) {
            return;
        }
        String descriptionText = slot.getInventoryItem().getItem().getItemName();
        descriptionText += "\n" + slot.getInventoryItem().getItem().getDescription();
        this.setText(descriptionText);
        this.pack();
    }
}
