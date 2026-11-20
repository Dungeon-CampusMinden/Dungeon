package feature.components;

import engine.Component;
import feature.entities.deco.Deco;

/**
 * This component marks an entity as a decoration owned by the level. The level editor selects,
 * moves, and saves exactly the entities that carry it; {@link
 * feature.entities.deco.DecoFactory#createDeco} adds it.
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
