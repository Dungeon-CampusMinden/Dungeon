package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import core.Game;

public class InventorySlotClickListener extends ClickListener {
    private final InventorySlot slot;

    /**
     * Creates an inventory slot click listener with the given inventory slot. When the inventory
     * slot is double-clicked, the item in the slot will be used.
     *
     * @param slot The inventory slot to listen to.
     */
    public InventorySlotClickListener(InventorySlot slot) {
        this.slot = slot;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        if (getTapCount() == 2) {
            if (slot.hasInventoryItem() && slot.getInventoryItem().getItem().getOnUse() != null) {
                InventoryItem item = slot.getInventoryItem();
                item.getItem().triggerUse(Game.getHero().orElseThrow());
                InventoryGUI.getInstance().updateInventory();
            }
        }
    }
}
