package core.hud.Inventory;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

import core.Game;
import core.hud.ScreenImage;
import core.utils.Constants;
import core.utils.Point;
import core.utils.controller.ScreenController;

import java.util.ArrayList;
import java.util.List;

public class InventoryGUI<T extends Actor> extends ScreenController<T> {

    private static final InventoryGUI<Actor> instance = new InventoryGUI<>(new SpriteBatch());
    private final int WIDTH = 330;
    private final int HEIGHT = 500;
    private final ArrayList<InventorySlot> inventorySlots;
    private final ScreenImage inventory;
    private final DragAndDrop dragAndDrop;
    private boolean isOpen = false;

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    private InventoryGUI(SpriteBatch batch) {
        super(batch);
        dragAndDrop = new DragAndDrop();
        inventorySlots = new ArrayList<>();
        inventory =
                new ScreenImage(
                        "animation/inventar.png",
                        new Point(
                                Constants.WINDOW_WIDTH - WIDTH - 230,
                                Constants.WINDOW_HEIGHT - HEIGHT + 100),
                        1);
        add((T) inventory);
        initInventorySlots();
    }

    /** Creates all inventory slots and adds them to the inventorySlots list */
    private void initInventorySlots() {
        InventoryComponent inventoryComponent =
                (InventoryComponent)
                        Game.getHero()
                                .orElseThrow()
                                .getComponent(InventoryComponent.class)
                                .orElse(null);
        if (inventoryComponent == null) {
            throw new NullPointerException("InventoryComponent is null");
        }
        int inventorySize = inventoryComponent.getMaxSize();

        for (int i = 0; i < inventorySize; i++) {
            int offsetY = i / 7;
            int offsetX = i % 7;
            InventorySlot slot =
                    new InventorySlot(
                            new Point(
                                    Constants.WINDOW_WIDTH - WIDTH - 220 + 70 * offsetX,
                                    Constants.WINDOW_HEIGHT - HEIGHT + 360 - 70 * offsetY));
            add((T) slot);
            inventorySlots.add(slot);
            dragAndDrop.addTarget(slot.getTarget());
        }
        dragAndDrop.setDragActorPosition(-10, -20);
    }

    /** Updates the inventory based on the heros inventory component */
    public void updateInventory() {
        InventoryComponent inventoryComponent =
                (InventoryComponent)
                        Game.getHero()
                                .orElseThrow()
                                .getComponent(InventoryComponent.class)
                                .orElse(null);
        if (inventoryComponent == null || inventoryComponent.getItems().isEmpty()) {
            return;
        }
        List<ItemData> items = inventoryComponent.getItems();
        // check if items have been added or removed
        for (InventorySlot inventorySlot : inventorySlots) {
            if (inventorySlot.getInventoryItem() == null) continue;
            // removes items that have been removed from the inventory
            if (!items.contains(inventorySlot.getItem())) {
                remove((T) inventorySlot.getInventoryItem());
                inventorySlot.setInventoryItem(null);
            }
            // removes items from the list that have been added to the inventory
            else items.remove(inventorySlot.getItem());
        }
        // adds new items to the inventory
        for (ItemData listItem : items) {
            InventoryItem item = null;
            for (InventorySlot inventorySlot : inventorySlots) {
                if (inventorySlot.getInventoryItem() == null) {
                    item =
                            new InventoryItem(
                                    listItem.getInventoryTexture().getNextAnimationTexturePath(),
                                    new Point(
                                            inventorySlot.getX(Align.bottomLeft),
                                            inventorySlot.getY(Align.bottomLeft)),
                                    3.5f,
                                    listItem);
                    inventorySlot.setInventoryItem(item);
                    break;
                }
            }
            add((T) item);
            item.setVisible(false);
            dragAndDrop.addSource(item.getSource());
        }
    }

    /** Debug method to print the inventory */
    private void print() {
        System.out.println();
        System.out.println(inventory.getPrefHeight() + " " + inventory.getPrefWidth());

        for (InventorySlot inventorySlot : inventorySlots) {
            System.out.println("Slot: " + inventorySlot.getInventoryItem());
        }
    }

    /** Opens the inventory */
    public void openInventory() {
        updateInventory();
        this.forEach((Actor s) -> s.setVisible(true));
        isOpen = true;
        //print();
    }

    /** Closes the inventory */
    public void closeInventory() {
        this.forEach((Actor s) -> s.setVisible(false));
        isOpen = false;
    }

    /**
     * Returns the instance of the InventoryGUI
     *
     * @return the instance of the InventoryGUI
     */
    public static InventoryGUI<Actor> getInstance() {
        return instance;
    }

    /**
     * Returns if the inventory is open
     *
     * @return if the inventory is open
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Returns the ScreenImage of the inventory
     *
     * @return the ScreenImage of the inventory
     */
    public ScreenImage getInventoryImage() {
        return inventory;
    }
}
