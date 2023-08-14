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

import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.hud.CombinableGUI;
import contrib.hud.GUICombination;
import contrib.hud.ImageButton;
import contrib.hud.inventory.ItemDragPayload;
import contrib.utils.components.item.ItemData;

import core.Game;
import core.utils.components.draw.Animation;
import core.utils.components.draw.TextureMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** */
public class CraftingGUI extends CombinableGUI {

    // Position settings
    private static final int NUMBER_PADDING = 5;
    private static final int ITEM_GAP = 10;

    // Positioning and sizing
    // These values should fit the background texture of the crafting GUI and should be between 0
    // and 1.
    // 0 is the bottom left corner and 1 is the top right corner.

    // X coordinate of the center of the input item row.
    private static final float INPUT_ITEMS_X = 0.5f;

    // Y coordinate of the bottom edge of the input item row.
    private static final float INPUT_ITEMS_Y = 0.775f;

    // The size is based on the height of the crafting GUI and items are always square.
    private static final float INPUT_ITEMS_MAX_SIZE = 0.2f;

    // X coordinate of the center of the result item.
    private static final float RESULT_ITEM_X = 0.5f;

    // Y coordinate of the bottom edge of the result item.
    private static final float RESULT_ITEM_Y = 0.219f;

    // The size is based on the height of the crafting GUI and items are always square.
    private static final float RESULT_ITEM_MAX_SIZE = 0.1f;

    private static final float BUTTON_OK_X = 0.812f;
    private static final float BUTTON_OK_Y = 0.05f;
    private static final float BUTTON_OK_WIDTH = 0.15f;
    private static final float BUTTON_OK_HEIGHT = 0.15f;

    private static final float BUTTON_CANCEL_X = 0.036f;
    private static final float BUTTON_CANCEL_Y = 0.05f;
    private static final float BUTTON_CANCEL_WIDTH = 0.15f;
    private static final float BUTTON_CANCEL_HEIGHT = 0.15f;

    // Colors
    private static final int NUMBER_BACKGROUND_COLOR = 0xd93030ff;

    private static final Texture texture;
    private static final TextureRegion numberBackground;
    private static final Animation backgroundAnimation;
    private static final BitmapFont bitmapFont;

    static {
        backgroundAnimation = Animation.of(new File("./game/assets/hud/crafting/idle"));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.drawPixel(0, 0, NUMBER_BACKGROUND_COLOR);

        texture = new Texture(pixmap);
        numberBackground = new TextureRegion(texture, 0, 0, 1, 1);

        // Init Font
        bitmapFont =
                new BitmapFont(
                        new FileHandle("./game/assets/skin/myFont.fnt"),
                        new FileHandle("./game/assets/skin/myFont.png"),
                        false);
    }

    private final ArrayList<ItemData> items = new ArrayList<>();
    private Recipe currentRecipe = null;
    private final ImageButton buttonOk, buttonCancel;
    private final InventoryComponent targetInventory;

    public CraftingGUI(InventoryComponent targetInventory) {
        this.targetInventory = targetInventory;
        this.buttonOk = new ImageButton(this, Animation.of("hud/check.png"), 0, 0, 1, 1);
        this.buttonCancel = new ImageButton(this, Animation.of("hud/cross.png"), 0, 0, 1, 1);
        this.buttonOk.onClick(
                (button) -> {
                    this.craft();
                });
        this.buttonCancel.onClick(
                (button) -> {
                    this.cancel();
                });
    }

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
    protected void boundsUpdate() {
        this.buttonOk.width(Math.round(this.width() * BUTTON_OK_WIDTH));
        this.buttonOk.height(Math.round(this.height() * BUTTON_OK_HEIGHT));
        this.buttonOk.x(this.x() + Math.round(this.width() * BUTTON_OK_X));
        this.buttonOk.y(this.y() + Math.round(this.height() * BUTTON_OK_Y));

        this.buttonCancel.width(Math.round(this.width() * BUTTON_CANCEL_WIDTH));
        this.buttonCancel.height(Math.round(this.height() * BUTTON_CANCEL_HEIGHT));
        this.buttonCancel.x(this.x() + Math.round(this.width() * BUTTON_CANCEL_X));
        this.buttonCancel.y(this.y() + Math.round(this.height() * BUTTON_CANCEL_Y));
    }

