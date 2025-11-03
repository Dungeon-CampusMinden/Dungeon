package contrib.components;

import contrib.entities.deco.Deco;
import core.Component;

/**
 * This component marks an entity as a decorative object. Used by the LevelEditor to detect deco
 * entities on click.
 */
public class DecoComponent implements Component {

  private final Deco type;

  /**
   * Constructs a DecoComponent with the specified deco type.
   *
   * @param type The type of the deco.
   */
  public DecoComponent(Deco type) {
    this.type = type;
  }

  /**
   * Gets the type of the deco.
   *
   * @return The type of the deco.
   */
  public Deco type() {
    return type;
  }
}
