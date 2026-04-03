package contrib.hud.crafting;

import contrib.hud.elements.CombinableGUI;
import contrib.item.Item;
import java.util.function.Supplier;

/**
 * Factory for backend-specific crafting action bars.
 *
 * <p>The dialog requests an action bar through this small abstraction so that it no longer needs
 * to know the concrete backend implementation type.
 */
@FunctionalInterface
public interface CraftingActionBarFactory {

  /**
   * Creates an action bar for the given crafting dialog.
   *
   * @param parent the owning GUI
   * @param dialogId dialog id for callback resolution
   * @param craftingPayloadSupplier supplier for the current crafting payload
   * @return backend-specific action bar implementation
   */
  CraftingActionBar create(
    CombinableGUI parent, String dialogId, Supplier<Item[]> craftingPayloadSupplier);
}
