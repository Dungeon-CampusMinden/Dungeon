package contrib.hud.inventory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.components.InventoryComponent;
import contrib.hud.GUI;
import contrib.utils.components.item.ItemData;

import core.Game;

public class InventoryGUI extends GUI {

    private static final Texture texture;
    private static final TextureRegion background;
    private static final int MAX_ITEMS_PER_ROW = 8;
    private static final int BORDER_COLOR = 0x9dc1ebff;
    private static final int BORDER_PADDING = 5;

    static {
        // Prepare background texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.drawPixel(0, 0, 0x3e3e63e1); // Background
        texture = new Texture(pixmap);
        background = new TextureRegion(texture, 0, 0, 1, 1);
    }

    private final InventoryComponent inventoryComponent;
    private Texture textureSlots;
    private String title;

    /**
     * Create a new inventory GUI
     *
     * @param title the title of the inventory
     * @param inventoryComponent the inventory component on which the GUI is based.
     */
    public InventoryGUI(String title, InventoryComponent inventoryComponent) {
        super();
        this.inventoryComponent = inventoryComponent;
        this.title = title;
    }

    /**
     * Create a new inventory GUI
     *
     * @param inventoryComponent the inventory component on which the GUI is based.
     */
    public InventoryGUI(InventoryComponent inventoryComponent) {
        this("Inventory", inventoryComponent);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw & cache slot squares
        this.drawSlots();

        // Draw Background & Slots
        batch.draw(background, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        batch.draw(this.textureSlots, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        // Draw Items
        this.drawItems(batch);
    }

    private void drawItems(Batch batch) {
        float slotSize = this.getWidth() / MAX_ITEMS_PER_ROW;
        for (int i = 0; i < this.inventoryComponent.items().length; i++) {
            if (this.inventoryComponent.items()[i] == null) continue;
            float x = this.getX() + slotSize * (i % MAX_ITEMS_PER_ROW) + 2 * BORDER_PADDING;
            float y =
                    this.getY()
                            + slotSize * (float) Math.floor((i / (float) MAX_ITEMS_PER_ROW))
                            + 2 * BORDER_PADDING;
            batch.draw(
                    new Texture(
                            this.inventoryComponent
                                    .items()[i]
                                    .item()
                                    .inventoryAnimation()
                                    .nextAnimationTexturePath()),
                    x,
                    y,
                    slotSize - 4 * BORDER_PADDING,
                    slotSize - 4 * BORDER_PADDING);
        }
    }

    private void drawSlots() {
        if (this.textureSlots == null
                || this.textureSlots.getWidth() != this.getWidth()
                || this.textureSlots.getHeight() != this.getHeight()) {
            if (this.textureSlots != null) this.textureSlots.dispose();

            // Minimized windows have 0 width and height -> container will be 0x0 -> Crash on pixmap
            // creation
            if ((int) this.getWidth() <= 0 || (int) this.getHeight() <= 0) return;

            Pixmap pixmap =
                    new Pixmap(
                            (int) this.getWidth(), (int) this.getHeight(), Pixmap.Format.RGBA8888);
            pixmap.setColor(BORDER_COLOR);
            int rows =
                    (int)
                            Math.ceil(
                                    this.inventoryComponent.items().length
                                            / (float) MAX_ITEMS_PER_ROW);
            int itemSize = (int) this.getWidth() / MAX_ITEMS_PER_ROW;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < MAX_ITEMS_PER_ROW; x++) {
                    if (x + y * MAX_ITEMS_PER_ROW >= this.inventoryComponent.items().length) break;
                    pixmap.drawRectangle(
                            x * itemSize + BORDER_PADDING,
                            pixmap.getHeight() - (y * itemSize + BORDER_PADDING),
                            itemSize - 2 * BORDER_PADDING,
                            -itemSize + 2 * BORDER_PADDING);
                }
            }
            this.textureSlots = new Texture(pixmap);
        }
    }

