package contrib.hud.crafting;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.crafting.CraftingDialogLogic;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.hud.IInventoryHolder;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.Button;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.elements.ImageButton;
import contrib.hud.inventory.ItemDragPayload;
import contrib.item.Item;
import contrib.platform.gdx.hud.GdxHudItemRenderer;
import core.Game;
import core.platform.gdx.render.GdxAnimationFrames;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
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
public class CraftingGUI extends CombinableGUI implements IInventoryHolder {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CraftingGUI.class);

  /** Callback key for the craft action. */
  public static final String CALLBACK_CRAFT = "craft";

  /** Callback key for the cancel action. */
  public static final String CALLBACK_CANCEL = "cancel";

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

  // Textures
  private static final String BACKGROUND_TEXTURE_PATH = "hud/crafting/background.png";
  private static final String BUTTON_OK_TEXTURE_PATH = "hud/check.png";
  private static final String BUTTON_CANCEL_TEXTURE_PATH = "hud/cross.png";

  private static final Animation backgroundAnimation;

  static {
    if (Game.isHeadless()) {
      backgroundAnimation = null;
    } else {
      backgroundAnimation = new Animation(new SimpleIPath(BACKGROUND_TEXTURE_PATH));
    }
  }

  private final InventoryComponent inventory;
  private final Button buttonOk, buttonCancel;
  private final InventoryComponent targetInventory;
  private Recipe currentRecipe = null;

  /**
   * Create a CraftingGUI that has the given InventoryComponent as target inventory for successfully
   * crafted items.
   *
   * @param sourceInventory The source inventory where items to be crafted are stored.
   * @param targetInventory The target inventory.
   * @param dialogId The dialog ID for network callbacks.
   */
  public CraftingGUI(
      InventoryComponent sourceInventory, InventoryComponent targetInventory, String dialogId) {
    var oldCallback = sourceInventory.onItemAdded();
    sourceInventory.onItemAdded(
        item -> {
          oldCallback.accept(item);
          this.updateRecipe();
        });
    this.inventory = sourceInventory;
    this.targetInventory = targetInventory;

    if (Game.isHeadless()) {
      this.buttonOk = new Button(this, 0, 0, 0, 0);
      this.buttonCancel = new Button(this, 0, 0, 0, 0);
      return;
    }

    this.buttonOk =
        new ImageButton(this, new Animation(new SimpleIPath(BUTTON_OK_TEXTURE_PATH)), 0, 0, 1, 1);
    this.buttonCancel =
        new ImageButton(
            this, new Animation(new SimpleIPath(BUTTON_CANCEL_TEXTURE_PATH)), 0, 0, 1, 1);
    this.buttonOk.onClick(
        (button) ->
            DialogCallbackResolver.createButtonCallback(dialogId, CALLBACK_CRAFT)
                .accept(this.inventory.items()));
    this.buttonCancel.onClick(
        (button) ->
            DialogCallbackResolver.createButtonCallback(dialogId, CALLBACK_CANCEL).accept(null));
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
              CraftingGUI.this.targetInventory.transfer(
                  itemDragPayload.item(), CraftingGUI.this.inventory);
              CraftingGUI.this.updateRecipe();
            }
          }
        });
  }

  /**
   * Registers the standard crafting callbacks on the given UIComponent.
   *
   * <p>Call this method after creating a UIComponent for CraftingGUI to enable the craft and cancel
   * functionality on the server.
   *
   * @param uiComponent the UIComponent to register callbacks on
   * @param craftingGUI the CraftingGUI instance
   */
  public static void registerCallbacks(UIComponent uiComponent, CraftingGUI craftingGUI) {
    uiComponent.registerCallback(
        CALLBACK_CRAFT,
        data -> {
          if (data instanceof Item[] items) {
            craftingGUI.inventory.setItems(items);
          } else {
            LOGGER.warn("Invalid data for crafting callback: expected Item[], got {}", data);
          }
          craftingGUI.craft();
          UIUtils.closeDialog(uiComponent);
        });
    uiComponent.registerCallback(
        CALLBACK_CANCEL,
        data -> {
          craftingGUI.cancel();
          UIUtils.closeDialog(uiComponent);
        });
    uiComponent.onClose(ui -> craftingGUI.cancel());
  }

  @Override
  protected Vector2 preferredSize(GUICombination.AvailableSpace availableSpace) {
    int size =
        Math.round(
            Math.min(availableSpace.height(), (Game.stage().orElseThrow().getHeight() / 4) * 3));
    if (size > availableSpace.width()) {
      size = availableSpace.width();
    }
    return Vector2.of(size, size);
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
    batch.draw(GdxAnimationFrames.toRegion(backgroundAnimation.update()), this.x(), this.y(), this.width(), this.height());

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
    if (this.inventory.isEmpty()) {
      return;
    }

    // Draw inserted items
    {
      int size =
        Math.min(
          Math.round(this.height() * INPUT_ITEMS_MAX_SIZE),
          (this.width() - this.inventory.count() * ITEM_GAP) / this.inventory.count());
      int rowWidth = GdxHudItemRenderer.rowWidth(this.inventory.count(), size, ITEM_GAP);
      int startX = this.x() + Math.round(this.width() * INPUT_ITEMS_X) - rowWidth / 2;
      int startY = this.y() + Math.round(this.height() * INPUT_ITEMS_Y);

      for (int i = 0; i < this.inventory.count(); i++) {
        Item item = this.inventory.get(i).orElseThrow();
        int itemX = startX + ITEM_GAP * (i + 1) + size * i;

        GdxHudItemRenderer.drawIndexedItem(batch, item, itemX, startY, size, i + 1, NUMBER_PADDING);
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
      int rowWidth = GdxHudItemRenderer.rowWidth(nrItemResults, size, ITEM_GAP);
      int startX = this.x() + Math.round(this.width() * RESULT_ITEM_X) - rowWidth / 2;
      int startY = this.y() + Math.round(this.height() * RESULT_ITEM_Y);

      int i = 0;
      for (CraftingResult result : this.currentRecipe.results()) {
        if (result.resultType() != CraftingType.ITEM || !(result instanceof Item item)) {
          continue;
        }

        int itemX = startX + ITEM_GAP * (i + 1) + size * i;
        GdxHudItemRenderer.drawNamedItem(
          batch, item, itemX, startY, size, item.displayName(), NUMBER_PADDING);
        i++;
      }
    }
  }

  private void updateRecipe() {
    this.currentRecipe = CraftingDialogLogic.currentRecipe(this.inventory).orElse(null);
  }

  private void craft() {
    CraftingDialogLogic.craft(this.inventory, this.targetInventory);
    this.updateRecipe();
  }

  /** Allows to reset the CraftingGUI moving all Items back to the Inventory they came from. */
  public void cancel() {
    CraftingDialogLogic.cancel(this.inventory, this.targetInventory);
    this.updateRecipe();
  }

  @Override
  public InventoryComponent inventoryComponent() {
    return this.inventory;
  }
}
