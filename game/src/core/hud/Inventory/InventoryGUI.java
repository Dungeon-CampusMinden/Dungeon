package core.hud.Inventory;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

import core.Game;
import core.utils.Constants;
import core.utils.controller.ScreenController;

import java.util.List;

public class InventoryGUI<T extends Actor> extends ScreenController<T> {

    private static final InventoryGUI<Actor> instance = new InventoryGUI<>(new SpriteBatch());
    private final Window inventory;
    private final DragAndDrop dragAndDrop;
    private boolean isOpen = false;
    private final int INVENTORYSLOTS_IN_A_ROW = 5;

    /**
     * Creates an inventory GUI as big as the inventory component of the hero
     *
     * @param batch the batch which should be used to draw with
     */
    private InventoryGUI(SpriteBatch batch) {
        super(batch);
        dragAndDrop = new DragAndDrop();
        inventory = new Window("", Constants.inventoryUI);
        inventory.setResizable(false);
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

        for (int i = 1; i < inventorySize + 1; i++) {
            InventorySlot slot = new InventorySlot();
            inventory.add(slot).pad(3);
            if (i % INVENTORYSLOTS_IN_A_ROW == 0) inventory.row();
            dragAndDrop.addTarget(new InventorySlotTarget(slot, dragAndDrop));
        }
        inventory.pack();
        inventory.setPosition(
                Constants.WINDOW_WIDTH / 2f - inventory.getWidth() / 2f,
                Constants.WINDOW_HEIGHT / 2f - inventory.getHeight() / 2f);
    }

    /** Updates the inventory based on the heros inventory component */
    public void updateInventory() {
        InventoryComponent inventoryComponent =
                (InventoryComponent)
                        Game.getHero()
                                .orElseThrow()
                                .getComponent(InventoryComponent.class)
                                .orElse(null);
        if (inventoryComponent == null) {
            return;
        }
        List<ItemData> items = inventoryComponent.getItems();
        // check if items have been added or removed
        for (Actor actors : inventory.getChildren()) {
            if (!(actors instanceof InventorySlot invent)) continue;
            if (invent.getInventoryItem() == null) continue;
            // removes items that have been removed from the inventory
            if (!items.contains(invent.getInventoryItem().getItem())) {
                invent.removeInventoryItem();
            }
            // removes items from the list that have been added to the inventory
            else items.remove(invent.getInventoryItem().getItem());
        }
        // adds new items to the inventory
        for (ItemData listItem : items) {
            InventoryItem item;
            for (Actor actors : inventory.getChildren()) {
                if (!(actors instanceof InventorySlot inventorySlot)) continue;
                if (inventorySlot.getInventoryItem() == null) {
                    item =
                            new InventoryItem(
                                    listItem.getInventoryTexture().getNextAnimationTexturePath(),
                                    listItem);
                    inventorySlot.setInventoryItem(item);
                    dragAndDrop.addSource(new InventorySlotSource(inventorySlot, dragAndDrop));
                    break;
                }
            }
        }
        inventory.pack();
    }

    /** Debug method to print the inventory */
    private void print() {
        System.out.println();
        System.out.println(inventory.getPrefHeight() + " " + inventory.getPrefWidth());

        for (Actor inventorySlot : getInventoryWindow().getChildren()) {
            if (inventorySlot instanceof InventorySlot inv)
                System.out.println("Slot: " + inv.getInventoryItem());
        }
    }

    /** Opens the inventory */
    public void openInventory() {
        updateInventory();
        inventory.setVisible(true);
        isOpen = true;
        // print();
    }

    /** Closes the inventory */
    public void closeInventory() {
        inventory.setVisible(false);
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
    public Window getInventoryWindow() {
        return inventory;
    }
}
