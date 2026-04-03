package contrib.hud.crafting;

/**
 * Factory for backend-specific crafting dialog body renderers.
 */
@FunctionalInterface
public interface CraftingDialogBodyRendererFactory {

  /**
   * Creates a renderer for the crafting dialog body.
   *
   * @return backend-specific renderer instance
   */
  CraftingDialogBodyRenderer create();
}
