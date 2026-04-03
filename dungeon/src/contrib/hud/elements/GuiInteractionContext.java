package contrib.hud.elements;

import java.util.Optional;

/**
 * Backend-neutral interaction context for combined HUD widgets.
 *
 * <p>Concrete backends can expose specialized interaction services by implementing this interface
 * and allowing widgets to unwrap them on demand.
 */
public interface GuiInteractionContext {

  /**
   * Tries to unwrap this interaction context to a backend-specific type.
   *
   * @param type requested backend-specific type
   * @param <T> target type
   * @return matching backend object if available
   */
  default <T> Optional<T> unwrap(Class<T> type) {
    return Optional.empty();
  }
}
