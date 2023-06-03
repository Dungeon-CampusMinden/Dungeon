package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class InventorySlotSource extends DragAndDrop.Source {
    private final InventorySlot inventorySlot;
    private final DragAndDrop dragAndDrop;

    /**
     * Creates a new inventory slot source with the given slot. Source gets removed from the slot
     * when dropped.
     *
     * @param slot The slot to create the source for.
     * @param dragAndDrop The drag and drop object to use.
     */
    public InventorySlotSource(InventorySlot slot, DragAndDrop dragAndDrop) {
        super(slot.getInventoryItem());
        this.inventorySlot = slot;
        this.dragAndDrop = dragAndDrop;
    }

    @Override
    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        DragAndDrop.Payload payload = new DragAndDrop.Payload();

        Actor actor = getActor();
        if (actor == null) {
            return null;
        }

        payload.setDragActor(actor);
        InventoryGUI.getInstance().getInventoryWindow().getParent().addActor(actor);
        dragAndDrop.setDragActorPosition(actor.getWidth() / 2, -actor.getHeight() / 2);
        return payload;
    }

    @Override
    public void dragStop(
            InputEvent event,
            float x,
            float y,
            int pointer,
            DragAndDrop.Payload payload,
            DragAndDrop.Target target) {
        if (target == null) {
            inventorySlot.add(payload.getDragActor());
        }
    }

    public InventorySlot getSourceSlot() {
        return inventorySlot;
    }
}
