package contrib.hud.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.entities.HeroController;
import contrib.hud.IInventoryHolder;
import contrib.hud.UIUtils;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.elements.GuiInteractionContext;
import contrib.item.Item;
import contrib.platform.gdx.hud.GdxGuiInteractionContext;
import contrib.platform.gdx.hud.GdxHudItemRenderer;
import contrib.platform.gdx.hud.GdxInventoryDragAndDropAdapters;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.network.messages.c2s.InputMessage;
import core.ui.StageHandle;
import core.ui.gdx.GdxUiAssetLoader;
import core.utils.*;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** WTF? . */
public class InventoryGUI extends CombinableGUI implements IInventoryHolder {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(InventoryGUI.class);
  private static final Map<Integer, Boolean> inventoryOpenMap = new HashMap<>();

  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");
  private static final int DEFAULT_MAX_ITEMS_PER_ROW = 8;
  private static final int BORDER_COLOR = 0x9dc1ebff;
  private static final int BACKGROUND_COLOR = 0x3e3e63e1;
  private static final int HOVER_BACKGROUND_COLOR = 0xffffffff;
  private static final int BORDER_PADDING = 5;
  private static final int LINE_GAP = 5;
  private static final Vector2 HOVER_OFFSET = Vector2.of(10, 10);
  private static final BitmapFont bitmapFont;
  private static final Texture backgroundTexture;
  private static final TextureRegion background, hoverBackground;

  static {
    if (Game.isHeadless()) {
      bitmapFont = null;
      backgroundTexture = null;
      background = null;
      hoverBackground = null;
    } else {
      backgroundTexture =
        GdxUiAssetLoader.createHorizontalStripTexture(BACKGROUND_COLOR, HOVER_BACKGROUND_COLOR);
      background = new TextureRegion(backgroundTexture, 0, 0, 1, 1);
      hoverBackground = new TextureRegion(backgroundTexture, 1, 0, 1, 1);
      bitmapFont = GdxUiAssetLoader.loadBitmapFont(FONT_FNT, FONT_PNG);
    }
  }

  private final DragAndDrop.Source dropAndDropSource;
  private final DragAndDrop.Target dropAndDropTarget;
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

    if (Game.isHeadless()) {
      this.dropAndDropSource = null;
      this.dropAndDropTarget = null;
      return;
    }
    this.dropAndDropSource =
      GdxInventoryDragAndDropAdapters.itemSource(
        this.actor(),
        this.inventoryComponent,
        this::getSlotByCoordinates,
        InventoryGUI::isPlayersInventory,
        () -> this.slotSize,
        this::gdxDragAndDrop,
        this::handleDraggedItemDroppedOutside);

