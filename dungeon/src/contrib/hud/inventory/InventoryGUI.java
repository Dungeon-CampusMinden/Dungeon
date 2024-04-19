package contrib.hud.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GUICombination;
import contrib.item.Item;
import core.Game;
import core.components.PositionComponent;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** WTF? . */
public class InventoryGUI extends CombinableGUI {

  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");
  private static final int DEFAULT_MAX_ITEMS_PER_ROW = 8;
  private static final int BORDER_COLOR = 0x9dc1ebff;
  private static final int BACKGROUND_COLOR = 0x3e3e63e1;
  private static final int HOVER_BACKGROUND_COLOR = 0xffffffff;
  private static final int BORDER_PADDING = 5;
  private static final int LINE_GAP = 5;
  private static final Vector2 HOVER_OFFSET = new Vector2(10, 10);
  private static final BitmapFont bitmapFont;
  private static final Texture texture;
  private static final TextureRegion background, hoverBackground;

  /**
   * Boolean to check if the opened inventory belongs to the hero. Items that are in an inventory
   * which does not belong to the hero, e.g. a treasure chest, are not usable.
   *
   * <p>Will be set to true if the hero inventory is opened and set to false if it's closed.
   */
  public static boolean inHeroInventory = false;

  static {
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

  private final InventoryComponent inventoryComponent;
  private Texture textureSlots;
  private String title;
  private int slotSize = 0;
  private int slotsPerRow = 0;
  private int maxItemsPerRow = DEFAULT_MAX_ITEMS_PER_ROW;

  /**
   * Create a new inventory GUI
   *
   * @param title the title of the inventory
   * @param inventoryComponent the inventory component on which the GUI is based.
   * @param maxItemsPerRow the maximum number of items per row in the inventory
   */
  public InventoryGUI(String title, InventoryComponent inventoryComponent, int maxItemsPerRow) {
    super();
    this.inventoryComponent = inventoryComponent;
    this.title = title;
    this.maxItemsPerRow = maxItemsPerRow;
    this.slotsPerRow = Math.min(maxItemsPerRow, this.inventoryComponent.items().length);
    addInputListener();
  }

  /**
   * Create a new inventory GUI.
   *
   * @param title the title of the inventory
   * @param inventoryComponent the inventory component on which the GUI is based.
   */
  public InventoryGUI(String title, InventoryComponent inventoryComponent) {
    super();
    this.inventoryComponent = inventoryComponent;
    this.title = title;
    this.slotsPerRow = Math.min(maxItemsPerRow, this.inventoryComponent.items().length);
    addInputListener();
  }

  /**
   * Create a new inventory GUI.
   *
   * @param inventoryComponent the inventory component on which the GUI is based.
   */
  public InventoryGUI(InventoryComponent inventoryComponent) {
    this.inventoryComponent = inventoryComponent;
    Game.find(inventoryComponent)
        .ifPresentOrElse(e -> this.title = e.toString(), () -> this.title = "Inventory");
    title = title.split("_(?=\\d+)")[0]; // remove id
    title = title.toUpperCase();
    this.slotsPerRow = Math.min(maxItemsPerRow, this.inventoryComponent.items().length);
    addInputListener();
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
    Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    Vector2 relMousePos = new Vector2(mousePos.x - this.x(), mousePos.y - this.y());
    return getSlotByCoordinates(relMousePos.x, relMousePos.y);
  }

  private int getSlotByCoordinates(int x, int y) {
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

      batch.draw(
          new Texture(
              this.inventoryComponent
                  .items()[i]
                  .inventoryAnimation()
                  .nextAnimationTexturePath()
                  .pathString()),
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
    Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    Vector2 relMousePos = new Vector2(mousePos.x - this.x(), mousePos.y - this.y());

    // Check if mouse is in inventory bounds
    if (mousePos.x < this.x() || mousePos.x > this.x() + this.width()) return;
    if (mousePos.y < this.y() || mousePos.y > this.y() + this.height()) return;

    // Check if mouse is dragging an item
    if (this.dragAndDrop().isDragging()) return;

    int hoveredSlot = this.getSlotByCoordinates(relMousePos.x, relMousePos.y);
    Item item = InventoryGUI.this.inventoryComponent.get(hoveredSlot);
    if (item == null) return;

    String title = item.displayName();
    String description = UIUtils.formatString(item.description());
    GlyphLayout layoutName = new GlyphLayout(bitmapFont, title);
    GlyphLayout layoutDesc = new GlyphLayout(bitmapFont, description);

    float x = mousePos.x + HOVER_OFFSET.x;
    float y = mousePos.y + HOVER_OFFSET.y;
    float width = Math.max(layoutName.width, layoutDesc.width) + HOVER_OFFSET.x;
    float height = layoutName.height + layoutDesc.height + HOVER_OFFSET.y + LINE_GAP;

    // if out of bounds, move to the left of cursor
    if (x + width > Gdx.graphics.getWidth()) {
      x = mousePos.x - width - HOVER_OFFSET.x;
    }

    batch.draw(hoverBackground, x, y, width, height);
    bitmapFont.setColor(Color.BLACK);
    bitmapFont.draw(
        batch,
        title,
        x + BORDER_PADDING,
        y + layoutDesc.height + LINE_GAP + layoutName.height + LINE_GAP);
    bitmapFont.setColor(new Color(0x000000b0));
    bitmapFont.draw(batch, description, x + BORDER_PADDING, y + layoutDesc.height + LINE_GAP);
  }

  @Override
  protected void initDragAndDrop(DragAndDrop dragAndDrop) {
    dragAndDrop.addSource(
        new DragAndDrop.Source(this.actor()) {
          @Override
          public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {

            int draggedSlot = InventoryGUI.this.getSlotByCoordinates(x, y);
            Item item = InventoryGUI.this.inventoryComponent.get(draggedSlot);
            if (item == null) return null;

            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setObject(
                new ItemDragPayload(InventoryGUI.this.inventoryComponent, draggedSlot, item));

            Image image =
                new Image(
                    new Texture(item.inventoryAnimation().nextAnimationTexturePath().pathString()));
            image.setSize(InventoryGUI.this.slotSize, InventoryGUI.this.slotSize);
            payload.setDragActor(image);
            dragAndDrop.setDragActorPosition(image.getWidth() / 2, -image.getHeight() / 2);

            InventoryGUI.this.inventoryComponent.set(draggedSlot, null);

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
              itemDragPayload
                  .item()
                  .drop(
                      Game.hero()
                          .orElseThrow(MissingHeroException::new)
                          .fetch(PositionComponent.class)
                          .orElseThrow(
                              () ->
                                  MissingComponentException.build(
                                      Game.hero().get(), PositionComponent.class))
                          .position());
            }
          }
        });

    dragAndDrop.addTarget(
        new DragAndDrop.Target(this.actor()) {
          @Override
          public boolean drag(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            if (payload.getObject() != null && payload.getObject() instanceof ItemDragPayload) {
              int slot = InventoryGUI.this.getSlotByCoordinates(x, y);
              return InventoryGUI.this.inventoryComponent.get(slot) == null
                  && slot < InventoryGUI.this.inventoryComponent.items().length
                  && slot >= 0;
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
            int slot = InventoryGUI.this.getSlotByCoordinates(x, y);
            if (payload.getObject() != null
                && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
              InventoryGUI.this.inventoryComponent.set(slot, itemDragPayload.item());
            }
          }
        });
  }

  private void addInputListener() {
    Game.stage().orElseThrow().setKeyboardFocus(this.actor());

    this.actor().setBounds(0, 0, this.width(), this.height());

    this.actor()
        .addListener(
            new InputListener() {
              @Override
              public boolean keyDown(InputEvent event, int keycode) {
                if (inHeroInventory) {
                  if (KeyboardConfig.USE_ITEM.value() == keycode) {
                    InventoryGUI.this.useItem(
                        InventoryGUI.this.inventoryComponent.get(
                            InventoryGUI.this.getSlotByMousePosition()));
                    return true;
                  }
                }
                return false;
              }

              @Override
              public boolean touchDown(
                  InputEvent event, float x, float y, int pointer, int button) {
                if (inHeroInventory) return false;

                UIComponent uiComponent =
                    Game.hero().flatMap(e -> e.fetch(UIComponent.class)).orElse(null);
                if (uiComponent != null
                    && uiComponent.dialog() instanceof GUICombination guiCombination) {
                  // if two inventories are open, transfer items between them if key is pressed
                  if (KeyboardConfig.TRANSFER_ITEM.value() == button) {
                    int slot = InventoryGUI.this.getSlotByMousePosition();
                    Item item = InventoryGUI.this.inventoryComponent.get(slot);
                    if (item != null) {
                      guiCombination
                          .combinableGuis()
                          .forEach(
                              gui -> {
                                if (gui instanceof InventoryGUI inventoryGui) {
                                  if (inventoryGui != InventoryGUI.this) {
                                    InventoryGUI.this.inventoryComponent.transfer(
                                        item, inventoryGui.inventoryComponent);
                                  }
                                } else if (gui instanceof CraftingGUI craftingGui) {
                                  craftingGui.addItem(item);
                                  InventoryGUI.this.inventoryComponent.remove(item);
                                }
                              });
                    }
                    return true;
                  }
                }
                return false;
              }
            });
  }

  private void useItem(Item item) {
    if (item != null)
      item.use(Game.hero().orElseThrow(() -> new NullPointerException("There is no hero")));
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
    return new Vector2(width, height);
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
}
