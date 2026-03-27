package contrib.hud.elements;

import contrib.components.InventoryComponent;
import contrib.hud.IInventoryHolder;
import java.util.stream.Stream;

/**
 * Scene2D GUI combination that exposes contained inventory components.
 */
public final class InventoryGuiGroup extends GUICombination
  implements InventoryComponentProvider {

  /**
   * Creates a new inventory-capable GUI root.
   *
   * @param combinableGuis the contained GUI elements
   */
  public InventoryGuiGroup(final CombinableGUI... combinableGuis) {
    super(combinableGuis);
  }

  /**
   * Creates a new inventory-capable GUI root with an explicit row count.
   *
   * @param guisPerRow number of GUIs per row
   * @param combinableGuis the contained GUI elements
   */
  public InventoryGuiGroup(int guisPerRow, final CombinableGUI... combinableGuis) {
    super(guisPerRow, combinableGuis);
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return combinableGuiStream()
      .filter(IInventoryHolder.class::isInstance)
      .map(IInventoryHolder.class::cast)
      .map(IInventoryHolder::inventoryComponent);
  }
}
