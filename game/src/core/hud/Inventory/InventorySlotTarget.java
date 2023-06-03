package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

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

        if (!inventorySlot.hasInventoryItem()) {
            inventorySlot.add(sourceActor);
            dragAndDrop.addSource(new InventorySlotSource(inventorySlot, dragAndDrop));
            dragAndDrop.removeSource(source);
        } else {
            sourceSlot.add(sourceActor);
        }
    }
}
