package contrib.hud.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.entities.HeroController;
import contrib.hud.IInventoryHolder;
import contrib.hud.UIUtils;
import contrib.hud.elements.*;
import contrib.platform.gdx.hud.*;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.network.messages.c2s.InputMessage;
import core.ui.StageHandle;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** WTF? . */
public class InventoryGUI extends CombinableGUI implements IInventoryHolder {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(InventoryGUI.class);
  private static final Map<Integer, Boolean> inventoryOpenMap = new HashMap<>();

  private static final int DEFAULT_MAX_ITEMS_PER_ROW = 8;
  private static final int BORDER_COLOR = 0x9dc1ebff;

  private DragAndDrop.Source dropAndDropSource;
  private DragAndDrop.Target dropAndDropTarget;
  private boolean inputListenerInstalled = false;

  private final InventoryComponent inventoryComponent;
  private Texture textureSlots;
  private String title;
  private int slotSize = 0;
  private int slotsPerRow = 0;

  /**
   * Create a new inventory GUI.
   *
   * @param title the title of the inventory
   * @param inventoryComponent the inventory component on which the GUI is based.
   * @param maxItemsPerRow the maximum number of items per row in the inventory
   */
  public InventoryGUI(String title, InventoryComponent inventoryComponent, int maxItemsPerRow) {
    super();
    this.inventoryComponent = inventoryComponent;
    this.title = title;
    this.slotsPerRow =
      Math.max(Math.min(maxItemsPerRow, this.inventoryComponent.items().length), 1);

    this.dropAndDropSource = null;
    this.dropAndDropTarget = null;
  }

  /**
   * Create a new inventory GUI. The max number of items per row is set to the default value.
   *
   * @param title the title of the inventory
   * @param inventoryComponent the inventory component on which the GUI is based.
   */
  public InventoryGUI(String title, InventoryComponent inventoryComponent) {
    this(title, inventoryComponent, DEFAULT_MAX_ITEMS_PER_ROW);
  }

  /**
   * Create a new inventory GUI.
   *
   * @param inventoryComponent the inventory component on which the GUI is based.
   */
  public InventoryGUI(InventoryComponent inventoryComponent) {
    this(
      Game.findInAll(inventoryComponent)
        .map(InventoryGUI::generateTitleFromEntity)
        .orElse("INVENTORY"),
      inventoryComponent);
  }

  private static String generateTitleFromEntity(Entity entity) {
    if (entity.isPresent(PlayerComponent.class)) {
      return entity.fetch(PlayerComponent.class).orElseThrow().playerName();
    }

    return entity.name().split("_(?=\\d+)")[0].toLowerCase();
  }

  /**
   * Checks if the given player's inventory is currently open.
   *
   * @param player the player entity
   * @return true if the inventory is open, false otherwise
   */
  public static boolean inPlayerInventory(Entity player) {
    return inventoryOpenMap.getOrDefault(player.id(), false);
  }

  @Override
  protected void initInteraction(GuiInteractionContext interactionContext) {
    if (Game.isHeadless()) {
      return;
    }

    Optional<Actor> actor = gdxActor();
    actor.ifPresent(this::ensureInputListenerInstalled);

    Optional<DragAndDrop> dragAndDrop = gdxDragAndDrop();
    if (actor.isEmpty() || dragAndDrop.isEmpty()) {
      this.dropAndDropSource = null;
      this.dropAndDropTarget = null;
      return;
    }

    this.dropAndDropSource =
      GdxInventoryDragAndDropAdapters.itemSource(
        actor.get(),
        this.inventoryComponent,
        this::getSlotByCoordinates,
        InventoryGUI::isPlayersInventory,
        () -> this.slotSize,
        this::gdxDragAndDrop,
        this::handleDraggedItemDroppedOutside);

    this.dropAndDropTarget =
      GdxInventoryDragAndDropAdapters.itemTarget(
        actor.get(),
        this::getSlotByCoordinates,
        itemDragPayload -> true,
        this::handleDraggedItemDroppedOnSlot);

    dragAndDrop.get().addSource(this.dropAndDropSource);
    dragAndDrop.get().addTarget(this.dropAndDropTarget);
  }

  @Override
  protected void renderContent(final GuiRenderContext renderContext) {
    renderContext
      .unwrap(GdxGuiRenderContext.class)
      .ifPresent(gdxRenderContext -> GdxInventoryGuiRenderer.render(this, gdxRenderContext));
  }

  @Override
  protected void renderTopLayerContent(final GuiRenderContext renderContext) {
    renderContext
      .unwrap(GdxGuiRenderContext.class)
      .ifPresent(gdxRenderContext -> GdxInventoryGuiRenderer.renderTopLayer(this, gdxRenderContext));
  }

  /**
   * Ensures that the cached libGDX slot texture matches the current inventory widget bounds.
   *
   * <p>This remains in InventoryGUI for now because the texture cache belongs to the widget state,
   * while the concrete drawing is handled by the backend renderer.
   */
  public final void ensureGdxSlotTexture() {
    this.drawSlots();
  }

