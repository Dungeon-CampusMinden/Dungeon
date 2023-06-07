package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import core.Game;
import core.components.PositionComponent;

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
        // Removes description from slot
        // this is used because dragStart somehow gets called before touchDragged
        ((InventoryDescriptionListener) inventorySlot.getListeners().get(1))
                .touchDragged(new InputEvent(), 0, 0, 0);

        payload.setDragActor(actor);
        // add the drag actor to the stage
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
        Window inv = InventoryGUI.getInstance().getInventoryWindow();
        InventoryItem itemActor = (InventoryItem) payload.getDragActor();

        if (target == null) {
            // if the item is dropped outside the inventory, drop it on the ground
            if (itemActor.getX() < inv.getX()
                    || itemActor.getX() > inv.getX() + inv.getWidth()
                    || itemActor.getY() < inv.getY()
                    || itemActor.getY() > inv.getY() + inv.getHeight()) {
                PositionComponent positionComponent =
                        (PositionComponent)
                                Game.getHero()
                                        .flatMap(e -> e.getComponent(PositionComponent.class))
                                        .orElseThrow();
                itemActor
                        .getItem()
                        .triggerDrop(Game.getHero().orElseThrow(), positionComponent.getPosition());
                itemActor.remove();
            } else {
                inventorySlot.add(payload.getDragActor());
            }
        }
    }

    public InventorySlot getSourceSlot() {
        return inventorySlot;
    }
}
