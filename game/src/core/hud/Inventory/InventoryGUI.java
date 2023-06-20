package core.hud.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

import contrib.utils.components.item.ItemNature;
import core.Game;
import core.System;
import core.hud.TextDialog;
import core.systems.PlayerSystem;
import core.utils.Constants;
import core.utils.controller.ScreenController;

import java.util.List;

public class InventoryGUI<T extends Actor> extends ScreenController<T> {

    private static final InventoryGUI<Actor> instance = new InventoryGUI<>(new SpriteBatch());
    private final TextDialog inventory;
    private final DragAndDrop dragAndDrop;
    private boolean isOpen = true;
    private final int INVENTORYSLOTS_IN_A_ROW = 9;

    /**
     * Creates an inventory GUI as big as the inventory component of the hero
     *
     * @param batch the batch which should be used to draw with
     */
    private InventoryGUI(SpriteBatch batch) {
        super(batch);
        dragAndDrop = new DragAndDrop();

        String[] arrayOfMessages = { "Inventory" };
        inventory = createInventoryDialog(new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),arrayOfMessages );

        inventory.setResizable(false);
        add((T) inventory);
        initInventorySlots();
        closeInventory();
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
            this.remove((T) inventory);
            return;
        }
        int inventorySize = inventoryComponent.getMaxSize();

        InventoryDescription description = new InventoryDescription(Constants.inventorySkin);
        InventorySlot slot;

        for (int i = 1; i < inventorySize + 1; i++) {

            if( i== 1 ||  i== 4 || i== 7 )
            {
                inventory.row().colspan(3);
            }
            else if( i == 10)
            {
                inventory.row().colspan(1);
            }

            if(i== 1)
                slot = new InventorySlot(Constants.INVENTORYSLOT_NECKLACE_PATH, ItemNature.NECKLACE);
            else if(i== 2)
                slot = new InventorySlot(Constants.INVENTORYSLOT_HELMET_PATH, ItemNature.HELMET);
            else if(i== 3)
                slot = new InventorySlot(Constants.INVENTORYSLOT_GLOVES_PATH, ItemNature.GLOVES);
            else if(i== 4)
                slot = new InventorySlot(Constants.INVENTORYSLOT_SCHIELD_PATH, ItemNature.SHIELD);
            else if(i== 5)
                slot = new InventorySlot(Constants.INVENTORYSLOT_ARMOUR_PATH, ItemNature.ARMOR);
            else if(i== 6)
                slot = new InventorySlot(Constants.INVENTORYSLOT_SWORD_PATH, ItemNature.SWORD);
            else if(i== 7)
                slot = new InventorySlot(Constants.INVENTORYSLOT_BOOK_PATH, ItemNature.BOOK);
            else if(i== 8)
                slot = new InventorySlot(Constants.INVENTORYSLOT_SHOES_PATH, ItemNature.SHOES);
            else if(i== 9)
                slot = new InventorySlot(Constants.INVENTORYSLOT_RING_PATH, ItemNature.RING );
            else
                slot = new InventorySlot();

            if(i== 1 ||  i== 4 || i== 7 ) {
                inventory.add(slot).pad(9);
            }
            else {
                inventory.add(slot).pad(3);
            }

            slot.addListener(new InventoryDescriptionListener(description));

            if (i != 11 && i % INVENTORYSLOTS_IN_A_ROW == 0)
            {
                inventory.row();
            }

            dragAndDrop.addTarget(new InventorySlotTarget(slot, dragAndDrop));
        }
        inventory.getParent().addActor(description);
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
                                .flatMap(e -> e.getComponent(InventoryComponent.class))
                                .orElse(null);
        if (inventoryComponent == null) {
            return;
        }

        List<ItemData> items = inventoryComponent.getItems();
        // removes items from list if the item is in the InventoryGUI
        // removes items from the InventoryGUI that are not present in the InventoryComponent
        for (Actor actors : inventory.getChildren()) {
            if (!(actors instanceof InventorySlot inventorySlot)) continue;
            if (inventorySlot.getInventoryItem() == null) continue;

            ItemData inventoryItem = inventorySlot.getInventoryItem().getItem();

            if (!inventoryComponent.getItems().contains(inventoryItem)) {
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
                if (inventorySlot.getInventoryItem() != null ) continue;

                if( listItem == null )
                {
                    selectionFieldNumber++;
                    break;
                }
                else {
                    if( maxNumberOfSelectionFields < selectionFieldNumber ) {
                        maxNumberOfSelectionFields = selectionFieldNumber;
                    }

                    if( selectionFieldNumber == 0 && considerSelectionFields)
                    {
                        selectionFieldNumber = maxNumberOfSelectionFields;
                        considerSelectionFields = false;
                    }

                    if( selectionFieldNumber > 0 ){
                        selectionFieldNumber--;
                        continue;
                    }

                    InventoryItem item =
                        new InventoryItem(
                            listItem.getInventoryTexture().getNextAnimationTexturePath(),
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
        if (Game.getHero().orElseThrow().getComponent(InventoryComponent.class).isEmpty()) return;
        updateInventory();
        inventory.setVisible(true);
        isOpen = true;
        Game.systems.values().stream()
                .filter(s -> !(s instanceof PlayerSystem))
                .forEach(System::stop);
    }

    private void closeInventory() {
        inventory.setVisible(false);
        isOpen = false;
        Game.systems.values().forEach(System::run);
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
     * Returns the Window of the inventory
     *
     * @return the Window of the inventory
     */
    public TextDialog getInventoryWindow() {
        return inventory;
    }

    private TextDialog createInventoryDialog(Skin skin, String... arrayOfMessages) {
        String caption = "Inventory";

       if( arrayOfMessages.length > 0)
           caption = arrayOfMessages[0];

        return new TextDialog(skin, caption);
    }
}
