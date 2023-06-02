package core.hud.Inventory;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Null;

import contrib.utils.components.item.ItemData;
import contrib.utils.components.item.ItemType;

import core.Game;
import core.components.PositionComponent;
import core.hud.*;
import core.utils.Point;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InventoryItem extends ScreenImage {
    private DragAndDrop.Source source;
    private ItemData item;
    private Point startingPosition;
    private ScreenText itemDescription;
    private final List<ScreenButton> contextMenu;

    /**
     * Creates an inventory item with the given texture path, position, scale, and item data.
     *
     * @param texturePath The path to the texture of the item.
     * @param position The position of the item.
     * @param scale The scale of the item.
     * @param item The item data of the item.
     */
    public InventoryItem(String texturePath, Point position, float scale, ItemData item) {
        super(texturePath, position, scale);
        this.startingPosition = new Point(0, 0);
        this.item = item;
        contextMenu = new ArrayList<>();
        setupSource();
        setupMouseListener();
    }

    private void setupSource() {
        this.source =
                new DragAndDrop.Source(this) {
                    private InventorySlot tempSlot = null;

                    @Override
                    public DragAndDrop.Payload dragStart(
                            InputEvent event, float x, float y, int pointer) {
                        startingPosition = new Point(getActor().getX(), getActor().getY());
                        DragAndDrop.Payload payload = new DragAndDrop.Payload();
                        // Remove the item description
                        if (itemDescription != null)
                            InventoryGUI.getInstance().remove(itemDescription);
                        // Loads the Payload with the item
                        payload.setDragActor(getActor());

                        // Finds the slot that contains the item
                        for (Actor actor : getActor().getParent().getChildren()) {
                            if (actor instanceof InventorySlot) {
                                tempSlot = (InventorySlot) actor;
                                if (tempSlot.getInventoryItem() != null
                                        && tempSlot.getItem() == item) {
                                    break;
                                }
                            }
                        }
                        payload.setObject(tempSlot);
                        return payload;
                    }

                    /**
                     * On drag stop, if the item is not dropped on a valid target, and outside the
                     * inventory, drop the item. If the item is dropped on an invalid target, put it
                     * back into the inventory slot.
                     *
                     * @param event The event that triggered the drag stop.
                     * @param x The x coordinate of the drag.
                     * @param y The y coordinate of the drag.
                     * @param pointer The pointer for the drag.
                     * @param payload null if dragStart returned null.
                     * @param target null if not dropped on a valid target.
                     */
                    @Override
                    public void dragStop(
                            InputEvent event,
                            float x,
                            float y,
                            int pointer,
                            @Null DragAndDrop.Payload payload,
                            @Null DragAndDrop.Target target) {
                        if (target == null) {
                            // If the item is not in the inventory, drop it
                            if (!isInInventory(getActor())) {
                                ItemData item = ((InventoryItem) payload.getDragActor()).getItem();
                                PositionComponent pc =
                                        (PositionComponent)
                                                Game.getHero()
                                                        .orElseThrow()
                                                        .getComponent(PositionComponent.class)
                                                        .orElseThrow();
                                item.getOnDrop()
                                        .onDrop(
                                                Game.getHero().orElseThrow(),
                                                item,
                                                pc.getPosition());
                                getActor().remove();
                                tempSlot.setInventoryItem(null);
                            }
                            // If the item is in the inventory, put it back into the inventory slot
                            else {
                                getActor().setPosition(startingPosition.x, startingPosition.y);
                            }
                        }
                    }
                };
    }

    private void setupMouseListener() {
        InventoryGUI<Actor> invGUI = InventoryGUI.getInstance();
        this.addListener(
                new InputListener() {
                    Instant instant = Instant.now();
                    boolean helper = false;

                    /**
                     * On right click, create the context menu. On double left click, use the item.
                     *
                     * @param event The event that triggered this method.
                     * @param x The x position of the mouse.
                     * @param y The y position of the mouse.
                     * @param pointer The pointer of the mouse.
                     * @param button The button that was pressed.
                     * @return True if the event was handled, false otherwise.
                     */
                    @Override
                    public boolean touchDown(
                            InputEvent event, float x, float y, int pointer, int button) {
                        if (button == Input.Buttons.RIGHT) {
                            // Remove the context menu if it exists
                            if (contextMenu != null) {
                                contextMenu.forEach(invGUI::remove);
                                contextMenu.clear();
                            }
                            // Create the context menu
                            TextButtonStyleBuilder builder =
                                    new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT);
                            builder.setFontColor(Color.WHITE);
                            builder.setDownFontColor(Color.BLACK);
                            TextButton.TextButtonStyle style = builder.build();
                            if (item.getItemType() == ItemType.Active) {
                                contextMenu.add(
                                        new ScreenButton(
                                                "use",
                                                new Point(getX() + 5, getY() + 35),
                                                useButton,
                                                style));
                            }
                            contextMenu.add(
                                    new ScreenButton(
                                            "drop",
                                            new Point(getX() + 5, getY() + 20),
                                            dropButton,
                                            style));
                            contextMenu.add(
                                    new ScreenButton(
                                            "inspect",
                                            new Point(getX() + 5, getY() + 5),
                                            inspectButton,
                                            style));
                            contextMenu.forEach(screenButton -> screenButton.setScale(1));
                            contextMenu.forEach(invGUI::add);
                            helper = true;
                        }
                        // item gets used on Double click
                        else if (button == Input.Buttons.LEFT) {
                            if (instant.plusMillis(300).isAfter(Instant.now())) {
                                item.getOnUse().onUse(Game.getHero().orElseThrow(), item);
                                InventoryGUI.getInstance().updateInventory();
                            }
                            instant = Instant.now();
                        }
                        return true;
                    }

                    final TextButtonListener useButton =
                            new TextButtonListener() {
                                /**
                                 * Uses the item and removes the context menu
                                 *
                                 * @param event The event that triggered this method.
                                 * @param x The x position of the mouse.
                                 * @param y The y position of the mouse.
                                 */
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    // Use the item and update the inventory
                                    item.getOnUse().onUse(Game.getHero().orElseThrow(), item);
                                    contextMenu.forEach(invGUI::remove);
                                    contextMenu.clear();
                                    InventoryGUI.getInstance().updateInventory();
                                }
                            };

                    final TextButtonListener dropButton =
                            new TextButtonListener() {
                                /**
                                 * Drops the item and removes the context menu
                                 *
                                 * @param event The event that triggered this method.
                                 * @param x The x position of the mouse.
                                 * @param y The y position of the mouse.
                                 */
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    ItemData item = getItem();
                                    PositionComponent pc =
                                            (PositionComponent)
                                                    Game.getHero()
                                                            .orElseThrow()
                                                            .getComponent(PositionComponent.class)
                                                            .orElseThrow();
                                    item.getOnDrop()
                                            .onDrop(
                                                    Game.getHero().orElseThrow(),
                                                    item,
                                                    pc.getPosition());
                                    // TODO verbessern
                                    // Remove the item from the inventory slot
                                    for (Actor actor : invGUI) {
                                        if (actor instanceof InventorySlot slot) {
                                            if (slot.getInventoryItem() != null
                                                    && slot.getItem() == item) {
                                                slot.getInventoryItem().remove();
                                                slot.setInventoryItem(null);
                                                contextMenu.forEach(invGUI::remove);
                                                contextMenu.clear();
                                                break;
                                            }
                                        }
                                    }
                                }
                            };

                    final TextButtonListener inspectButton =
                            new TextButtonListener() {
                                /**
                                 * Shows the item description when the inspect button is clicked
                                 *
                                 * @param event The event that triggered the listener
                                 * @param x The x coordinate of the click
                                 * @param y The y coordinate of the click
                                 */
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    if (itemDescription != null) {
                                        invGUI.remove(itemDescription);
                                        itemDescription = null;
                                    }
                                    if (!invGUI.isOpen()) {
                                        return;
                                    }
                                    // Show the item description and removes the context menu
                                    itemDescription =
                                            new ScreenText(
                                                    item.getItemName()
                                                            + "\n"
                                                            + item.getDescription(),
                                                    new Point(getX(), getY() - 30),
                                                    1,
                                                    new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                                                            .setFontcolor(Color.WHITE)
                                                            .build());
                                    invGUI.add(itemDescription);
                                    contextMenu.forEach(invGUI::remove);
                                    contextMenu.clear();
                                }
                            };

                    /**
                     * Removes the item description and the context menu when the mouse leaves the
                     * InventorySlot
                     *
                     * @param event The event
                     * @param x The x coordinate of the mouse
                     * @param y The y coordinate of the mouse
                     * @param pointer The pointer
                     * @param toActor May be null.
                     */
                    @Override
                    public void exit(
                            InputEvent event, float x, float y, int pointer, Actor toActor) {
                        // Remove the item description if present
                        if (itemDescription != null) {
                            invGUI.remove(itemDescription);
                            itemDescription = null;
                        }
                        // Remove the context menu if present
                        // helper is used to prevent the context menu from being removed when right
                        // mouse button is pressed
                        if (helper) {
                            helper = false;
                        } else if (contextMenu != null) {
                            contextMenu.forEach(invGUI::remove);
                            contextMenu.clear();
                        }
                    }
                });
    }

    /**
     * Checks if the actor is in the inventory
     *
     * @param actor The actor to check
     * @return True if the actor is in the inventory, false otherwise
     */
    private boolean isInInventory(Actor actor) {
        ScreenImage inventory = InventoryGUI.getInstance().getInventoryImage();
        return actor.getX() > inventory.getX()
                && actor.getX() < inventory.getX() + inventory.getWidth()
                && actor.getY() > inventory.getY()
                && actor.getY() < inventory.getY() + inventory.getHeight();
    }

    /**
     * Sets the item of the inventory item
     *
     * @param item The item to set
     */
    public void setItem(ItemData item) {
        this.item = item;
    }

    /**
     * Gets the item of the inventory item
     *
     * @return The item of the inventory item
     */
    public ItemData getItem() {
        return this.item;
    }

    /**
     * Gets the source of the inventory item
     *
     * @return The source of the inventory item
     */
    public DragAndDrop.Source getSource() {
        return this.source;
    }

    /**
     * Returns the source of the inventory item
     *
     * @return The source of the inventory item
     */
    public Point getStartingPosition() {
        return this.startingPosition;
    }
}
