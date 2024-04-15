package contrib.hud.crafting;

import com.badlogic.gdx.Gdx;
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
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.elements.ImageButton;
import contrib.hud.inventory.ItemDragPayload;
import contrib.item.Item;
import core.Game;
import core.utils.components.draw.Animation;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents the GUI for the crafting system. If this gui is open, the player can craft
 * items in it.
 *
 * <p>The GUI is a {@link CombinableGUI} and can be combined with other GUIs. It is recommended to
 * combine it with a {@link contrib.hud.inventory.InventoryGUI InventoryGUI} that shows the target
 * inventory.
 *
 * <p>The GUI has a target inventory. If the player successfully crafts an item, the item will be
 * added to the target inventory.
 *
 * <p>The GUI shows a list of items that have been added to the cauldron. The player can add items
 * to the cauldron by dragging them into the GUI. The GUI will then try to find a recipe that
 * matches the items in the cauldron. If a recipe is found, the GUI will show the result of the
 * recipe. The player can then click the OK button to craft the item or the cancel button to cancel
 * the crafting process and get the items back (if there is enough space in the target inventory).
 *
 * <p>The GUI is configured by the many constants at the top of this file. These constants are used
 * to position the items and buttons in the GUI. The GUI is always square and the size is based on a
 * percentage of the height of the crafting GUI.
 */
public class CraftingGUI extends CombinableGUI {

  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");

  // Position settings
  private static final int NUMBER_PADDING = 5;
  private static final int ITEM_GAP = 10;

  // Positioning and sizing
  // These values should fit the background texture of the crafting GUI and should be between 0
  // and 1.
  // 0 is the bottom-left corner and 1 is the top right corner.

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

  // Textures
  private static final String BACKGROUND_TEXTURE_PATH = "hud/crafting/background.png";
  private static final String BUTTON_OK_TEXTURE_PATH = "hud/check.png";
  private static final String BUTTON_CANCEL_TEXTURE_PATH = "hud/cross.png";

  private static final Texture texture;
  private static final TextureRegion numberBackground;
  private static final Animation backgroundAnimation;
  private static final BitmapFont bitmapFont;

  static {
    backgroundAnimation = Animation.fromSingleImage(new SimpleIPath(BACKGROUND_TEXTURE_PATH));

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.drawPixel(0, 0, NUMBER_BACKGROUND_COLOR);

    texture = new Texture(pixmap);
    numberBackground = new TextureRegion(texture, 0, 0, 1, 1);

    // Init Font
    bitmapFont =
        new BitmapFont(
            Gdx.files.internal(FONT_FNT.pathString()),
            Gdx.files.internal(FONT_PNG.pathString()),
            false);
  }

  private final ArrayList<Item> items = new ArrayList<>();
  private final ImageButton buttonOk, buttonCancel;
  private final InventoryComponent targetInventory;
  private Recipe currentRecipe = null;

  /**
   * Create a CraftingGUI that has the given InventoryComponent as target inventory for successfully
   * crafted items.
   *
   * @param targetInventory The target inventory.
   */
  public CraftingGUI(InventoryComponent targetInventory) {
    this.targetInventory = targetInventory;
    this.buttonOk =
        new ImageButton(
            this, Animation.fromSingleImage(new SimpleIPath(BUTTON_OK_TEXTURE_PATH)), 0, 0, 1, 1);
    this.buttonCancel =
        new ImageButton(
            this,
            Animation.fromSingleImage(new SimpleIPath(BUTTON_CANCEL_TEXTURE_PATH)),
            0,
            0,
            1,
            1);
    this.buttonOk.onClick((button) -> this.craft());
    this.buttonCancel.onClick((button) -> this.cancel());
  }

  // Init CraftingGUI as drag and drop target so that items can be dragged into the cauldron/ui
  // but not as source
  // so that items can't be dragged out of the cauldron/ui.
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
            if (payload != null && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
              return itemDragPayload.item() != null;
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
            if (payload != null && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
              CraftingGUI.this.items.add(itemDragPayload.item());
              CraftingGUI.this.updateRecipe();
            }
          }
        });
  }

  @Override
  protected Vector2 preferredSize(GUICombination.AvailableSpace availableSpace) {
    int size =
        Math.round(
            Math.min(availableSpace.height(), (Game.stage().orElseThrow().getHeight() / 4) * 3));
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
                .textureAt(this.items.get(i).inventoryAnimation().nextAnimationTexturePath());
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
                      result -> result.resultType() == CraftingType.ITEM && result instanceof Item)
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
        if (result.resultType() != CraftingType.ITEM || !(result instanceof Item item)) {
          continue;
        }
        Texture itemTexture =
            TextureMap.instance().textureAt(item.inventoryAnimation().nextAnimationTexturePath());
        batch.draw(itemTexture, x + ITEM_GAP * (i + 1) + size * i, y, size, size);

        GlyphLayout layout = new GlyphLayout(bitmapFont, item.displayName());
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
            item.displayName(),
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
    Item[] itemData = this.items.toArray(new Item[0]);
    this.currentRecipe = Crafting.recipeByIngredients(itemData).orElse(null);
  }

  private void craft() {
    if (this.currentRecipe == null) return;
    CraftingResult[] results = this.currentRecipe.results();
    Arrays.stream(results)
        .filter(result -> result.resultType() == CraftingType.ITEM && result instanceof Item)
        .forEach(
            result -> {
              Item item = (Item) result;
              this.targetInventory.add(item);
            });
    this.items.clear();
    this.updateRecipe();
  }

  /** Allows to reset the CraftingGUI moving all Items back to the Inventory they came from. */
  public void cancel() {
    this.items.forEach(this.targetInventory::add);
    this.items.clear();
    this.updateRecipe();
  }

  /**
   * Add an item to the cauldron.
   *
   * @param item The item to add.
   */
  public void addItem(Item item) {
    this.items.add(item);
    this.updateRecipe();
  }
}