    @Override
    protected void initDragAndDrop(DragAndDrop dragAndDrop) {
        dragAndDrop.addSource(
                new DragAndDrop.Source(InventoryGUI.this) {
                    @Override
                    public DragAndDrop.Payload dragStart(
                            InputEvent event, float x, float y, int pointer) {
                        float slotSize = InventoryGUI.this.getWidth() / MAX_ITEMS_PER_ROW;
                        int slot = (int) (x / slotSize) + (int) (y / slotSize) * MAX_ITEMS_PER_ROW;
                        ItemData item = InventoryGUI.this.inventoryComponent.get(slot);
                        if (item == null) return null;

                        DragAndDrop.Payload payload = new DragAndDrop.Payload();
                        payload.setObject(
                                new ItemDragPayload(
                                        InventoryGUI.this.inventoryComponent, slot, item));

                        Image image =
                                new Image(
                                        new Texture(
                                                item.item()
                                                        .inventoryAnimation()
                                                        .nextAnimationTexturePath()));
                        image.setSize(slotSize, slotSize);
                        payload.setDragActor(image);
                        dragAndDrop.setDragActorPosition(
                                image.getWidth() / 2, -image.getHeight() / 2);

                        InventoryGUI.this.inventoryComponent.set(slot, null);

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
                        if (target == null
                                && payload != null
                                && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
                            InventoryGUI.this.inventoryComponent.set(
                                    itemDragPayload.slot(),
                                    (itemDragPayload
                                            .itemData())); // reset item to original slot if not
                            // dropped on target
                        }
                    }
                });

        dragAndDrop.addTarget(
                new DragAndDrop.Target(InventoryGUI.this) {
                    @Override
                    public boolean drag(
                            DragAndDrop.Source source,
                            DragAndDrop.Payload payload,
                            float x,
                            float y,
                            int pointer) {
                        if (payload.getObject() != null
                                && payload.getObject() instanceof ItemDragPayload) {
                            float slotSize = InventoryGUI.this.getWidth() / MAX_ITEMS_PER_ROW;
                            int slot =
                                    (int) (x / slotSize) + (int) (y / slotSize) * MAX_ITEMS_PER_ROW;
                            return InventoryGUI.this.inventoryComponent.get(slot) == null
                                    && slot < InventoryGUI.this.inventoryComponent.items().length
                                    && slot >= 0;
                        }
                        return false;
                    }

                    @Override
                    public void drop(
                            DragAndDrop.Source source,
                            DragAndDrop.Payload payload,
                            float x,
                            float y,
                            int pointer) {
                        float slotSize = InventoryGUI.this.getWidth() / MAX_ITEMS_PER_ROW;
                        int slot = (int) (x / slotSize) + (int) (y / slotSize) * MAX_ITEMS_PER_ROW;
                        if (payload.getObject() != null
                                && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
                            InventoryGUI.this.inventoryComponent.set(
                                    slot, itemDragPayload.itemData());
                        }
                    }
                });
    }

    @Override
    public float getMinWidth() {
        return 50;
    }

    @Override
    public float getMinHeight() {
        return 50;
    }

    @Override
    public float getPrefWidth() {
        int nrOfChildren = this.getParent().getChildren().size;
        return Math.min(
                Game.stage().orElseThrow().getHeight() * 0.9f,
                Game.stage().orElseThrow().getWidth() / (float) nrOfChildren
                        - 30.0f); // -30.0f um etwas Platz um die Inventories zu lassen (Padding)
    }

    @Override
    public float getPrefHeight() {
        return (float) Math.ceil(this.inventoryComponent.items().length / (float) MAX_ITEMS_PER_ROW)
                * (this.getWidth() / MAX_ITEMS_PER_ROW);
    }

    @Override
    public float getMaxWidth() {
        return Game.stage().orElseThrow().getHeight() * 0.9f;
    }

    @Override
    public float getMaxHeight() {
        return this.getMaxWidth();
    }

    /**
     * Get the displayed title of this InventoryGUI
     *
     * @return title of the inventory
     */
    public String title() {
        return this.title;
    }

    /**
     * Set the displayed title of this InventoryGUI
     *
     * @param title title of the inventory
     */
    public void title(String title) {
        this.title = title;
    }
}
