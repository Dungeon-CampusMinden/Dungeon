package core.hud.Inventory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import core.utils.Constants;
import core.utils.Point;

public class InventoryDescriptionListener extends InputListener {

    private final InventoryDescription description;
    private final Vector2 position;
    private final Point offSet;
    private Point defaultOffSet;
    private boolean isOver = false;

    /**
     * Creates a new InventoryDescriptionListener
     *
     * @param description the description Window to use
     */
    public InventoryDescriptionListener(InventoryDescription description) {
        this.description = description;
        this.position = new Vector2(0, 0);
        this.offSet = new Point(40, 0);
        this.defaultOffSet = new Point(offSet.x, offSet.y);
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        InventorySlot slot = (InventorySlot) event.getListenerActor();
        description.setVisible(slot, false);
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        InventorySlot inventorySlot = (InventorySlot) event.getListenerActor();
        Window inv = InventoryGUI.getInstance().getInventoryWindow();
        isOver = true;
        defaultOffSet = new Point(inventorySlot.getWidth(), inventorySlot.getHeight());

        position.set(inventorySlot.getX() + inv.getX(), inventorySlot.getY() + inv.getY());
        description.updateDescription(inventorySlot);
        this.updateOffset();

        description.setPosition(position.x + offSet.x, position.y + offSet.y);
        description.toFront();
        description.setVisible(inventorySlot, true);
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
        if (isOver) {
            InventorySlot inventorySlot = (InventorySlot) event.getListenerActor();
            Window inv = InventoryGUI.getInstance().getInventoryWindow();

            position.set(inventorySlot.getX() + inv.getX(), inventorySlot.getY() + inv.getY());
            description.updateDescription(inventorySlot);
            this.updateOffset();

            description.setPosition(position.x + offSet.x, position.y + offSet.y);
            description.toFront();
            description.setVisible(inventorySlot, true);
        }
        return false;
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        InventorySlot inventorySlot = (InventorySlot) event.getListenerActor();
        isOver = false;

        description.setVisible(inventorySlot, false);
    }

    // Positions the description screen to where it can fit on the screen
    private void updateOffset() {
        // does it fit on the right side?
        if (position.x + description.getWidth() + defaultOffSet.x < Constants.WINDOW_WIDTH) {
            offSet.x = defaultOffSet.x;
            offSet.y = 0;
        }
        // does it fit on the left side ?
        else if (position.x - description.getWidth() - defaultOffSet.x > 0) {
            offSet.x = -description.getWidth();
            offSet.y = 0;
        }
        // does it fit on top ?
        else if (position.y + description.getHeight() + defaultOffSet.x < Constants.WINDOW_HEIGHT) {
            offSet.x = Constants.WINDOW_WIDTH - position.x - description.getWidth();
            offSet.y = defaultOffSet.y;
        }
        // put it below
        else {
            offSet.x = Constants.WINDOW_WIDTH - position.x - description.getWidth();
            offSet.y = -description.getHeight();
        }
    }
}
