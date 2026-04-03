package contrib.hud.crafting;

import com.badlogic.gdx.graphics.g2d.*;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.crafting.*;
import contrib.hud.IInventoryHolder;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.elements.GuiInteractionContext;
import contrib.item.Item;
import contrib.platform.gdx.hud.GdxHudItemRenderer;
import core.Game;
import core.input.MouseButtons;
import core.platform.gdx.render.GdxAnimationFrames;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

  // Position settings
  private static final int NUMBER_PADDING = 5;
  private static final int ITEM_GAP = 10;

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

  // Textures
  private static final String BACKGROUND_TEXTURE_PATH = "hud/crafting/background.png";

  private static final Animation backgroundAnimation;

  static {
    if (Game.isHeadless()) {
      backgroundAnimation = null;
    } else {
      backgroundAnimation = new Animation(new SimpleIPath(BACKGROUND_TEXTURE_PATH));
    }
  }

  private final CraftingDialogController controller;
  private final CraftingDialogInteraction interaction;
  private final CraftingActionBar actionBar;

  private int pressedCraftingSlot = -1;
  private boolean leftButtonDownLastFrame = false;

  /**
   * Create a CraftingGUI that has the given InventoryComponent as target inventory for successfully
   * crafted items.
   *
   * @param sourceInventory The source inventory where items to be crafted are stored.
   * @param targetInventory The target inventory.
   * @param dialogId The dialog ID for network callbacks.
   */
  public CraftingGUI(
    InventoryComponent sourceInventory,
    InventoryComponent targetInventory,
    String dialogId,
    CraftingActionBarFactory actionBarFactory) {
    this.controller = new CraftingDialogController(targetInventory, sourceInventory);
    this.interaction = new CraftingDialogInteraction(this.controller);
    this.actionBar =
      Objects.requireNonNull(actionBarFactory, "actionBarFactory must not be null")
        .create(this, dialogId, this.controller::craftingPayload);
  }

  @Override
  protected void initInteraction(GuiInteractionContext interactionContext) {
    // Crafting slot interaction is now handled via backend-neutral click polling.
    // This aligns the libGDX dialog semantics with the existing LITIENGINE crafting overlay.
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
    craftingGUI.controller.registerCallbacks(uiComponent);
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
    this.actionBar.updateBounds(this.x(), this.y(), this.width(), this.height());
  }

  @Override
  protected void draw(Batch batch) {
    this.handleCraftingInput();

    assert backgroundAnimation != null;
    batch.draw(
      GdxAnimationFrames.toRegion(backgroundAnimation.update()),
      this.x(),
      this.y(),
      this.width(),
      this.height());

    this.drawItems(batch);
    this.actionBar.draw(batch);
  }

  /**
   * Draws the items that have been added to the cauldron.
   *
   * @param batch The batch to draw to.
   */
  private void drawItems(Batch batch) {
    Item[] craftingSlots = this.controller.craftingSlots();
    List<Integer> visibleCraftingSlots = this.visibleCraftingSlotIndices(craftingSlots);

    if (visibleCraftingSlots.isEmpty()) {
      return;
    }

    // Draw inserted items
    {
      int size = this.craftingItemSize(visibleCraftingSlots.size());
      int rowWidth = GdxHudItemRenderer.rowWidth(visibleCraftingSlots.size(), size, ITEM_GAP);
      int startX = this.x() + Math.round(this.width() * INPUT_ITEMS_X) - rowWidth / 2;
      int startY = this.y() + Math.round(this.height() * INPUT_ITEMS_Y);

      for (int i = 0; i < visibleCraftingSlots.size(); i++) {
        int itemX = startX + ITEM_GAP * (i + 1) + size * i;
        Item item = craftingSlots[visibleCraftingSlots.get(i)];

        GdxHudItemRenderer.drawIndexedItem(
          batch, item, itemX, startY, size, i + 1, NUMBER_PADDING);
      }
    }

    // Draw result if present
    {
      Item[] resultItems = this.controller.resultItems();
      if (resultItems.length == 0) {
        return;
      }

      int size =
        Math.min(
          Math.round(this.height() * RESULT_ITEM_MAX_SIZE),
          (this.width() - resultItems.length * ITEM_GAP) / resultItems.length);

      int rowWidth = GdxHudItemRenderer.rowWidth(resultItems.length, size, ITEM_GAP);
      int startX = this.x() + Math.round(this.width() * RESULT_ITEM_X) - rowWidth / 2;
      int startY = this.y() + Math.round(this.height() * RESULT_ITEM_Y);

      for (int i = 0; i < resultItems.length; i++) {
        int itemX = startX + ITEM_GAP * (i + 1) + size * i;

        GdxHudItemRenderer.drawNamedItem(
          batch, resultItems[i], itemX, startY, size, resultItems[i].displayName(), NUMBER_PADDING);
      }
    }
  }

  private void handleCraftingInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      this.pressedCraftingSlot = -1;
      this.leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = Math.round(stage.getHeight()) - stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !this.leftButtonDownLastFrame) {
      this.pressedCraftingSlot = this.findCraftingSlotAt(mouseX, mouseY);
    }

    if (!leftButtonDown && this.leftButtonDownLastFrame) {
      int releasedCraftingSlot = this.findCraftingSlotAt(mouseX, mouseY);
      int previouslyPressedSlot = this.pressedCraftingSlot;
      this.pressedCraftingSlot = -1;

      if (previouslyPressedSlot >= 0 && previouslyPressedSlot == releasedCraftingSlot) {
        this.interaction.transferClickedSlot(
          CraftingDialogController.InventorySide.CRAFTING, releasedCraftingSlot);
      }
    }

    this.leftButtonDownLastFrame = leftButtonDown;
  }

  private int findCraftingSlotAt(int mouseX, int mouseY) {
    Item[] craftingSlots = this.controller.craftingSlots();
    List<Integer> visibleCraftingSlots = this.visibleCraftingSlotIndices(craftingSlots);
    if (visibleCraftingSlots.isEmpty()) {
      return -1;
    }

    int size = this.craftingItemSize(visibleCraftingSlots.size());
    int rowWidth = GdxHudItemRenderer.rowWidth(visibleCraftingSlots.size(), size, ITEM_GAP);
    int startX = this.x() + Math.round(this.width() * INPUT_ITEMS_X) - rowWidth / 2;
    int startY = this.y() + Math.round(this.height() * INPUT_ITEMS_Y);

    for (int i = 0; i < visibleCraftingSlots.size(); i++) {
      int itemX = startX + ITEM_GAP * (i + 1) + size * i;

      if (mouseX >= itemX
        && mouseX <= itemX + size
        && mouseY >= startY
        && mouseY <= startY + size) {
        return visibleCraftingSlots.get(i);
      }
    }

    return -1;
  }

  private int craftingItemSize(int visibleItemCount) {
    return Math.min(
      Math.round(this.height() * INPUT_ITEMS_MAX_SIZE),
      (this.width() - visibleItemCount * ITEM_GAP) / visibleItemCount);
  }

  private List<Integer> visibleCraftingSlotIndices(Item[] craftingSlots) {
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < craftingSlots.length; i++) {
      if (craftingSlots[i] != null) {
        indices.add(i);
      }
    }
    return indices;
  }

  @Override
  public InventoryComponent inventoryComponent() {
    return this.controller.craftingInventory();
  }
}
