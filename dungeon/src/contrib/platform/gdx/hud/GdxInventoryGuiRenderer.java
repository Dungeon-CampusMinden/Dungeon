package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.hud.UIUtils;
import contrib.hud.inventory.InventoryGUI;
import contrib.hud.inventory.ItemDragPayload;
import contrib.item.Item;
import core.Game;
import core.ui.StageHandle;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Objects;
import java.util.Optional;

/**
 * libGDX-specific renderer entry point for {@link InventoryGUI}.
 *
 * <p>This adapter keeps concrete libGDX batch rendering out of the backend-neutral inventory GUI.
 */
public final class GdxInventoryGuiRenderer {

  private static final int BORDER_PADDING = 5;
  private static final int LINE_GAP = 5;
  private static final Vector2 HOVER_OFFSET = Vector2.of(10, 10);

  private GdxInventoryGuiRenderer() {}

  /**
   * Renders the main inventory layer with the active libGDX batch.
   *
   * @param inventoryGUI inventory gui to render
   * @param renderContext active libGDX render context
   */
  public static void render(
    final InventoryGUI inventoryGUI, final GdxGuiRenderContext renderContext) {
    Objects.requireNonNull(inventoryGUI, "inventoryGUI");
    Objects.requireNonNull(renderContext, "renderContext");

    Batch batch = renderContext.batch();

    inventoryGUI.ensureGdxSlotTexture();

    TextureRegion background = InventoryGUI.gdxBackgroundRegion();
    if (background != null) {
      batch.draw(
        background,
        inventoryGUI.x(),
        inventoryGUI.y(),
        inventoryGUI.width(),
        inventoryGUI.height());
    }

    Texture slotTexture = inventoryGUI.gdxSlotTexture();
    if (slotTexture != null) {
      batch.draw(
        slotTexture,
        inventoryGUI.x(),
        inventoryGUI.y(),
        inventoryGUI.width(),
        inventoryGUI.height());
    }

    drawItems(inventoryGUI, batch);

    if (!inventoryGUI.title().isEmpty()) {
      drawInventoryTitle(inventoryGUI, batch);
    }
  }

  /**
   * Renders the inventory top layer with the active libGDX batch.
   *
   * @param inventoryGUI inventory gui to render
   * @param renderContext active libGDX render context
   */
  public static void renderTopLayer(
    final InventoryGUI inventoryGUI, final GdxGuiRenderContext renderContext) {
    Objects.requireNonNull(inventoryGUI, "inventoryGUI");
    Objects.requireNonNull(renderContext, "renderContext");

    drawItemInfo(inventoryGUI, renderContext.batch());
  }

  private static void drawItems(final InventoryGUI inventoryGUI, final Batch batch) {
    Item[] items = inventoryGUI.inventoryComponent().items();

    for (int i = 0; i < items.length; i++) {
      Item item = items[i];
      if (item == null) {
        continue;
      }

      int itemX =
        inventoryGUI.x()
          + inventoryGUI.gdxSlotSize() * (i % inventoryGUI.gdxSlotsPerRow())
          + (2 * BORDER_PADDING);
      int itemY =
        inventoryGUI.y()
          + inventoryGUI.gdxSlotSize() * (i / inventoryGUI.gdxSlotsPerRow())
          + (2 * BORDER_PADDING);

      if (inventoryGUI.gdxIsDragging()) {
        DragAndDrop.Payload payload = inventoryGUI.gdxCurrentDragPayload();
        if (payload != null
          && payload.getObject() instanceof ItemDragPayload itemDragPayload
          && itemDragPayload.inventoryComponent() == inventoryGUI.inventoryComponent()
          && itemDragPayload.slot() == i) {
          continue;
        }
      }

      GdxHudItemRenderer.drawItem(
        batch,
        item,
        itemX,
        itemY,
        inventoryGUI.gdxSlotSize() - (4 * BORDER_PADDING));
    }
  }

  private static void drawInventoryTitle(final InventoryGUI inventoryGUI, final Batch batch) {
    BitmapFont font = InventoryGUI.gdxBitmapFont();
    if (font == null || inventoryGUI.title() == null || inventoryGUI.title().isBlank()) {
      return;
    }

    GlyphLayout layout = new GlyphLayout(font, inventoryGUI.title());
    font.setColor(Color.WHITE);
    font.draw(
      batch,
      inventoryGUI.title(),
      inventoryGUI.x() + (inventoryGUI.width() - layout.width) / 2f,
      inventoryGUI.y() + inventoryGUI.height() + layout.height + BORDER_PADDING);
  }

  private static void drawItemInfo(final InventoryGUI inventoryGUI, final Batch batch) {
    BitmapFont font = InventoryGUI.gdxBitmapFont();
    TextureRegion hoverBackground = InventoryGUI.gdxHoverBackgroundRegion();

    if (font == null || hoverBackground == null) {
      return;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    Point mousePos = new Point(stage.mouseX(), Math.round(stage.getHeight()) - stage.mouseY());
    Point relMousePos = new Point(mousePos.x() - inventoryGUI.x(), mousePos.y() - inventoryGUI.y());

    if (mousePos.x() < inventoryGUI.x() || mousePos.x() > inventoryGUI.x() + inventoryGUI.width()) {
      return;
    }
    if (mousePos.y() < inventoryGUI.y() || mousePos.y() > inventoryGUI.y() + inventoryGUI.height()) {
      return;
    }

    if (inventoryGUI.gdxIsDragging()) {
      return;
    }

    int hoveredSlot = inventoryGUI.gdxSlotByCoordinates((int) relMousePos.x(), (int) relMousePos.y());
    Optional<Item> item = inventoryGUI.inventoryComponent().get(hoveredSlot);
    if (item.isEmpty()) {
      return;
    }

    Item itemToShow = item.get();
    String title = itemToShow.displayName();
    String description = UIUtils.formatString(itemToShow.description());

    GlyphLayout layoutName = new GlyphLayout(font, title);
    GlyphLayout layoutDesc = new GlyphLayout(font, description);

    Point hoverPos = mousePos.translate(HOVER_OFFSET);
    float width = Math.max(layoutName.width, layoutDesc.width) + HOVER_OFFSET.x();
    float height = layoutName.height + layoutDesc.height + HOVER_OFFSET.y() + LINE_GAP;

    if (hoverPos.x() + width > stage.getWidth()) {
      hoverPos = hoverPos.translate(Vector2.of(-width - HOVER_OFFSET.x(), 0));
    }

    batch.draw(hoverBackground, hoverPos.x(), hoverPos.y(), width, height);
    Point textPos = hoverPos.translate(Vector2.of(BORDER_PADDING, layoutDesc.height + LINE_GAP));

    font.setColor(Color.BLACK);
    font.draw(batch, title, textPos.x(), textPos.y() + layoutName.height + LINE_GAP);

    font.setColor(new Color(0x000000b0));
    font.draw(batch, description, textPos.x(), textPos.y());
  }
}
