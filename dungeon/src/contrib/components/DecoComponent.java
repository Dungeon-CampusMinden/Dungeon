package contrib.components;

import contrib.entities.deco.Deco;
import core.Component;

/**
 * This component marks an entity as a decorative object. Used by the LevelEditor to detect deco
 * entities on click.
 *
 * @param type The type of the deco.
 */
public record DecoComponent(Deco type) implements Component {

  /**
   * Constructs a DecoComponent with the specified deco type.
   *
   * @param type The type of the deco.
   */
  public DecoComponent {}

  /**
   * Gets the type of the deco.
   *
   * @return The type of the deco.
   */
  @Override
  public Deco type() {
    return type;
  }
}