    @Override
    protected void draw(Batch batch) {
        // Draw background
        batch.draw(
                TextureMap.instance().textureAt(backgroundAnimation.nextAnimationTexturePath()),
                this.x(),
                this.y(),
                this.width(),
                this.height());

        this.drawItems(batch);

        this.buttonOk.draw(batch);
        this.buttonCancel.draw(batch);
    }

    /**
     * Draws the items that have been added to the cauldron.
     *
     * @param batch The batch to draw to.
     */
    private void drawItems(Batch batch) {
        if (this.items.isEmpty()) {
            return;
        }

        // Draw inserted items
        {
            int size =
                    Math.min(
                            Math.round(this.height() * INPUT_ITEMS_MAX_SIZE),
                            (this.width() - this.items.size() * ITEM_GAP) / this.items.size());
            int rowWidth = size * this.items.size() + ITEM_GAP * (this.items.size() + 1);
            int startX = this.x() + Math.round(this.width() * INPUT_ITEMS_X) - rowWidth / 2;
            int startY = this.y() + Math.round(this.height() * INPUT_ITEMS_Y);

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
                        Align.center,
                        false);
            }
        }

        // Draw result if present
        {
            if (this.currentRecipe == null) {
                return;
            }

            int nrItemResults =
                    (int)
                            Arrays.stream(this.currentRecipe.results())
                                    .filter(
                                            result ->
                                                    result.resultType() == CraftingType.ITEM
                                                            && result instanceof ItemData)
                                    .count();
            if (nrItemResults == 0) {
                return;
            }

            int size =
                    Math.min(
                            Math.round(this.height() * RESULT_ITEM_MAX_SIZE),
                            (this.width() - nrItemResults * ITEM_GAP) / nrItemResults);
            int rowWidth = size * nrItemResults + ITEM_GAP * (nrItemResults + 1);
            int x = this.x() + Math.round(this.width() * RESULT_ITEM_X) - rowWidth / 2;
            int y = this.y() + Math.round(this.height() * RESULT_ITEM_Y);

            int i = 0;
            for (CraftingResult result : this.currentRecipe.results()) {
                if (result.resultType() != CraftingType.ITEM
                        || !(result instanceof ItemData item)) {
                    continue;
                }
                Texture itemTexture =
                        TextureMap.instance()
                                .textureAt(
                                        item.item()
                                                .inventoryAnimation()
                                                .nextAnimationTexturePath());
                batch.draw(itemTexture, x + ITEM_GAP * (i + 1) + size * i, y, size, size);

                GlyphLayout layout = new GlyphLayout(bitmapFont, item.item().displayName());
                int boxX =
                        x
                                + ITEM_GAP * (i + 1)
                                + size * i
                                + (size / 2)
                                - Math.round((layout.width / 2))
                                - NUMBER_PADDING;
                int boxY = y - NUMBER_PADDING;
                batch.draw(
                        numberBackground,
                        boxX,
                        boxY,
                        layout.width + 2 * NUMBER_PADDING,
                        layout.height + 2 * NUMBER_PADDING);
                bitmapFont.draw(
                        batch,
                        item.item().displayName(),
                        boxX + NUMBER_PADDING,
                        boxY + NUMBER_PADDING + layout.height,
                        layout.width,
                        Align.center,
                        false);

                i++;
            }
        }
    }

    private void updateRecipe() {
        ItemData[] itemData = this.items.toArray(new ItemData[0]);
        this.currentRecipe = Crafting.recipeByIngredients(itemData).orElse(null);
    }

    private void craft() {
        if (this.currentRecipe == null) return;
        CraftingResult[] results = this.currentRecipe.results();
        Arrays.stream(results)
                .filter(
                        result ->
                                result.resultType() == CraftingType.ITEM
                                        && result instanceof ItemData)
                .forEach(
                        result -> {
                            ItemData item = (ItemData) result;
                            this.targetInventory.add(item);
                        });
        this.items.clear();
        this.updateRecipe();
    }

    private void cancel() {
        this.items.forEach(this.targetInventory::add);
        this.items.clear();
        this.updateRecipe();
    }
}
