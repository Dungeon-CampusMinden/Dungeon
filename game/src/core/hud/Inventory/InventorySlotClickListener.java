package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class InventorySlotClickListener extends ClickListener {
    private final InventorySlot slot;

    /**
     * Creates an inventory slot click listener for the given inventory slot. When the inventory
     * slot is double-clicked, the item in the slot will be used.
     *
     * @param slot The inventory slot to listen to.
     */
    public InventorySlotClickListener(InventorySlot slot) {
        this.slot = slot;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        /*
        why is it uncommented ??
        if (getTapCount() == 2) {
            if (slot.hasInventoryItem() && slot.getInventoryItem().getItem().getOnUse() != null) {
                ItemData item = slot.getInventoryItem().getItem();
                item.triggerUse(Game.getHero().orElseThrow());
                InventoryGUI.getInstance().updateInventory();
            }
        }*/
    }
}
