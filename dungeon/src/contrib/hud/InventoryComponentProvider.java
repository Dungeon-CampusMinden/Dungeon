package contrib.hud;

import contrib.components.InventoryComponent;
import java.util.stream.Stream;

/**
 * Exposes inventory components from a backend-specific inventory dialog root.
 */
public interface InventoryComponentProvider {

  /**
   * Returns all inventory components represented by this dialog root.
   *
   * @return stream of contained inventory components
   */
  Stream<InventoryComponent> inventoryComponents();
}
