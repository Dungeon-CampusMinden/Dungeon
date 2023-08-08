package contrib.hud.inventory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import contrib.components.InventoryComponent;

import core.Game;

public class InventoryGUI extends WidgetGroup {

    private static final Texture texture;
    private static final TextureRegion background;
    private static final int MAX_ITEMS_PER_ROW = 8;
    private static final int BORDER_WIDTH = 2;
    private static final int BORDER_COLOR = 0x9dc1ebff;
    private static final int BORDER_PADDING = 5;

    static {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.drawPixel(0, 0, 0x3e3e63e1); // Background
        texture = new Texture(pixmap);
        background = new TextureRegion(texture, 0, 0, 1, 1);
    }

    private InventoryComponent inventoryComponent;
    private DragAndDrop dragAndDrop;
    private Texture textureFields;
    private String title;

    public InventoryGUI(InventoryComponent inventoryComponent, DragAndDrop dragAndDrop) {
        super();
        this.inventoryComponent = inventoryComponent;
        this.dragAndDrop = dragAndDrop;
        this.title = "Inventory";
        this.update();
    }

    public InventoryGUI(
            String title, InventoryComponent inventoryComponent, DragAndDrop dragAndDrop) {
        super();
        this.inventoryComponent = inventoryComponent;
        this.dragAndDrop = dragAndDrop;
        this.title = title;
        this.update();
    }

    public void update() {
        this.clearChildren();
        float itemSize = getWidth() / (float) MAX_ITEMS_PER_ROW;
        for (int i = 0; i < inventoryComponent.items().length; i++) {
            if (inventoryComponent.items()[i] == null) continue;
            ItemActor itemActor = new ItemActor(inventoryComponent.items()[i]);
            int column = i % MAX_ITEMS_PER_ROW;
            int row = i / MAX_ITEMS_PER_ROW;
            itemActor.setPosition(
                    column * itemSize + 2 * BORDER_PADDING, row * itemSize + 2 * BORDER_PADDING);
            this.addActor(itemActor);
        }
    }

    @Override
    public void layout() {
        this.update();
        super.layout();
        float itemSize = getWidth() / (float) MAX_ITEMS_PER_ROW - 4 * BORDER_PADDING;
        this.getChildren()
                .forEach(
                        actor -> {
                            if (actor instanceof ItemActor itemActor) {
                                itemActor.setSize(itemSize, itemSize);
                                // itemActor.setPosition(actor.getX() + (itemSize / 2.0f),
                                // actor.getY() + (itemSize / 2.0f), Align.center);
                            }
                        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // super.draw(batch, parentAlpha);

        // Cache the texture fields
        if (this.textureFields == null
                || this.textureFields.getWidth() != this.getWidth()
                || this.textureFields.getHeight() != this.getHeight()) {
            if (this.textureFields != null) this.textureFields.dispose();

            Pixmap pixmap =
                    new Pixmap(
                            (int) this.getWidth(), (int) this.getHeight(), Pixmap.Format.RGBA8888);
            pixmap.setColor(BORDER_COLOR);
            int rows =
                    (int)
                            Math.ceil(
                                    this.inventoryComponent.items().length
                                            / (float) MAX_ITEMS_PER_ROW);
            int itemSize = (int) getWidth() / MAX_ITEMS_PER_ROW;
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
            this.textureFields = new Texture(pixmap);
        }

        batch.draw(background, getX(), getY(), getWidth(), getHeight());
        batch.draw(this.textureFields, getX(), getY(), getWidth(), getHeight());

        // Draw Items
        /*for (int i = 0; i < inventoryComponent.items().length; i++) {
            if(inventoryComponent.items()[i] == null) continue;
            float x = getX() + BORDER_WIDTH + itemWidth * (i % ITEMS_PER_ROW);
            float y = getY() + BORDER_WIDTH + itemHeight * (i / ITEMS_PER_ROW);
            batch.draw(new Texture(inventoryComponent.items()[i].item().inventoryAnimation().nextAnimationTexturePath()), x, y, itemWidth, itemHeight);
        }*/
        super.draw(batch, parentAlpha);
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
}
