package contrib.hud.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.entities.HeroController;
import contrib.hud.IInventoryHolder;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.network.messages.c2s.InputMessage;
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
  private static final Texture texture;
  private static final TextureRegion background, hoverBackground;

  static {
    if (Game.isHeadless()) {
      bitmapFont = null;
      texture = null;
      background = null;
      hoverBackground = null;
    } else {
      // Prepare background texture
      Pixmap pixmap = new Pixmap(2, 1, Pixmap.Format.RGBA8888);
      pixmap.drawPixel(0, 0, BACKGROUND_COLOR); // Background
      pixmap.drawPixel(1, 0, HOVER_BACKGROUND_COLOR); // Hover
      texture = new Texture(pixmap);
      background = new TextureRegion(texture, 0, 0, 1, 1);
      hoverBackground = new TextureRegion(texture, 1, 0, 1, 1);
      bitmapFont =
          new BitmapFont(
              Gdx.files.internal(FONT_FNT.pathString()),
              Gdx.files.internal(FONT_PNG.pathString()),
              false);
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
    this.dropAndDropSource = this.buildDragAndDropSource();
    this.dropAndDropTarget = this.buildDragAndDropTarget();
    this.addInputListener();
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
   * Builds an InventoryDialog from the given DialogContext for a single inventory.
   *
   * @param ctx The dialog context containing the entity with the inventory component.
   * @return A new InventoryDialog instance.
   */
  public static Group buildSimple(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    if (inventory == null) {
      LOGGER.warn("Entity {} has no InventoryComponent for InventoryDialog", entity);
      throw new DialogCreationException("Missing InventoryComponent for InventoryDialog");
    }
    InventoryGUI inventoryGUI = new InventoryGUI(inventory);
    return new GUICombination(inventoryGUI);
  }

  /**
   * Builds an InventoryDialog from the given DialogContext for dual inventories.
   *
   * @param ctx The dialog context containing the entities with the inventory components.
   * @return A new InventoryDialog instance.
   */
  public static Group buildDual(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    Entity otherEntity = ctx.requireEntity(DialogContextKeys.SECONDARY_ENTITY);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent otherInventory = otherEntity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null || otherInventory == null) {
      Entity missingEntity = (inventory == null) ? entity : otherEntity;
      LOGGER.error("Entity {} has no InventoryComponent for DualInventoryDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for DualInventoryDialog");
    }

    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(entity.name());
    InventoryGUI inventoryGUI = new InventoryGUI(title, inventory);
    String otherTitle =
        ctx.find(DialogContextKeys.SECONDARY_TITLE, String.class).orElse(otherEntity.name());
    InventoryGUI otherInventoryGUI = new InventoryGUI(otherTitle, otherInventory);
    return new GUICombination(inventoryGUI, otherInventoryGUI);
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
    this.drawItemInfo(batch);
  }

  private int getSlotByMousePosition() {
    Vector2 mousePos = Vector2.of(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
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
    for (int i = 0; i < this.inventoryComponent.items().length; i++) {
      if (this.inventoryComponent.items()[i] == null) continue;
      float x = this.x() + this.slotSize * (i % this.slotsPerRow) + (2 * BORDER_PADDING);
      float y =
          this.y()
              + this.slotSize * (float) Math.floor((i / (float) this.slotsPerRow))
              + (2 * BORDER_PADDING);

      // Don't draw item being dragged
      if (this.dragAndDrop().isDragging()) {
        DragAndDrop.Payload payload = this.dragAndDrop().getDragPayload();
        if (payload != null
            && payload.getObject() instanceof ItemDragPayload itemDragPayload
            && itemDragPayload.inventoryComponent() == this.inventoryComponent
            && itemDragPayload.slot() == i) {
          continue;
        }
      }

      batch.draw(
          this.inventoryComponent.items()[i].inventoryAnimation().update(),
          x,
          y,
          this.slotSize - (4 * BORDER_PADDING),
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
    if (this.dragAndDrop().isDragging()) return;

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
  protected void initDragAndDrop(DragAndDrop dragAndDrop) {
    dragAndDrop.addSource(dropAndDropSource);
    dragAndDrop.addTarget(dropAndDropTarget);
  }

  private DragAndDrop.Source buildDragAndDropSource() {
    return new DragAndDrop.Source(this.actor()) {
      @Override
      public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        int draggedSlot = InventoryGUI.this.getSlotByCoordinates(x, y);
        Optional<Item> item = InventoryGUI.this.inventoryComponent.get(draggedSlot);
        if (item.isEmpty()) return null;
        Item itemToTransfer = item.get();
        boolean isHeroInv =
            isPlayersInventory(Game.player().orElseThrow(), InventoryGUI.this.inventoryComponent);

        DragAndDrop.Payload payload = new DragAndDrop.Payload();
        payload.setObject(
            new ItemDragPayload(
                InventoryGUI.this.inventoryComponent, isHeroInv, draggedSlot, itemToTransfer));

        // TODO: Test if SpriteDrawable is equivalent to creating a texture on the fly
        Image image = new Image(new SpriteDrawable(itemToTransfer.inventoryAnimation().update()));
        image.setSize(InventoryGUI.this.slotSize, InventoryGUI.this.slotSize);
        payload.setDragActor(image);
        dragAndDrop().setDragActorPosition(image.getWidth() / 2, -image.getHeight() / 2);

        return payload;
      }

      @Override
      public void dragStop(
          InputEvent event,
          float x,
          float y,
          int pointer,
          DragAndDrop.Payload payload,
          DragAndDrop.Target target) {
        if (target == null
            && payload != null
            && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
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
      }
    };
  }

  private DragAndDrop.Target buildDragAndDropTarget() {
    return new DragAndDrop.Target(this.actor()) {
      @Override
      public boolean drag(
          DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        // Valid if item in hand (cursor)
        return payload.getObject() != null && payload.getObject() instanceof ItemDragPayload;
      }

      @Override
      public void drop(
          DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        int slot = InventoryGUI.this.getSlotByCoordinates(x, y);
        if (payload.getObject() != null
            && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
          int sourceSlot = itemDragPayload.slot();
          if (itemDragPayload.wasHeroInv()) {
            sourceSlot = (-sourceSlot) - 1; // negative slots for hero inventory (to distinguish)
          }
          int targetSlot = slot;
          if (isPlayersInventory(
              Game.player().orElseThrow(), InventoryGUI.this.inventoryComponent)) {
            targetSlot = (-slot) - 1; // negative slots for hero inventory (to distinguish)
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
    };
  }

  private void addInputListener() {
    Game.stage().orElseThrow().setKeyboardFocus(this.actor());

    this.actor().setBounds(0, 0, this.width(), this.height());

    this.actor()
        .addListener(
            new InputListener() {
              @Override
              public boolean keyDown(InputEvent event, int keycode) {
                Entity player = Game.player().orElseThrow();
                if (inPlayerInventory(player)) {
                  if (KeyboardConfig.USE_ITEM.value() == keycode) {
                    if (Game.network().isServer()) {
                      return HeroController.useItem(player, getSlotByMousePosition());
                    } else {
                      Game.network()
                          .send(
                              (short) 0,
                              new InputMessage(
                                  InputMessage.Action.INV_USE,
                                  Vector2.of(getSlotByMousePosition(), 0)),
                              true);
                      return true;
                    }
                  }
                }
                return false;
              }

              @Override
              public boolean touchDown(
                  InputEvent event, float x, float y, int pointer, int button) {
                Entity player = Game.player().orElseThrow();
                if (inPlayerInventory(player)) {
                  if (KeyboardConfig.MOUSE_USE_ITEM.value() == button) {
                    if (Game.network().isServer()) {
                      return HeroController.useItem(player, getSlotByMousePosition());
                    } else {
                      Game.network()
                          .send(
                              (short) 0,
                              new InputMessage(
                                  InputMessage.Action.INV_USE,
                                  Vector2.of(getSlotByMousePosition(), 0)),
                              true);
                      return true;
                    }
                  }
                  return false;
                }

                UIComponent uiComponent =
                    Game.player().flatMap(e -> e.fetch(UIComponent.class)).orElse(null);
                if (uiComponent != null && uiComponent.dialog() instanceof GUICombination) {
                  // if two inventories are open, transfer items between them if key is pressed
                  if (KeyboardConfig.TRANSFER_ITEM.value() == button) {
                    int sourceSlot = getSlotByMousePosition();
                    if (isPlayersInventory(
                        Game.player().orElseThrow(), InventoryGUI.this.inventoryComponent)) {
                      sourceSlot = (-sourceSlot) - 1; // negative slots for hero inventory
                    }
                    Optional<InventoryComponent> targetInventory =
                        UIUtils.getInventoriesFromUI(uiComponent)
                            .filter(invComp -> invComp != InventoryGUI.this.inventoryComponent)
                            .findFirst();

                    if (targetInventory.isEmpty()) return false;

                    int nextBestTargetSlot = targetInventory.get().findNextAvailableSlot();
                    if (nextBestTargetSlot == -1) {
                      LOGGER.debug("No available slot in target inventory for transfer.");
                      return false;
                    }
                    if (isPlayersInventory(Game.player().orElseThrow(), targetInventory.get())) {
                      nextBestTargetSlot = (-nextBestTargetSlot) - 1; // negative slots for hero
                    }

                    if (Game.network().isServer()) {
                      return HeroController.moveItem(
                          Game.player().orElseThrow(), sourceSlot, nextBestTargetSlot);
                    } else {
                      Game.network()
                          .send(
                              (short) 0,
                              new InputMessage(
                                  InputMessage.Action.INV_MOVE,
                                  Vector2.of(sourceSlot, nextBestTargetSlot)),
                              true);
                      return true;
                    }
                  }
                }
                return false;
              }
            });
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
}
