package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import java.util.stream.Stream;

/**
 * Exposes inventory components from a backend-specific inventory dialog root.
 */
public interface InventoryDialogProvider {

  /**
   * Returns all inventory components represented by this dialog root.
   *
   * @return stream of contained inventory components
   */
  Stream<InventoryComponent> inventoryComponents();
}