    this.dropAndDropTarget =
      GdxInventoryDragAndDropAdapters.itemTarget(
        this.actor(),
        this::getSlotByCoordinates,
        itemDragPayload -> true,
        this::handleDraggedItemDroppedOnSlot);
  }

  /**
   * Create a new inventory GUI. The max number of items per row is set to the default value. (see
   * {@link InventoryGUI#DEFAULT_MAX_ITEMS_PER_ROW})
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

  /**
   * Retrieves the InventoryGUI associated with the given player's inventory, if it exists.
   *
   * @param player the player entity
   * @return an Optional containing the InventoryGUI if found, or empty if not found
   */
  public static Optional<InventoryGUI> getPlayerInventoryGUI(Entity player) {
    LOGGER.debug("Fetching InventoryGUI for player " + player.id() + ".");
    return player
        .fetch(UIComponent.class)
        .flatMap(
            uiComp ->
                UIUtils.getInventoriesFromUI(uiComp)
                    .filter(invComp -> isPlayersInventory(player, invComp))
                    .map(InventoryGUI::new)
                    .findFirst());
  }

  /**
   * Sets whether the inventory is open for the given player.
   *
   * @param player the player entity
   * @param open true if the inventory is open, false otherwise
   */
  public static void setInventoryOpen(Entity player, boolean open) {
    LOGGER.debug("Setting inventory open state for player " + player.id() + " to " + open + ".");
    inventoryOpenMap.put(player.id(), open);
  }

  @Override
  public void draw(Batch batch) {
    // Draw & cache slot squares
    this.drawSlots();

    // Draw Background & Slots
    batch.draw(background, this.x(), this.y(), this.width(), this.height());
    batch.draw(this.textureSlots, this.x(), this.y(), this.width(), this.height());

    // Draw Items
    this.drawItems(batch);

    // Draw inventory title
    if (!this.title.isEmpty()) this.drawInventoryTitle(batch);
  }

  @Override
  protected void drawTopLayer(Batch batch) {
    this.handleInput();
    this.drawItemInfo(batch);
  }

  private int getSlotByMousePosition() {
    StageHandle stage = Game.stage().orElseThrow();

    Vector2 mousePos =
      Vector2.of(stage.mouseX(), Math.round(stage.getHeight()) - stage.mouseY());

    Vector2 relMousePos = Vector2.of(mousePos.x() - this.x(), mousePos.y() - this.y());
    return getSlotByCoordinates(relMousePos.x(), relMousePos.y());
  }

  private int getSlotByCoordinates(int x, int y) {
    if (this.slotSize == 0) return -1; // Prevent division by zero
    return (x / this.slotSize) + (y / this.slotSize) * this.slotsPerRow;
  }

  private int getSlotByCoordinates(float x, float y) {
    return this.getSlotByCoordinates((int) x, (int) y);
  }

  private void drawItems(Batch batch) {
    Item[] items = this.inventoryComponent.items();

    for (int i = 0; i < items.length; i++) {
      Item item = items[i];
      if (item == null) {
        continue;
      }

      int itemX = this.x() + this.slotSize * (i % this.slotsPerRow) + (2 * BORDER_PADDING);
      int itemY =
        this.y() + this.slotSize * (i / this.slotsPerRow) + (2 * BORDER_PADDING);

      // Don't draw item being dragged
      if (this.isDragging()) {
        DragAndDrop.Payload payload = this.currentDragPayload();
        if (payload != null
          && payload.getObject() instanceof ItemDragPayload itemDragPayload
          && itemDragPayload.inventoryComponent() == this.inventoryComponent
          && itemDragPayload.slot() == i) {
          continue;
        }
      }

      GdxHudItemRenderer.drawItem(
        batch,
        item,
        itemX,
        itemY,
        this.slotSize - (4 * BORDER_PADDING));
    }
  }

  private void drawSlots() {
    if (this.textureSlots == null
        || this.textureSlots.getWidth() != this.width()
        || this.textureSlots.getHeight() != this.height()) {
      if (this.textureSlots != null) this.textureSlots.dispose();

      // Minimized windows have 0 width and height -> container will be 0x0 -> Crash on pixmap
      // creation
      if (this.width() <= 0 || this.height() <= 0) return;

      Pixmap pixmap = new Pixmap(this.width(), this.height(), Pixmap.Format.RGBA8888);
      pixmap.setColor(BORDER_COLOR);
      int rows = (int) Math.ceil(this.inventoryComponent.items().length / (float) this.slotsPerRow);
      for (int y = 0; y < rows; y++) {
        for (int x = 0; x < this.slotsPerRow; x++) {
          if (x + y * this.slotsPerRow >= this.inventoryComponent.items().length) break;
          pixmap.drawRectangle(
              x * this.slotSize + BORDER_PADDING,
              pixmap.getHeight() - ((y * this.slotSize) + BORDER_PADDING),
              this.slotSize - (2 * BORDER_PADDING),
              -this.slotSize + (2 * BORDER_PADDING));
        }
      }
      this.textureSlots = new Texture(pixmap);
    }
  }

  private void drawInventoryTitle(Batch batch) {

    GlyphLayout glyphLayout = new GlyphLayout(bitmapFont, this.title);

    int x = this.x() + (this.width() / 2) - Math.round(glyphLayout.width) / 2;
    int y = this.y() + this.height() + BORDER_PADDING;

    batch.draw(
        hoverBackground,
        x,
        y,
        glyphLayout.width + (BORDER_PADDING * 2),
        glyphLayout.height + (BORDER_PADDING * 2));
    bitmapFont.setColor(Color.BLACK);
    bitmapFont.draw(batch, this.title, x + BORDER_PADDING, y + glyphLayout.height + BORDER_PADDING);
  }

  private void drawItemInfo(Batch batch) {
    // Flip Y axis (mouse origin top left, batch origin bottom left)
    Point mousePos = new Point(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    Point relMousePos = new Point(mousePos.x() - this.x(), mousePos.y() - this.y());

    // Check if mouse is in inventory bounds
    if (mousePos.x() < this.x() || mousePos.x() > this.x() + this.width()) return;
    if (mousePos.y() < this.y() || mousePos.y() > this.y() + this.height()) return;

    // Check if mouse is dragging an item
    if (this.isDragging()) return;

    int hoveredSlot = this.getSlotByCoordinates(relMousePos.x(), relMousePos.y());
    Optional<Item> item = InventoryGUI.this.inventoryComponent.get(hoveredSlot);
    if (item.isEmpty()) return;
    Item itemToShow = item.get();

    String title = itemToShow.displayName();
    String description = UIUtils.formatString(itemToShow.description());
    GlyphLayout layoutName = new GlyphLayout(bitmapFont, title);
    GlyphLayout layoutDesc = new GlyphLayout(bitmapFont, description);

    Point hoverPos = mousePos.translate(HOVER_OFFSET);
    float width = Math.max(layoutName.width, layoutDesc.width) + HOVER_OFFSET.x();
    float height = layoutName.height + layoutDesc.height + HOVER_OFFSET.y() + LINE_GAP;

    // if out of bounds, move to the left of cursor
    if (hoverPos.x() + width > Gdx.graphics.getWidth()) {
      hoverPos = hoverPos.translate(Vector2.of(-width - HOVER_OFFSET.x(), 0));
    }

    batch.draw(hoverBackground, hoverPos.x(), hoverPos.y(), width, height);
    Point textPos = hoverPos.translate(Vector2.of(BORDER_PADDING, layoutDesc.height + LINE_GAP));

    bitmapFont.setColor(Color.BLACK);
    bitmapFont.draw(
        batch,
        title,
        textPos.x(),
        textPos.y() + layoutName.height + LINE_GAP); // place above description
    bitmapFont.setColor(new Color(0x000000b0));
    bitmapFont.draw(batch, description, textPos.x(), textPos.y());
  }

  private static boolean isPlayersInventory(Entity player, InventoryComponent inventoryComponent) {
    Optional<Entity> owner = Game.findInAll(inventoryComponent);
    return owner.isPresent() && owner.get().id() == player.id();
  }

  @Override
  protected void initInteraction(GuiInteractionContext interactionContext) {
    gdxDragAndDrop()
      .ifPresent(
        dragAndDrop -> {
          dragAndDrop.addSource(dropAndDropSource);
          dragAndDrop.addTarget(dropAndDropTarget);
        });
  }

  private void handleInput() {
    Entity player = Game.player().orElse(null);
    if (player == null) {
      return;
    }

    if (isDragging() || currentDragPayload() != null) {
      return;
    }

    int hoveredSlot = this.getHoveredSlotIndex();
    if (hoveredSlot < 0) {
      return;
    }

    if (inPlayerInventory(player)) {
      if (InputManager.isKeyJustPressed(KeyboardConfig.USE_ITEM.value())) {
        this.useHoveredSlot(player, hoveredSlot);
        return;
      }

      if (InputManager.isButtonJustPressed(KeyboardConfig.MOUSE_USE_ITEM.value())) {
        this.useHoveredSlot(player, hoveredSlot);
      }
      return;
    }

    if (InputManager.isButtonJustPressed(KeyboardConfig.TRANSFER_ITEM.value())) {
      this.transferHoveredSlot(player, hoveredSlot);
    }
  }

  private int getHoveredSlotIndex() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return -1;
    }

    int mouseX = stage.mouseX();
    int mouseY = Math.round(stage.getHeight()) - stage.mouseY();

    if (mouseX < this.x()
      || mouseX >= this.x() + this.width()
      || mouseY < this.y()
      || mouseY >= this.y() + this.height()) {
      return -1;
    }

    int slot =
      this.getSlotByCoordinates(
        mouseX - this.x(),
        mouseY - this.y());

    return slot >= 0 && slot < this.inventoryComponent.items().length ? slot : -1;
  }

  private void useHoveredSlot(Entity player, int slot) {
    if (Game.network().isServer()) {
      HeroController.useItem(player, slot);
    } else {
      Game.network()
        .send(
          (short) 0,
          new InputMessage(InputMessage.Action.INV_USE, Vector2.of(slot, 0)),
          true);
    }
  }

  private void transferHoveredSlot(Entity player, int slot) {
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

  private boolean isDragging() {
    return gdxDragAndDrop().map(DragAndDrop::isDragging).orElse(false);
  }

  private DragAndDrop.Payload currentDragPayload() {
    return gdxDragAndDrop().map(DragAndDrop::getDragPayload).orElse(null);
  }

  private void handleDraggedItemDroppedOutside(ItemDragPayload itemDragPayload) {
    if (Game.network().isServer()) {
      HeroController.dropItem(
        Game.player().orElseThrow(),
        itemDragPayload.inventoryComponent(),
        itemDragPayload.slot());
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
    int sourceSlot = itemDragPayload.slot();
    if (itemDragPayload.wasHeroInv()) {
      sourceSlot = (-sourceSlot) - 1;
    }

    int targetSlot = slot;
    if (isPlayersInventory(Game.player().orElseThrow(), this.inventoryComponent)) {
      targetSlot = (-slot) - 1;
    }

    if (Game.network().isServer()) {
      HeroController.moveItem(Game.player().orElseThrow(), sourceSlot, targetSlot);
    } else {
      Game.network()
        .send(
          (short) 0,
          new InputMessage(
            InputMessage.Action.INV_MOVE, Vector2.of(sourceSlot, targetSlot)),
          true);
    }
  }
}
