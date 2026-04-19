package contrib.hud.elements.bars;

/**
 * Exposes an {@link AttributeBarHandle} from a backend-specific UI node.
 */
public interface AttributeBarHandleProvider {

  /**
   * Returns the backend-agnostic attribute bar handle associated with the UI node.
   *
   * @return the attribute bar handle
   */
  AttributeBarHandle attributeBarHandle();
}