  /**
   * Returns the cached libGDX slot texture.
   *
   * @return cached slot texture, may be null
   */
  public final Texture gdxSlotTexture() {
    return this.textureSlots;
  }

  /**
   * Returns the calculated slot size used by the current inventory layout.
   *
   * @return slot size in pixels
   */
  public final int gdxSlotSize() {
    return this.slotSize;
  }

  /**
   * Returns the number of slots per row used by the current inventory layout.
   *
   * @return slot count per row
   */
  public final int gdxSlotsPerRow() {
    return this.slotsPerRow;
  }

  /**
   * Resolves a slot index for the given widget-local coordinates.
   *
   * @param x local x coordinate
   * @param y local y coordinate
   * @return slot index or -1 if invalid
   */
  public final int gdxSlotByCoordinates(int x, int y) {
    return this.getSlotByCoordinates(x, y);
  }

  /**
   * Returns whether a libGDX drag operation is currently active for this inventory context.
   *
   * @return true if an item is currently being dragged
   */
  public final boolean gdxIsDragging() {
    return this.isDragging();
  }

  /**
   * Returns the current libGDX drag payload, if any.
   *
   * @return active drag payload or null
   */
  public final DragAndDrop.Payload gdxCurrentDragPayload() {
    return this.currentDragPayload();
  }

