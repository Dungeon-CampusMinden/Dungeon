package feature.prefabs;

import engine.Entity;
import engine.level.elements.ILevel;
import java.util.Objects;

/**
 * Runtime creation context supplied to prefab definitions.
 *
 * @param level level that owns the prefab instance
 * @param side side currently being instantiated
 */
public record PrefabCreationContext(ILevel level, PrefabSide side) {

  /** Validates context members. */
  public PrefabCreationContext {
    Objects.requireNonNull(level, "level");
    Objects.requireNonNull(side, "side");
  }

  /**
   * Creates an entity appropriate for this context.
   *
   * @param name entity name
   * @return normal server entity or negative-ID local entity
   */
  public Entity createEntity(String name) {
    return side == PrefabSide.CLIENT ? Entity.createLocalEntity(name) : new Entity(name);
  }
}
