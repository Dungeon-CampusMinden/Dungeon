package feature.entities.deco;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import feature.components.CollideComponent;
import feature.components.DecoComponent;

/**
 * A factory class for creating {@link Entity} instances from a {@link Deco} definition.
 *
 * <p>A prop has the position, sprite, and collider of a {@link Deco} and belongs to the code that
 * creates it, such as a prefab or a room. A decoration is a prop that the level owns: it
 * additionally carries a {@link DecoComponent}, so the level editor can select, move, and save it.
 */
public class DecoFactory {

  /**
   * Creates a level-owned decoration with the defaults of the {@link Deco} definition.
   *
   * <p>Used for decorations loaded from a level and placed in the level editor.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing all defaults
   * @return a decorative {@link Entity} marked with a {@link DecoComponent}
   */
  public static Entity createDeco(Point pos, Deco deco) {
    Entity entity = createProp(pos, deco);
    entity.add(new DecoComponent(deco));
    return entity;
  }

  /**
   * Creates a prop with the defaults of the {@link Deco} definition: depth, animation
   * configuration, and collider.
   *
   * @param pos the world position where the prop should be placed
   * @param deco the {@link Deco} definition containing all defaults
   * @return a new {@link Entity} with position, sprite, and optional collider
   */
  public static Entity createProp(Point pos, Deco deco) {
    return applyProp(new Entity(deco.name()), pos, deco);
  }

  /**
   * Creates a prop with full control over all parameters.
   *
   * @param pos the world position where the prop should be placed
   * @param deco the {@link Deco} definition containing the asset path
   * @param depth the rendering depth determining draw order
   * @param config the {@link AnimationConfig} used for this prop
   * @param solidCollider the {@link Rectangle} defining the collider, or {@code null} if non-solid
   * @return a new {@link Entity} with position, sprite, and optional collider
   */
  public static Entity createProp(
      Point pos, Deco deco, int depth, AnimationConfig config, Rectangle solidCollider) {
    return applyProp(new Entity(deco.name()), pos, deco, depth, config, solidCollider);
  }

  /**
   * Adds the position, sprite, and collider of a prop to a supplied entity, using the defaults of
   * the {@link Deco} definition.
   *
   * <p>This allows side-aware callers such as prefabs to provide a local-only entity.
   *
   * @param entity entity to configure
   * @param pos world position
   * @param deco decoration definition
   * @return the supplied configured entity
   */
  public static Entity applyProp(Entity entity, Point pos, Deco deco) {
    return applyProp(entity, pos, deco, deco.defaultDepth(), deco.config(), deco.defaultCollider());
  }

  private static Entity applyProp(
      Entity entity,
      Point pos,
      Deco deco,
      int depth,
      AnimationConfig config,
      Rectangle solidCollider) {
    entity.add(new PositionComponent(pos));
    // Explicit sprite regions and scales take precedence over an asset's animation JSON.
    DrawComponent dc =
        config.config().isPresent()
            ? new DrawComponent(new Animation(deco.path(), config))
            : new DrawComponent(deco.path(), config);
    dc.depth(depth);
    entity.add(dc);

    if (solidCollider != null) {
      entity.add(new CollideComponent(solidCollider));
    }
    return entity;
  }
}