  private int getSlotByMousePosition() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return -1;
    }

    Point mousePos = new Point(stage.mouseX(), Math.round(stage.getHeight()) - stage.mouseY());
    Point relMousePos = new Point(mousePos.x() - this.x(), mousePos.y() - this.y());
    return getSlotByCoordinates(relMousePos.x(), relMousePos.y());
  }

  private int getSlotByCoordinates(int x, int y) {
    if (this.slotSize == 0) {
      return -1;
    }
    return (x / this.slotSize) + (y / this.slotSize) * this.slotsPerRow;
  }

  private int getSlotByCoordinates(float x, float y) {
    return this.getSlotByCoordinates((int) x, (int) y);
  }

  private void drawSlots() {
    if (Game.isHeadless()) {
      return;
    }

    if (this.textureSlots == null
      || this.textureSlots.getWidth() != this.width()
      || this.textureSlots.getHeight() != this.height()) {
      if (this.textureSlots != null) {
        this.textureSlots.dispose();
      }

      if (this.width() <= 0 || this.height() <= 0) {
        return;
      }

      Pixmap pixmap = new Pixmap(this.width(), this.height(), Pixmap.Format.RGBA8888);
      try {
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        pixmap.setColor(new Color(BORDER_COLOR));
        int rows =
          (int)
            Math.max(
              Math.ceil(this.inventoryComponent.items().length / (float) this.slotsPerRow),
              1.0f);

        for (int row = 0; row < rows; row++) {
          for (int col = 0; col < this.slotsPerRow; col++) {
            int slotIndex = col + row * this.slotsPerRow;
            if (slotIndex >= this.inventoryComponent.items().length) {
              break;
            }

            int slotX = col * this.slotSize;
            int slotY = row * this.slotSize;
            pixmap.drawRectangle(slotX, slotY, this.slotSize, this.slotSize);
          }
        }

        this.textureSlots = new Texture(pixmap);
      } finally {
        pixmap.dispose();
      }
    }
  }

  private static boolean isPlayersInventory(Entity player, InventoryComponent inventoryComponent) {
    Optional<Entity> owner = Game.findInAll(inventoryComponent);
    return owner.isPresent() && owner.get().id() == player.id();
  }

  private void ensureInputListenerInstalled(Actor actor) {
    if (this.inputListenerInstalled) {
      return;
    }

    Game.stage().ifPresent(stage -> stage.setKeyboardFocus(actor));

    actor.addListener(
      new InputListener() {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
          Entity player = Game.player().orElse(null);
          if (player == null) {
            return false;
          }

          if (KeyboardConfig.USE_ITEM.value() == keycode && allowItemUse(player)) {
            return useHoveredSlot(player, getSlotByMousePosition());
          }

          return false;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
          Entity player = Game.player().orElse(null);
          if (player == null) {
            return false;
          }

          if (KeyboardConfig.MOUSE_USE_ITEM.value() == button && allowItemUse(player)) {
            return useHoveredSlot(player, getSlotByMousePosition());
          }

          if (KeyboardConfig.TRANSFER_ITEM.value() == button && allowQuickTransfer(player)) {
            transferHoveredSlot(player, getSlotByMousePosition());
            return true;
          }

          return false;
        }
      });

    this.inputListenerInstalled = true;
  }

  private boolean useHoveredSlot(Entity player, int slot) {
    if (slot < 0) {
      return false;
    }

    if (Game.network().isServer()) {
      return HeroController.useItem(player, slot);
    } else {
      Game.network()
        .send(
          (short) 0,
          new InputMessage(InputMessage.Action.INV_USE, Vector2.of(slot, 0)),
          true);
      return true;
    }
  }

  private void transferHoveredSlot(Entity player, int slot) {
    if (slot < 0) {
      return;
    }

    UIComponent uiComponent = player.fetch(UIComponent.class).orElse(null);
    if (uiComponent == null
      || uiComponent.dialog().flatMap(handle -> handle.unwrap(GUICombination.class)).isEmpty()) {
      return;
    }

    int sourceSlot = slot;
    if (isPlayersInventory(player, this.inventoryComponent)) {
      sourceSlot = (-sourceSlot) - 1;
    }

    Optional<InventoryComponent> targetInventory =
      UIUtils.getInventoriesFromUI(uiComponent)
        .filter(invComp -> invComp != this.inventoryComponent)
        .findFirst();

    if (targetInventory.isEmpty()) {
      return;
    }

    int nextBestTargetSlot = targetInventory.get().findNextAvailableSlot();
    if (nextBestTargetSlot == -1) {
      LOGGER.debug("No available slot in target inventory for transfer.");
      return;
    }

    if (isPlayersInventory(player, targetInventory.get())) {
      nextBestTargetSlot = (-nextBestTargetSlot) - 1;
    }

    if (Game.network().isServer()) {
      HeroController.moveItem(player, sourceSlot, nextBestTargetSlot);
    } else {
      Game.network()
        .send(
          (short) 0,
          new InputMessage(
            InputMessage.Action.INV_MOVE, Vector2.of(sourceSlot, nextBestTargetSlot)),
          true);
    }
  }

  @Override
  protected Vector2 preferredSize(GUICombination.AvailableSpace availableSpace) {
    int rows =
      (int)
        Math.max(
          Math.ceil(this.inventoryComponent.items().length / (float) this.slotsPerRow), 1.0f);
    int width =
      (int) Math.min(availableSpace.width(), (Game.stage().orElseThrow().getWidth() * 0.75f));
    int height = (width / this.slotsPerRow) * rows;

    if (height > availableSpace.height()) {
      height = availableSpace.height();
      width = (height / rows) * this.slotsPerRow;
    }

    this.slotSize = width / this.slotsPerRow;
    return Vector2.of(width, height);
  }

  /**
   * Get the displayed title of this InventoryGUI.
   *
   * @return title of the inventory
   */
  public String title() {
    return this.title;
  }

  /**
   * Set the displayed title of this InventoryGUI.
   *
   * @param title title of the inventory
   */
  public void title(String title) {
    this.title = title;
  }

  /**
   * Get the InventoryComponent associated with this InventoryGUI.
   *
   * @return the InventoryComponent
   */
  public InventoryComponent inventoryComponent() {
    return this.inventoryComponent;
  }

  private Optional<DragAndDrop> gdxDragAndDrop() {
    return interactionContext(GdxGuiInteractionContext.class)
      .flatMap(GdxGuiInteractionContext::dragAndDrop);
  }

  private Optional<Actor> gdxActor() {
    return interactionContext(GdxGuiInteractionContext.class)
      .flatMap(GdxGuiInteractionContext::actor);
  }

  private boolean isDragging() {
    return gdxDragAndDrop().map(DragAndDrop::isDragging).orElse(false);
  }

  private DragAndDrop.Payload currentDragPayload() {
    return gdxDragAndDrop().map(DragAndDrop::getDragPayload).orElse(null);
  }

  private void handleDraggedItemDroppedOutside(ItemDragPayload itemDragPayload) {
    if (!allowDropOutside(itemDragPayload)) {
      return;
    }

    Entity player = Game.player().orElse(null);
    if (player == null) {
      return;
    }

    if (Game.network().isServer()) {
      HeroController.dropItem(player, itemDragPayload.inventoryComponent(), itemDragPayload.slot());
    } else {
      Game.network()
        .send(
          (short) 0,
          new InputMessage(
            InputMessage.Action.INV_DROP, Vector2.of(itemDragPayload.slot(), 0)),
          true);
    }
  }

  private void handleDraggedItemDroppedOnSlot(ItemDragPayload itemDragPayload, int slot) {
    Entity player = Game.player().orElse(null);
    if (player == null) {
      return;
    }

    int sourceSlot = itemDragPayload.slot();
    if (draggedFromPlayerInventory(itemDragPayload)) {
      sourceSlot = (-sourceSlot) - 1;
    }

    int targetSlot = slot;
    if (belongsToPlayer(player)) {
      targetSlot = (-slot) - 1;
    }

    if (Game.network().isServer()) {
      HeroController.moveItem(player, sourceSlot, targetSlot);
    } else {
      Game.network()
        .send(
          (short) 0,
          new InputMessage(
            InputMessage.Action.INV_MOVE, Vector2.of(sourceSlot, targetSlot)),
          true);
    }
  }

  private boolean belongsToPlayer(Entity player) {
    return player != null && isPlayersInventory(player, this.inventoryComponent);
  }

  private boolean draggedFromPlayerInventory(ItemDragPayload payload) {
    return payload != null && payload.wasHeroInv();
  }

  private boolean allowItemUse(Entity player) {
    return belongsToPlayer(player);
  }

  private boolean allowQuickTransfer(Entity player) {
    return player != null && !belongsToPlayer(player);
  }

  private boolean allowDropOutside(ItemDragPayload payload) {
    return draggedFromPlayerInventory(payload);
  }
}
