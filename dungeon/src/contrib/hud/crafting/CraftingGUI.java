package contrib.hud.crafting;

import com.badlogic.gdx.graphics.g2d.*;
import contrib.components.InventoryComponent;
import contrib.crafting.*;
import contrib.hud.IInventoryHolder;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.elements.GuiInteractionContext;
import contrib.platform.gdx.hud.GdxCraftingDialogRenderer;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.Vector2;
import core.utils.logging.DungeonLogger;
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
  private static final CraftingDialogLayout LAYOUT = new CraftingDialogLayout();
  private static final GdxCraftingDialogRenderer DIALOG_RENDERER =
    new GdxCraftingDialogRenderer();

  private final CraftingDialogController controller;
  private final CraftingDialogInteraction interaction;
  private final CraftingActionBar actionBar;
  private final CraftingDialogBodyRenderer bodyRenderer;

  private int pressedCraftingSlot = -1;
  private boolean leftButtonDownLastFrame = false;

  public CraftingGUI(
    CraftingDialogController controller,
    String dialogId,
    CraftingActionBarFactory actionBarFactory,
    CraftingDialogBodyRendererFactory bodyRendererFactory) {
    this.controller = Objects.requireNonNull(controller, "controller must not be null");
    this.interaction = new CraftingDialogInteraction(this.controller);
    this.actionBar =
      Objects.requireNonNull(actionBarFactory, "actionBarFactory must not be null")
        .create(this, dialogId, this.controller::craftingPayload);
    this.bodyRenderer =
      Objects.requireNonNull(bodyRendererFactory, "bodyRendererFactory must not be null").create();
  }

  @Override
  protected void initInteraction(GuiInteractionContext interactionContext) {
    // Crafting slot interaction is now handled via backend-neutral click polling.
    // This aligns the libGDX dialog semantics with the existing LITIENGINE crafting overlay.
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
    this.bodyRenderer.draw(batch, this.controller, this.x(), this.y(), this.width(), this.height());
    this.actionBar.draw(batch);
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
    for (CraftingDialogLayout.SlotBounds slot :
      LAYOUT.visibleCraftingSlots(
        this.controller.craftingSlots(), this.x(), this.y(), this.width(), this.height())) {
      if (slot.contains(mouseX, mouseY)) {
        return slot.slotIndex();
      }
    }

    return -1;
  }

  @Override
  public InventoryComponent inventoryComponent() {
    return this.controller.craftingInventory();
  }
}
