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

import contrib.crafting.Crafting;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.hud.CombinableGUI;
import contrib.hud.GUICombination;
import contrib.hud.inventory.ItemDragPayload;
import contrib.utils.components.item.ItemData;

import core.Game;
import core.utils.components.draw.Animation;
import core.utils.components.draw.TextureMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/** */
public class CraftingGUI extends CombinableGUI {

    // Position settings
    private static final int PADDING = 10;
    private static final int NUMBER_PADDING = 5;
    private static final int ITEM_GAP = 10;

    // Item positioning and sizing
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

    // Colors
    private static final int HOVER_BACKGROUND_COLOR = 0xffffffff;
    private static final int NUMBER_BACKGROUND_COLOR = 0xd93030ff;

    private static final Texture texture;
    private static final TextureRegion hoverBackground, numberBackground;
    private static final Animation backgroundAnimation;
    private static final BitmapFont bitmapFont;

    static {
        backgroundAnimation = Animation.of(new File("./game/assets/hud/crafting/idle"));

        Pixmap pixmap = new Pixmap(2, 1, Pixmap.Format.RGBA8888);
        pixmap.drawPixel(0, 0, HOVER_BACKGROUND_COLOR);
        pixmap.drawPixel(1, 0, NUMBER_BACKGROUND_COLOR);

        texture = new Texture(pixmap);
        hoverBackground = new TextureRegion(texture, 0, 0, 1, 1);
        numberBackground = new TextureRegion(texture, 1, 0, 1, 1);

        // Init Font
        bitmapFont =
                new BitmapFont(
                        new FileHandle("./game/assets/skin/myFont.fnt"),
                        new FileHandle("./game/assets/skin/myFont.png"),
                        false);
    }

    private ArrayList<ItemData> items = new ArrayList<>();
    private Optional<Recipe> currentRecipe = Optional.empty();

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
        batch.draw(
                TextureMap.instance().textureAt(backgroundAnimation.nextAnimationTexturePath()),
                this.x(),
                this.y(),
                this.width(),
                this.height());

        this.drawItems(batch);
        this.drawButtons(batch);
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
            if (this.currentRecipe.isEmpty()) {
                return;
            }
            Recipe recipe = this.currentRecipe.get();

            int nrItemResults =
                    (int)
                            Arrays.stream(recipe.results())
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
            for (CraftingResult result : recipe.results()) {
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

    private void drawButtons(Batch batch) {
        // TODO: Implement
    }

    private void updateRecipe() {
        ItemData[] itemData = this.items.toArray(new ItemData[0]);
        this.currentRecipe = Crafting.recipeByIngredients(itemData);
    }
}
