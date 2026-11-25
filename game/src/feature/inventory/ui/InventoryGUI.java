package feature.inventory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Disposable;
import engine.Entity;
import engine.Game;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.language.Localization;
import engine.language.Translation;
import engine.network.messages.c2s.InputMessage;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.Vector2;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.entities.HeroController;
import feature.hud.IInventoryHolder;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogCreationException;
import feature.hud.elements.CombinableGUI;
import feature.hud.elements.GUICombination;
import feature.hud.elements.RichLabel;
import feature.inventory.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shows an inventory as a grid of slots on a plate, with its name above the slots.
 *
 * <p>The inventories of a dialog form one stack in the middle of the screen: the player's own
 * inventory at the bottom, a container above it. The slot under the pointer is the selected one,
 * for the mouse as for the pad cursor, which the D-pad moves from slot to slot. The own inventory
 * describes the selected item of either inventory below its slots, and a bar at the bottom of the
 * screen names the inputs that do something with that item.
 */
public class InventoryGUI extends CombinableGUI
    implements IInventoryHolder, Disposable, CursorUtil.CursorOverride {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(InventoryGUI.class);
  private static final Translation trans = new Translation("dialog.inventory_dialog");

  private static final int MAX_ITEMS_PER_ROW = 8;
  private static final int MAX_SLOT_SIZE = 76;
  private static final int PADDING = 18;
  private static final int SLOT_GAP = 8;
  private static final int SCREEN_MARGIN = 20;

  /**
   * Room the layout keeps below the slots for the name and two lines of description of an item. The
   * plate ends below the slots while no item is selected and grows downwards for the description,
   * beyond this room for a longer one.
   */
  private static final int DESCRIPTION_HEIGHT = 84;

  /** Width that keeps the description readable below a few slots. */
  private static final int DESCRIPTION_MIN_WIDTH = 380;

  /** Height kept free at the bottom of the screen for the bar of input hints. */
  private static final int HINT_BAR_HEIGHT = 44;

  /** Distance from the line below the slots to the top of the description text. */
  private static final int DESCRIPTION_TOP = 34;

  /** Height of description text shown at once, five lines; a longer text scrolls by itself. */
  private static final int DESCRIPTION_MAX_TEXT = 80;

  /** Speed of a scrolling description in pixels per second, about one line. */
  private static final float DESCRIPTION_SCROLL_SPEED = 18f;

  /** Seconds a scrolling description rests at its start and at its end. */
  private static final float DESCRIPTION_SCROLL_PAUSE = 2.5f;

  private static final Color SLOT = new Color(0xcdcfddff);
  private static final Color SLOT_EDGE = new Color(0xa9acbfff);
  private static final Color SLOT_SELECTED = new Color(0xf4f5faff);
  private static final Color SLOT_SELECTED_EDGE = new Color(0x1e9bd7ff);
  private static final Color TEXT = new Color(0x111118ff);
  private static final Color TEXT_MUTED = new Color(0x3a3a48ff);
  private static final Map<Integer, NinePatch> PATCHES = new HashMap<>();
  private static NinePatch menuPlate;

  private final DragAndDrop.Source dropAndDropSource;
  private final DragAndDrop.Target dropAndDropTarget;
  private final InventoryComponent inventoryComponent;
  private final List<Actor> slots = new ArrayList<>();
  private final String title;
  private BitmapFont titleFont;
  private BitmapFont nameFont;
  private RichLabel descriptionLabel;
  private String descriptionShown;

  /** Height of the description text that is visible, and the seconds this text has been shown. */
  private float descriptionVisible;

  private float descriptionTime;
  private RichLabel hintLabel;
  private String hintsShown;

  /** Whether this is the inventory of the local player. */
  private boolean own;

  /** Whether this inventory shows the item description and the hint bar of its dialog. */
  private boolean describes;

  private int slotSize = 0;
  private final int slotsPerRow;
  private final int rows;
  private int gridOffsetX;
  private int headerHeight;

  /** Position and height the layout gave this inventory, before the description resizes it. */
  private int layoutY;

  private int layoutHeight;

  private InventoryGUI(String title, InventoryComponent inventoryComponent) {
    this.inventoryComponent = inventoryComponent;
    this.title = title;
    // Rows of equal length instead of full rows and a rest.
    int slotCount = Math.max(1, inventoryComponent.items().length);
    this.rows = (int) Math.ceil(slotCount / (float) MAX_ITEMS_PER_ROW);
    this.slotsPerRow = (int) Math.ceil(slotCount / (float) rows);

    if (Game.isHeadless()) {
      this.dropAndDropSource = null;
      this.dropAndDropTarget = null;
      return;
    }
    this.dropAndDropSource = this.buildDragAndDropSource();
    this.dropAndDropTarget = this.buildDragAndDropTarget();
    this.addSlots();
  }

  // The player's inventory is always labelled as their own. A dialog can name its container;
  // otherwise the container takes the entry of its entity name under container_names in the
  // translation files, e.g. "chest", or the generic label. The name tells the inventories apart
  // at a glance.
  private static String title(DialogContext ctx, String key, Entity owner) {
    if (Game.player().filter(player -> player.id() == owner.id()).isPresent()) {
      return trans.text("own");
    }
    return ctx.find(key, String.class)
        .filter(title -> !title.isBlank())
        .map(InventoryGUI::displayText)
        .orElseGet(() -> containerTitle(owner));
  }

  private static String containerTitle(Entity owner) {
    return Optional.of(owner.name())
        // Entities of one kind may be numbered, e.g. "chest_3".
        .map(name -> name.replaceFirst("_\\d+$", ""))
        // Only a plain name is a single entry; an unnamed entity is "_" and its id.
        .filter(name -> name.matches("[\\w-]+"))
        .map(name ->
            trans.lookup(Localization.getInstance().currentLanguage(), "container_names." + name))
        .orElseGet(() -> trans.text("container"));
  }

  private static InventoryGUI buildInventory(DialogContext ctx, String entityKey, String titleKey) {
    Entity entity = ctx.requireEntity(entityKey);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    if (inventory == null) {
      String type = ctx.dialogType().type();
      LOGGER.warn("Entity {} has no InventoryComponent for {}", entity, type);
      throw new DialogCreationException("Missing InventoryComponent for " + type);
    }
    return new InventoryGUI(title(ctx, titleKey, entity), inventory);
  }

  /**
   * Builds an InventoryDialog from the given DialogContext for a single inventory.
   *
   * @param ctx The dialog context containing the entity with the inventory component.
   * @return A new InventoryDialog instance.
   */
  public static Group buildSimple(DialogContext ctx) {
    return new GUICombination(
        buildInventory(ctx, DialogContextKeys.ENTITY, DialogContextKeys.TITLE));
  }

  /**
   * Builds an InventoryDialog from the given DialogContext for dual inventories.
   *
   * @param ctx The dialog context containing the entities with the inventory components.
   * @return A new InventoryDialog instance.
   */
  public static Group buildDual(DialogContext ctx) {
    return new GUICombination(
        buildInventory(ctx, DialogContextKeys.ENTITY, DialogContextKeys.TITLE),
        buildInventory(ctx, DialogContextKeys.SECONDARY_ENTITY, DialogContextKeys.SECONDARY_TITLE));
  }

  @Override
  public void draw(Batch batch) {
    Optional<Item> described = describes ? fitDescription() : Optional.empty();
    drawPlate(batch, x(), y(), width(), height());
    int selected = this.getSlotByMousePosition();
    for (int i = 0; i < this.slots.size(); i++) {
      this.drawSlot(batch, this.x() + slotX(i), this.y() + slotY(i), i == selected);
    }
    batch.setColor(Color.WHITE);
    this.drawItems(batch);
    if (!this.title.isEmpty()) {
      titleFont.draw(
          batch, this.title, this.x() + PADDING + 2, this.y() + this.height() - PADDING - 2);
    }
    if (describes) this.drawDescription(batch, described);
  }

  @Override
  protected void drawTopLayer(Batch batch) {
    if (describes) this.drawHintBar(batch);
  }

  @Override
  public Optional<Cursors> cursorOverride() {
    if (dragging()) return Optional.of(Cursors.GRABBING);
    return selectedItem().isPresent() ? Optional.of(Cursors.GRAB) : Optional.empty();
  }

  private boolean dragging() {
    return dragAndDrop() != null && dragAndDrop().isDragging();
  }

  private int getSlotByMousePosition() {
    Vector2 pointer = GameInput.pointer();
    return getSlotByCoordinates(
        pointer.x() - this.x(), Gdx.graphics.getHeight() - pointer.y() - this.y());
  }

  // The item in the slot under the pointer.
  private Optional<Item> selectedItem() {
    int slot = getSlotByMousePosition();
    return slot < 0 ? Optional.empty() : inventoryComponent.get(slot);
  }

  // The inventories of this dialog, this one included.
  private List<InventoryGUI> inventories() {
    List<InventoryGUI> guis = new ArrayList<>();
    if (getParent() == null) return guis;
    for (Actor actor : getParent().getChildren()) {
      if (actor instanceof InventoryGUI gui) guis.add(gui);
    }
    return guis;
  }

  // Left edge of a slot, relative to this GUI.
  private int slotX(int slot) {
    return PADDING + gridOffsetX + (slot % slotsPerRow) * (slotSize + SLOT_GAP);
  }

  // Bottom edge of a slot, relative to this GUI; the first row is the top one.
  private int slotY(int slot) {
    int row = slot / slotsPerRow;
    return height() - PADDING - headerHeight - (row + 1) * slotSize - row * SLOT_GAP;
  }

  // Finds the slot at a position relative to this GUI. A slot also takes half of the gap around it,
  // so a position between two slots belongs to the nearer one.
  //
  // Returns the slot index, or -1 on the plate or the title.
  private int getSlotByCoordinates(float x, float y) {
    float fromLeft = x - PADDING - gridOffsetX + SLOT_GAP / 2f;
    float fromTop = height() - PADDING - headerHeight - y + SLOT_GAP / 2f;
    if (slotSize <= 0 || fromLeft < 0 || fromTop < 0) return -1;
    int pitch = slotSize + SLOT_GAP;
    int column = (int) (fromLeft / pitch);
    if (column >= slotsPerRow) return -1;
    int slot = column + (int) (fromTop / pitch) * slotsPerRow;
    return slot < inventoryComponent.items().length ? slot : -1;
  }

  // Size at which an item is drawn in a slot. Pixel art stays sharp at a whole-number scale, so
  // that is used when it costs little size.
  private Vector2 itemSize(TextureRegion region) {
    float maxSize = slotSize - 2 * (slotSize / 12);
    float regionW = region.getRegionWidth();
    float regionH = region.getRegionHeight();
    if (regionW <= 0 || regionH <= 0) return Vector2.of(maxSize, maxSize);
    float scale = maxSize / Math.max(regionW, regionH);
    float whole = (float) Math.floor(scale);
    if (whole >= 0.85f * scale) scale = whole;
    return Vector2.of(regionW * scale, regionH * scale);
  }

  private void drawItems(Batch batch) {
    for (int i = 0; i < this.inventoryComponent.items().length; i++) {
      if (this.inventoryComponent.items()[i] == null) continue;

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

      TextureRegion region = this.inventoryComponent.items()[i].inventoryAnimation().update();
      Vector2 size = itemSize(region);
      float drawX = this.x() + slotX(i) + Math.round((slotSize - size.x()) / 2f);
      float drawY = this.y() + slotY(i) + Math.round((slotSize - size.y()) / 2f);
      batch.draw(region, drawX, drawY, size.x(), size.y());
    }
  }

  // The plate of the game's menus, a light panel with a rim and a rivet in each corner, at half
  // the size so that its corners fit small inventories.
  private static void drawPlate(Batch batch, float x, float y, float width, float height) {
    if (menuPlate == null) {
      menuPlate = new NinePatch(UIUtils.defaultSkin().getPatch("window_background_big"));
      menuPlate.scale(0.5f, 0.5f);
    }
    batch.setColor(Color.WHITE);
    menuPlate.draw(batch, x, y, width, height);
  }

  private void drawSlot(Batch batch, int x, int y, boolean selected) {
    int border = selected ? 3 : 2;
    int inner = slotSize - 2 * border;
    round(batch, selected ? SLOT_SELECTED_EDGE : SLOT_EDGE, 7, x, y, slotSize, slotSize);
    round(batch, selected ? SLOT_SELECTED : SLOT, 7 - border, x + border, y + border, inner, inner);
  }

  private static void round(
      Batch batch, Color color, int radius, float x, float y, float w, float h) {
    batch.setColor(color);
    patch(radius).draw(batch, x, y, w, h);
  }

  // Builds a white, antialiased rounded rectangle as a nine-patch to tint through the batch.
  private static NinePatch patch(int radius) {
    return PATCHES.computeIfAbsent(
        radius,
        key -> {
          int size = 2 * radius + 2;
          Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
          pixmap.setBlending(Pixmap.Blending.None);
          for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
              float dx = px + 0.5f - Math.clamp(px + 0.5f, radius, size - radius);
              float dy = py + 0.5f - Math.clamp(py + 0.5f, radius, size - radius);
              float alpha = Math.clamp(radius - (float) Math.hypot(dx, dy) + 0.5f, 0f, 1f);
              pixmap.drawPixel(px, py, 0xffffff00 | Math.round(alpha * 255));
            }
          }
          Texture texture = new Texture(pixmap);
          pixmap.dispose();
          return new NinePatch(texture, radius, radius, radius, radius);
        });
  }

  // Lays out the description of the selected item of this dialog and sizes the plate for it: it
  // ends below the slots without an item and grows downwards for the text, up to a maximum and as
  // far as the hint bar allows; a longer text scrolls by itself. The slots keep their place on the
  // screen, so the pointer stays on the item.
  //
  // Returns the item to describe; empty while none is selected or one is dragged.
  private Optional<Item> fitDescription() {
    Optional<Item> item =
        dragging()
            ? Optional.empty()
            : inventories().stream().flatMap(gui -> gui.selectedItem().stream()).findFirst();
    int needed = 0;
    if (item.isPresent()) {
      String description = RichLabel.toRichText(displayText(item.get().description()));
      // Prompts in the description follow the device.
      String shown = description + GameInput.device() + GameInput.padName();
      if (descriptionLabel == null) {
        descriptionLabel = new RichLabel(description, 14, TEXT_MUTED, false);
        descriptionLabel.setWrap(true);
      } else if (!shown.equals(descriptionShown)) {
        descriptionLabel.setText(description);
      }
      descriptionTime =
          shown.equals(descriptionShown) ? descriptionTime + Gdx.graphics.getDeltaTime() : 0;
      descriptionShown = shown;
      descriptionLabel.setWidth(this.width() - 2 * PADDING - 4);
      descriptionLabel.setHeight(descriptionLabel.getPrefHeight());
      descriptionLabel.validate();
      // The text gets what the hint bar leaves below the stack, up to its maximum; the rest
      // scrolls.
      int room = DESCRIPTION_HEIGHT + Math.max(0, layoutY - stackBottom()) - 12 - DESCRIPTION_TOP;
      descriptionVisible =
          Math.min(descriptionLabel.getHeight(), Math.min(DESCRIPTION_MAX_TEXT, room));
      needed = 12 + DESCRIPTION_TOP + (int) Math.ceil(descriptionVisible);
    }
    int extra = needed - DESCRIPTION_HEIGHT;
    if (this.height() != layoutHeight + extra) {
      this.height(layoutHeight + extra);
      this.y(layoutY - extra);
      this.placeSlots();
    }
    return item;
  }

  // Name and description of an item below the slots, laid out by fitDescription().
  private void drawDescription(Batch batch, Optional<Item> item) {
    float left = this.x() + PADDING;
    if (item.isEmpty()) return;
    float top = slotY(this.slots.size() - 1) + this.y() - 12;
    round(batch, SLOT_EDGE, 1, left, top, this.width() - 2 * PADDING, 2);
    batch.setColor(Color.WHITE);
    GlyphLayout name = new GlyphLayout(nameFont, displayText(item.get().displayName()));
    nameFont.draw(batch, name, left + 2, top - 10);

    // A text taller than its area rests at its start, scrolls to its end, rests and starts over.
    float hidden = Math.max(0, descriptionLabel.getHeight() - descriptionVisible);
    float cycle = 2 * DESCRIPTION_SCROLL_PAUSE + hidden / DESCRIPTION_SCROLL_SPEED;
    float scrolled =
        Math.clamp(
            (descriptionTime % cycle - DESCRIPTION_SCROLL_PAUSE) * DESCRIPTION_SCROLL_SPEED,
            0,
            hidden);
    float textTop = top - DESCRIPTION_TOP;
    descriptionLabel.setPosition(left + 2, textTop - descriptionLabel.getHeight() + scrolled);
    Rectangle area =
        new Rectangle(
            left, textTop - descriptionVisible, this.width() - 2 * PADDING, descriptionVisible);
    Rectangle scissors = new Rectangle();
    batch.flush();
    ScissorStack.calculateScissors(
        getStage().getCamera(), batch.getTransformMatrix(), area, scissors);
    if (ScissorStack.pushScissors(scissors)) {
      descriptionLabel.draw(batch, 1f);
      batch.flush();
      ScissorStack.popScissors();
    }
  }

  private void placeSlots() {
    for (int i = 0; i < slots.size(); i++) {
      // With half of the gap around it, see getSlotByCoordinates.
      slots
          .get(i)
          .setBounds(
              slotX(i) - SLOT_GAP / 2f,
              slotY(i) - SLOT_GAP / 2f,
              slotSize + SLOT_GAP,
              slotSize + SLOT_GAP);
    }
  }

  // The inputs that do something right now, as a bar at the bottom of the screen.
  private void drawHintBar(Batch batch) {
    String hints = hints();
    // Prompts follow the device.
    String shown = hints + GameInput.device() + GameInput.padName();
    if (hintLabel == null) {
      hintLabel = new RichLabel(hints, 15, TEXT, false);
    } else if (!shown.equals(hintsShown)) {
      hintLabel.setText(hints);
    }
    hintsShown = shown;
    hintLabel.setWidth(hintLabel.getPrefWidth());
    hintLabel.setHeight(hintLabel.getPrefHeight());
    hintLabel.validate();

    float width = hintLabel.getWidth() + 2 * PADDING;
    float x = Math.round((Game.stage().orElseThrow().getWidth() - width) / 2f);
    drawPlate(batch, x, SCREEN_MARGIN, width, HINT_BAR_HEIGHT);
    batch.setColor(Color.WHITE);
    hintLabel.setPosition(
        x + PADDING, SCREEN_MARGIN + Math.round((HINT_BAR_HEIGHT - hintLabel.getHeight()) / 2f));
    hintLabel.draw(batch, 1f);
  }

  // Lists the inputs for the selected item as rich text: only what the item, its inventory and the
  // other inventory allow, with the prompts of the device in use.
  private String hints() {
    boolean pad = GameInput.device() == InputDevice.PAD;
    StringBuilder hints = new StringBuilder();
    InventoryGUI selected =
        inventories().stream()
            .filter(gui -> gui.selectedItem().isPresent())
            .findFirst()
            .orElse(null);
    // The slots take the mouse themselves, so only the pad has bindings for click and transfer.
    String click = pad ? "[action id=ui:click]" : "[binding id=mouse:LEFT]";
    if (dragging()) {
      hint(hints, click, trans.text("hint_release"));
    } else if (selected != null) {
      Item item = selected.selectedItem().orElseThrow();
      hint(hints, click, trans.text("hint_move"));
      if (selected.own) {
        item.useHint()
            .ifPresent(use -> hint(hints, "[action id=ui:use_item]", trans.text("use_" + use)));
      }
      if (selected.transferTarget().isPresent()) {
        hint(
            hints,
            pad ? "[action id=ui:transfer_item]" : "[binding id=mouse:RIGHT]",
            trans.text(selected.own ? "hint_store" : "hint_take"));
      }
      if (selected.own && item.canDrop()) {
        hint(hints, "[action id=ui:drop_item]", trans.text("hint_drop"));
      }
    }
    hint(hints, "[action id=ui:close]", Game.localization().text("input.action.ui.close"));
    return hints.toString();
  }

  private static void hint(StringBuilder hints, String prompt, String text) {
    if (!hints.isEmpty()) hints.append("      ");
    hints.append(prompt).append(' ').append(text);
  }

  private static boolean isPlayersInventory(Entity player, InventoryComponent inventoryComponent) {
    Optional<Entity> owner = Game.findInAll(inventoryComponent);
    return owner.isPresent() && owner.get().id() == player.id();
  }

  // Resolves the keys of the room's translator in a synchronized item or title text.
  private static String displayText(String text) {
    return Game.localization().getCurrentTranslator().translate(text == null ? "" : text);
  }

  @Override
  protected void initDragAndDrop(DragAndDrop dragAndDrop) {
    dragAndDrop.addSource(dropAndDropSource);
    dragAndDrop.addTarget(dropAndDropTarget);
  }

  private DragAndDrop.Source buildDragAndDropSource() {
    return new DragAndDrop.Source(this) {
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
        Sprite dragRegion = itemToTransfer.inventoryAnimation().update();
        Image image = new Image(new SpriteDrawable(dragRegion));
        Vector2 dragSize = itemSize(dragRegion);
        image.setSize(dragSize.x(), dragSize.y());
        // The cursor reads the UI under the dragged item.
        image.setTouchable(Touchable.disabled);
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
        // A drag ended by focus loss or an unplugged pad must not drop the item.
        if (target == null
            && !event.isTouchFocusCancel()
            && payload != null
            && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
          if (Game.network().isServer()) {
            HeroController.dropItem(
                Game.player().orElseThrow(),
                itemDragPayload.inventoryComponent(),
                itemDragPayload.slot());
          } else if (isPlayersInventory(
              Game.player().orElseThrow(), itemDragPayload.inventoryComponent())) {
            // The server drops the slot from the player's own inventory, so items from another
            // inventory, e.g. a chest, have to be moved into the player's inventory first.
            Game.network().send((short) 0, InputMessage.invDrop(itemDragPayload.slot()), true);
          }
        }
      }
    };
  }

  private DragAndDrop.Target buildDragAndDropTarget() {
    return new DragAndDrop.Target(this) {
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
        // Drops on the border or padding keep the item in its slot.
        if (slot < 0) return;
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
          if (sourceSlot == targetSlot) return;
          if (Game.network().isServer()) {
            HeroController.moveItem(Game.player().orElseThrow(), sourceSlot, targetSlot);
          } else {
            Game.network().send((short) 0, InputMessage.invMove(sourceSlot, targetSlot), true);
          }
        }
      }
    };
  }

  // Every slot is a control of its own, so that the D-pad moves the pad cursor from slot to slot.
  private void addSlots() {
    for (int i = 0; i < this.inventoryComponent.items().length; i++) {
      Actor actor = new Actor();
      actor.addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (button == Input.Buttons.RIGHT) return transferItemAtPointer();
              // The selected slot, not this actor's: they differ on the line between two slots.
              if (selectedItem().isPresent()) GameInput.startDrag();
              return false;
            }
          });
      this.slots.add(actor);
      this.addActor(actor);
    }
  }

  /**
   * Uses the item under pointer 0, if it lies in the local player's own inventory.
   *
   * @return true if a use was sent
   */
  public boolean useItemAtPointer() {
    // A dragged item is in the player's hand; its slot takes no other action until it is released.
    if (dragging()) return false;
    Entity player = Game.player().orElseThrow();
    if (!isPlayersInventory(player, inventoryComponent)) return false;
    int slot = getSlotByMousePosition();
    if (slot < 0) return false;
    if (Game.network().isServer()) return HeroController.useItem(player, slot);
    Game.network().send((short) 0, InputMessage.invUse(slot), true);
    return true;
  }

  /**
   * Drops the item under pointer 0 at the player's feet, if it lies in the local player's own
   * inventory and can be dropped.
   *
   * @return true if a drop was sent
   */
  public boolean dropItemAtPointer() {
    // A dragged item is in the player's hand; its slot takes no other action until it is released.
    if (dragging()) return false;
    Entity player = Game.player().orElseThrow();
    if (!isPlayersInventory(player, inventoryComponent)) return false;
    int slot = getSlotByMousePosition();
    if (selectedItem().filter(Item::canDrop).isEmpty()) return false;
    if (Game.network().isServer()) {
      HeroController.dropItem(player, inventoryComponent, slot);
    } else {
      Game.network().send((short) 0, InputMessage.invDrop(slot), true);
    }
    return true;
  }

  // The other inventory of this dialog, if it has a free slot.
  private Optional<InventoryComponent> transferTarget() {
    return inventories().stream()
        .filter(gui -> gui != this)
        .map(InventoryGUI::inventoryComponent)
        .filter(inventory -> inventory.findNextAvailableSlot() != -1)
        .findFirst();
  }

  /**
   * Moves the item under pointer 0 to the other open inventory.
   *
   * @return true if a move was sent; false without another inventory or free slot
   */
  public boolean transferItemAtPointer() {
    // A dragged item is in the player's hand; its slot takes no other action until it is released.
    if (dragging()) return false;
    Entity player = Game.player().orElseThrow();
    int sourceSlot = getSlotByMousePosition();
    if (sourceSlot < 0) return false;
    if (isPlayersInventory(player, inventoryComponent)) {
      sourceSlot = (-sourceSlot) - 1; // negative slots for hero inventory
    }
    Optional<InventoryComponent> targetInventory = transferTarget();
    if (targetInventory.isEmpty()) return false;

    int targetSlot = targetInventory.get().findNextAvailableSlot();
    if (isPlayersInventory(player, targetInventory.get())) {
      targetSlot = (-targetSlot) - 1; // negative slots for hero
    }
    if (Game.network().isServer()) return HeroController.moveItem(player, sourceSlot, targetSlot);
    Game.network().send((short) 0, InputMessage.invMove(sourceSlot, targetSlot), true);
    return true;
  }

  @Override
  protected Vector2 preferredSize(GUICombination.AvailableSpace availableSpace) {
    List<InventoryGUI> inventories = inventories();
    inventories.forEach(gui -> gui.measure(inventories));

    // The stack shares the screen above the hint bar: what its plates, titles and the description
    // leave of the height goes to the rows of all inventories alike.
    int stageWidth = (int) Game.stage().orElseThrow().getWidth();
    int slotsHeight =
        (int) Game.stage().orElseThrow().getHeight()
            - stackBottom()
            - SCREEN_MARGIN
            - (inventories.size() - 1) * GUICombination.GAP;
    int allRows = 0;
    for (InventoryGUI gui : inventories) {
      slotsHeight -= gui.fixedHeight() + (gui.rows - 1) * SLOT_GAP;
      allRows += gui.rows;
    }
    int byWidth =
        (stageWidth - 2 * SCREEN_MARGIN - 2 * PADDING - (slotsPerRow - 1) * SLOT_GAP) / slotsPerRow;
    this.slotSize = Math.max(1, Math.min(MAX_SLOT_SIZE, Math.min(byWidth, slotsHeight / allRows)));
    int gridWidth = slotsPerRow * slotSize + (slotsPerRow - 1) * SLOT_GAP;
    int width = 2 * PADDING + gridWidth;
    if (describes) width = Math.max(width, DESCRIPTION_MIN_WIDTH);
    if (!title.isEmpty()) {
      int titleWidth = (int) Math.ceil(new GlyphLayout(titleFont, title).width);
      width = Math.max(width, titleWidth + 2 * PADDING + 4);
    }
    this.gridOffsetX = (width - 2 * PADDING - gridWidth) / 2;
    return Vector2.of(width, fixedHeight() + rows * slotSize + (rows - 1) * SLOT_GAP);
  }

  // Works out what does not depend on the slot size: role and title height.
  private void measure(List<InventoryGUI> inventories) {
    Entity player = Game.player().orElseThrow();
    this.own = isPlayersInventory(player, this.inventoryComponent);
    // The own inventory describes the items; a dialog without it leaves that to its first one.
    boolean ownShown =
        inventories.stream().anyMatch(gui -> isPlayersInventory(player, gui.inventoryComponent));
    this.describes = ownShown ? own : inventories.indexOf(this) == 0;
    this.titleFont =
        FontHelper.getFont(FontSpec.of(Scene2dElementFactory.FONT_PATH_BOLD, 20, TEXT));
    this.nameFont = FontHelper.getFont(FontSpec.of(Scene2dElementFactory.FONT_PATH_BOLD, 17, TEXT));
    this.headerHeight = this.title.isEmpty() ? 0 : (int) titleFont.getCapHeight() + 16;
  }

  // Height of everything but the slots.
  private int fixedHeight() {
    return 2 * PADDING + headerHeight + (describes ? DESCRIPTION_HEIGHT : 0);
  }

  // Lower edge of the stack of inventories, above the hint bar.
  private static int stackBottom() {
    return SCREEN_MARGIN + HINT_BAR_HEIGHT + GUICombination.GAP;
  }

  /**
   * Stacks the inventories of this dialog in the middle of the screen, each as wide as its own
   * slots need, the describing one at the bottom. Runs for every inventory; the last run sees all
   * of them sized.
   */
  @Override
  protected void boundsUpdate() {
    List<InventoryGUI> inventories = inventories();
    int height = (inventories.size() - 1) * GUICombination.GAP;
    for (InventoryGUI gui : inventories) height += gui.height();
    int stageWidth = (int) Game.stage().orElseThrow().getWidth();
    int stageHeight = (int) Game.stage().orElseThrow().getHeight();
    int y = stackBottom() + (stageHeight - SCREEN_MARGIN - stackBottom() - height) / 2;

    inventories.sort((a, b) -> Boolean.compare(b.describes, a.describes));
    for (InventoryGUI gui : inventories) {
      gui.x((stageWidth - gui.width()) / 2);
      gui.y(y);
      gui.layoutY = y;
      gui.layoutHeight = gui.height();
      gui.placeSlots();
      y += gui.height() + GUICombination.GAP;
    }
  }

  /**
   * Get the InventoryComponent associated with this InventoryGUI.
   *
   * @return the InventoryComponent
   */
  public InventoryComponent inventoryComponent() {
    return this.inventoryComponent;
  }

  @Override
  public void dispose() {
    // clear drag and drop holding item
    if (this.dragAndDrop() != null && this.dragAndDrop().isDragging()) {
      this.dragAndDrop().getDragActor().remove();
    }
  }
}
