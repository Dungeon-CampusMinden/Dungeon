package contrib.hud.elements;

import java.util.Optional;

/**
 * Backend-neutral render context for combined HUD widgets.
 *
 * <p>Concrete backends can expose specialized render services by implementing this interface
 * and allowing widgets to unwrap them on demand.
 */
public interface GuiRenderContext {

  /**
   * Tries to unwrap this render context to a backend-specific type.
   *
   * @param type requested backend-specific type
   * @param <T> target type
   * @return matching backend object if available
   */
  default <T> Optional<T> unwrap(Class<T> type) {
    return Optional.empty();
  }

  /**
   * Gives the active backend a chance to execute the temporary legacy main-layer render path of
   * {@link CombinableGUI}.
   *
   * <p>Backends that do not support the legacy compatibility seam can keep the default no-op
   * implementation.
   *
   * @param gui gui element to render
   */
  default void renderLegacyContent(CombinableGUI gui) {
    // no-op by default
  }

  /**
   * Gives the active backend a chance to execute the temporary legacy top-layer render path of
   * {@link CombinableGUI}.
   *
   * <p>Backends that do not support the legacy compatibility seam can keep the default no-op
   * implementation.
   *
   * @param gui gui element to render
   */
  default void renderLegacyTopLayer(CombinableGUI gui) {
    // no-op by default
  }
}
