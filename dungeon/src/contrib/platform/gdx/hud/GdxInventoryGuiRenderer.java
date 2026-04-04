package contrib.platform.gdx.hud;

import contrib.hud.inventory.InventoryGUI;
import java.util.Objects;

/**
 * libGDX-specific renderer entry point for {@link InventoryGUI}.
 *
 * <p>This adapter keeps the backend-specific render dispatch out of the backend-neutral
 * {@code renderContent(...)} / {@code renderTopLayerContent(...)} methods.
 */
public final class GdxInventoryGuiRenderer {

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
    inventoryGUI.renderGdxMainLayer(renderContext.batch());
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
    inventoryGUI.renderGdxTopLayer(renderContext.batch());
  }
}
