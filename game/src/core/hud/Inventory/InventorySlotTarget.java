package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.utils.components.item.ItemNature;

public class InventorySlotTarget extends DragAndDrop.Target {

    private final InventorySlot inventorySlot;
    private final DragAndDrop dragAndDrop;

    /**
     * Creates an inventory slot target with the given slot.
     *
     * @param slot The slot to create the target for.
     * @param dragAndDrop The drag and drop object to use.
     */
    public InventorySlotTarget(InventorySlot slot, DragAndDrop dragAndDrop) {
        super(slot);
        this.inventorySlot = slot;
        this.dragAndDrop = dragAndDrop;
    }

    @Override
    public boolean drag(
            DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        return true;
    }

    @Override
    public void drop(
            DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        InventoryItem sourceActor = (InventoryItem) payload.getDragActor();
        InventorySlot sourceSlot = ((InventorySlotSource) source).getSourceSlot();

        if (sourceActor == null) {
            return;
        }

        boolean belongsToAreaOfValidity = false;

        if (!inventorySlot.hasInventoryItem()) {
            if (sourceActor != null) {
                final ItemNature natureSlot = inventorySlot.itemNature();
                final ItemNature natureItem = sourceActor.itemNature();

                if (natureItem == natureSlot || natureSlot == ItemNature.UNDEFINED) {
                    belongsToAreaOfValidity = true;
                }
            }
        }

        if (belongsToAreaOfValidity) {
            inventorySlot.add(sourceActor);
            dragAndDrop.addSource(new InventorySlotSource(inventorySlot, dragAndDrop));
            dragAndDrop.removeSource(source);
        } else {
            sourceSlot.add(sourceActor);
        }
    }
}
