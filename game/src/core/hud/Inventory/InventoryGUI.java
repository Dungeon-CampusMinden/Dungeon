package core.hud.Inventory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;
import contrib.utils.components.item.ItemNature;

import core.Game;
import core.utils.Constants;

import java.util.List;

public class InventoryGUI extends Group {

    private static final InventoryGUI instance = new InventoryGUI();
    private final Dialog inventory;
    private final DragAndDrop dragAndDrop;
    private boolean isOpen = true;
    private final int INVENTORYSLOTS_IN_A_ROW = 9;

    /** Creates an inventory GUI as big as the inventory component of the hero */
    private InventoryGUI() {

        dragAndDrop = new DragAndDrop();

        String[] arrayOfMessages = {"Inventory"};
        // TODO: default skin laden
        inventory = new Dialog("Inventar", new Skin());
        //                createInventoryDialog(
        //                      new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
        // arrayOfMessages);

        inventory.setResizable(false);
        addActor(inventory);
        initInventorySlots();
        closeInventory();
    }

    /** Creates all inventory slots and adds them to the inventorySlots list */
    private void initInventorySlots() {
        InventoryComponent inventoryComponent =
                (InventoryComponent)
                        Game.hero().orElseThrow().fetch(InventoryComponent.class).orElseThrow();
        if (inventoryComponent == null) {
            inventory.remove();
            return;
        }
        int inventorySize = inventoryComponent.maxSize();
        // TODO: skin ersetzten
        InventoryDescription description = new InventoryDescription(new Skin());
        InventorySlot slot;

        for (int i = 1; i < inventorySize + 1; i++) {

            if (i == 1 || i == 4 || i == 7) {
                inventory.row().colspan(3);
            } else if (i == 10) {
                inventory.row().colspan(1);
            }

            // create a new slot speicific for the current iteration
            slot =
                    switch (i) {
                        case 1 -> new InventorySlot(ItemNature.NECKLACE);
                        case 2 -> new InventorySlot(ItemNature.HELMET);
                        case 3 -> new InventorySlot(ItemNature.GLOVES);
                        case 4 -> new InventorySlot(ItemNature.SHIELD);
                        case 5 -> new InventorySlot(ItemNature.ARMOR);
                        case 6 -> new InventorySlot(ItemNature.WEAPON);
                        case 7 -> new InventorySlot(ItemNature.BOOK);
                        case 8 -> new InventorySlot(ItemNature.PANTS);
                        case 9 -> new InventorySlot(ItemNature.RING);
                        default -> new InventorySlot();
                    };
            // ????
            if (i == 1 || i == 4 || i == 7) {
                inventory.add(slot).pad(9);
            } else {
                inventory.add(slot).pad(3);
            }

            slot.addListener(new InventoryDescriptionListener(description));
            // ?????
            if (i != 11 && i % INVENTORYSLOTS_IN_A_ROW == 0) {
                inventory.row();
            }

            dragAndDrop.addTarget(new InventorySlotTarget(slot, dragAndDrop));
        }
        inventory.getParent().addActor(description);
        inventory.pack();
        inventory.setPosition(
                Constants.viewportWidth() / 2f - inventory.getWidth() / 2f,
                Constants.viewportHeight() / 2f - inventory.getHeight() / 2f);
    }

    /** Updates the inventory based on the heros inventory component */
    public void updateInventory() {
        InventoryComponent inventoryComponent =
                (InventoryComponent)
                        Game.hero().flatMap(e -> e.fetch(InventoryComponent.class)).orElseThrow();

        List<ItemData> items = inventoryComponent.items();
        // removes items from list if the item is in the InventoryGUI
        // removes items from the InventoryGUI that are not present in the InventoryComponent
        for (Actor actors : inventory.getChildren()) {
            if (!(actors instanceof InventorySlot inventorySlot)) continue;
            if (inventorySlot.getInventoryItem() == null) continue;

            ItemData inventoryItem = inventorySlot.getInventoryItem().getItem();

            if (!inventoryComponent.items().contains(inventoryItem)) {
                inventorySlot.removeInventoryItem();
            } else {
                items.remove(inventoryItem);
            }
        }
        // adds new items to the inventory
        int selectionFieldNumber = 0;
        int maxNumberOfSelectionFields = 0;
        boolean considerSelectionFields = false;

        for (ItemData listItem : items) {
            for (Actor actors : inventory.getChildren()) {
                if (!(actors instanceof InventorySlot inventorySlot)) continue;

                if (inventorySlot.getInventoryItem() != null) {
                    continue;
                }

                if (listItem == null) {
                    selectionFieldNumber++;
                    break;
                } else {
                    if (maxNumberOfSelectionFields < selectionFieldNumber) {
                        maxNumberOfSelectionFields = selectionFieldNumber;
                    }

                    if (selectionFieldNumber == 0 && considerSelectionFields) {
                        selectionFieldNumber = maxNumberOfSelectionFields;
                        considerSelectionFields = false;
                    }

                    if (selectionFieldNumber > 0) {
                        selectionFieldNumber--;

                        if (inventorySlot.itemNature() != ItemNature.UNDEFINED) {
                            continue;
                        }
                    }

                    InventoryItem item =
                            new InventoryItem(
                                    listItem.inventoryTexture().nextAnimationTexturePath(),
                                    listItem);
                    inventorySlot.setInventoryItem(item);
                    dragAndDrop.addSource(new InventorySlotSource(inventorySlot, dragAndDrop));
                }

                considerSelectionFields = true;
                break;
            }
        }
        inventory.pack();
    }

    /** Toggles the visibility of the inventory */
    public void toggleInventory() {
        if (isOpen) closeInventory();
        else openInventory();
    }

    private void openInventory() {
        if (Game.hero().map(x -> x.fetch(InventoryComponent.class)).isEmpty()) return;
        updateInventory();
        inventory.setVisible(true);
        isOpen = true;
        // pause all system when open
    }

    private void closeInventory() {
        inventory.setVisible(false);
        isOpen = false;
        // unpause all systems when hidden
    }

    /**
     * Returns the instance of the InventoryGUI
     *
     * @return the instance of the InventoryGUI
     */
    public static InventoryGUI getInstance() {
        return instance;
    }

    /**
     * Returns the Window of the inventory
     *
     * @return the Window of the inventory
     */
    public Dialog getInventoryWindow() {
        return inventory;
    }

    /*
    obsolete Textdialog
    private TextDialog createInventoryDialog(Skin skin, String... arrayOfMessages) {
          String caption = "Inventory";

          if (arrayOfMessages.length > 0) caption = arrayOfMessages[0];

          return new TextDialog(skin, caption);
      }*/
}
