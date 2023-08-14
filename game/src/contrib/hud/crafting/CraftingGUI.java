package contrib.hud.crafting;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import contrib.hud.CombinableGUI;
import contrib.hud.GUICombination;
import contrib.hud.inventory.ItemDragPayload;
import contrib.utils.components.item.ItemData;

import core.Game;
import core.utils.components.draw.Animation;
import core.utils.components.draw.TextureMap;

import java.io.File;
import java.util.ArrayList;

public class CraftingGUI extends CombinableGUI {

    // Position settings
    private static final int PADDING = 10;
    private static final int NUMBER_PADDING = 5;
    private static final int ITEM_GAP = 10;

    // Colors
    private static final int BORDER_COLOR = 0x9dc1ebff;
    private static final int BACKGROUND_COLOR = 0x3e3e63e1;
    private static final int HOVER_BACKGROUND_COLOR = 0xffffffff;
    private static final int NUMBER_BACKGROUND_COLOR = 0xd93030ff;

    private static final Texture texture;
    private static final TextureRegion background, hoverBackground, border, numberBackground;
    private static final Animation cauldronAnimation;
    private static final BitmapFont bitmapFont;

    static {
        cauldronAnimation = Animation.of(new File("./game/assets/objects/cauldron/idle"));

        Pixmap pixmap = new Pixmap(4, 1, Pixmap.Format.RGBA8888);
        pixmap.drawPixel(0, 0, BACKGROUND_COLOR);
        pixmap.drawPixel(1, 0, HOVER_BACKGROUND_COLOR);
        pixmap.drawPixel(2, 0, BORDER_COLOR);
        pixmap.drawPixel(3, 0, NUMBER_BACKGROUND_COLOR);

        texture = new Texture(pixmap);
        background = new TextureRegion(texture, 0, 0, 1, 1);
        hoverBackground = new TextureRegion(texture, 1, 0, 1, 1);
        border = new TextureRegion(texture, 2, 0, 1, 1);
        numberBackground = new TextureRegion(texture, 3, 0, 1, 1);

        // Init Font
        bitmapFont =
                new BitmapFont(
                        new FileHandle("./game/assets/skin/myFont.fnt"),
                        new FileHandle("./game/assets/skin/myFont.png"),
                        false);
    }

    private ArrayList<ItemData> items = new ArrayList<>();

    @Override
    protected void initDragAndDrop(DragAndDrop dragAndDrop) {
        dragAndDrop.addTarget(
                new DragAndDrop.Target(this.actor()) {
                    @Override
                    public boolean drag(
                            DragAndDrop.Source source,
                            DragAndDrop.Payload payload,
                            float x,
                            float y,
                            int pointer) {
                        if (payload != null
                                && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
                            return itemDragPayload.itemData() != null;
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
                        if (payload != null
                                && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
                            CraftingGUI.this.items.add(itemDragPayload.itemData());
                            CraftingGUI.this.updateRecipe();
                        }
                    }
                });
    }

    @Override
    protected Vector2 preferredSize(GUICombination.AvailableSpace availableSpace) {
        int size =
                Math.round(
                        Math.min(
                                availableSpace.height(),
                                (Game.stage().orElseThrow().getHeight() / 4) * 3));
        if (size > availableSpace.width()) {
            size = availableSpace.width();
        }
        return new Vector2(size, size);
    }

    @Override
    protected void draw(Batch batch) {

        // Draw background
        batch.draw(background, this.x(), this.y(), this.width(), this.height());

        // Draw border
        batch.draw(border, this.x(), this.y(), this.width(), 1);
        batch.draw(border, this.x(), this.y(), 1, this.height());
        batch.draw(border, this.x() + this.width() - 1, this.y(), 1, this.height());
        batch.draw(border, this.x(), this.y() + this.height() - 1, this.width(), 1);

        this.drawCauldron(batch);
        this.drawItems(batch);
        this.drawButtons(batch);
    }

    private void drawCauldron(Batch batch) {

        int size = (this.height() / 3) * 2;
        int x = this.x() + this.width() / 2 - size / 2;
        int y = this.y() + PADDING;

        batch.draw(
                TextureMap.instance().textureAt(cauldronAnimation.nextAnimationTexturePath()),
                x,
                y,
                size,
                size);
    }

    private void drawItems(Batch batch) {
        if (this.items.isEmpty()) {
            return;
        }

        int size =
                Math.min(
                        this.height() / 3 - 2 * PADDING,
                        (this.width() - this.items.size() * ITEM_GAP) / (this.items.size()));
        int rowWidth = size * this.items.size() + ITEM_GAP * (this.items.size() + 1);
        int startX = this.x() + (this.width() / 2) - (rowWidth / 2);
        int startY = this.y() + (this.height() / 6) * 5 - size / 2;

        for (int i = 0; i < this.items.size(); i++) {
            Texture itemTexture =
                    TextureMap.instance()
                            .textureAt(
                                    this.items
                                            .get(i)
                                            .item()
                                            .inventoryAnimation()
                                            .nextAnimationTexturePath());
            int textureX = startX + ITEM_GAP * (i + 1) + size * i;
            batch.draw(itemTexture, textureX, startY, size, size);

            GlyphLayout layout = new GlyphLayout(bitmapFont, Integer.toString(i + 1));
            int boxX = textureX + (size / 2) - Math.round((layout.height / 2)) - NUMBER_PADDING;
            int boxY = startY - NUMBER_PADDING;
            batch.draw(
                    numberBackground,
                    boxX,
                    boxY,
                    layout.height + 2 * NUMBER_PADDING,
                    layout.height + 2 * NUMBER_PADDING);

            bitmapFont.draw(
                    batch,
                    Integer.toString(i + 1),
                    boxX + NUMBER_PADDING,
                    boxY + NUMBER_PADDING + layout.height,
                    layout.width,
                    Align.left,
                    false);
        }
    }

    private void drawButtons(Batch batch) {
        // TODO: Implement
    }

    private void updateRecipe() {
        // TODO: Implement
    }
}
