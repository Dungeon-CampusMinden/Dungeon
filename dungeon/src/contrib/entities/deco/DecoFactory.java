package contrib.entities.deco;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.AnimationConfig;

/**
 * A factory class for creating decorative {@link Entity} instances.
 *
 * <p>Decorations (Decos) are static or animated entities placed in the game world, typically used
 * for visual details such as background objects, props, or scenery. This factory provides multiple
 * overloads to simplify creation with default or custom parameters.
 */
public class DecoFactory {

  /**
   * Creates a decorative entity with full control over all parameters.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing asset path and configuration
   * @param depth the rendering depth determining draw order
   * @param config the {@link AnimationConfig} used for this decoration
   * @param solidCollider the {@link Rectangle} defining the collider, or {@code null} if non-solid
   * @return a fully initialized decorative {@link Entity}
   */
  public static Entity createDeco(
      Point pos, Deco deco, int depth, AnimationConfig config, Rectangle solidCollider) {
    Entity entity = new Entity(deco.name());
    entity.add(new PositionComponent(pos));
    DrawComponent dc = new DrawComponent(deco.path(), config);
    dc.depth(depth);
    entity.add(dc);

    if (solidCollider != null) {
      CollideComponent cc = new CollideComponent(solidCollider);
      entity.add(cc);
    }

    return entity;
  }

  /**
   * Creates a decorative entity with a specified animation configuration. The decoration is placed
   * at the background depth layer and has no collider.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing asset path and configuration
   * @param config the {@link AnimationConfig} used for this decoration
   * @return a decorative {@link Entity} rendered in the background layer
   */
  public static Entity createDeco(Point pos, Deco deco, AnimationConfig config) {
    return createDeco(pos, deco, DepthLayer.BackgroundDeco.depth(), config, null);
  }

  /**
   * Creates a decorative entity with a collider but using the default animation configuration. The
   * decoration is placed at the background depth layer.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing asset path and configuration
   * @param solidCollider the {@link Rectangle} defining the collider
   * @return a decorative {@link Entity} with collision in the background layer
   */
  public static Entity createDeco(Point pos, Deco deco, Rectangle solidCollider) {
    return createDeco(pos, deco, DepthLayer.BackgroundDeco.depth(), deco.config(), solidCollider);
  }

  /**
   * Creates a decorative entity using all default settings from the {@link Deco} definition. This
   * includes the default depth, animation configuration, and collider.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing all defaults
   * @return a decorative {@link Entity} initialized with default values
   */
  public static Entity createDeco(Point pos, Deco deco) {
    return createDeco(pos, deco, deco.defaultDepth(), deco.config(), deco.defaultCollider());
  }

  /**
   * Creates a decorative entity with a specified rendering depth. Uses the default animation
   * configuration and no collider.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing asset path and configuration
   * @param depth the rendering depth determining draw order
   * @return a decorative {@link Entity} rendered at the specified depth
   */
  public static Entity createDeco(Point pos, Deco deco, int depth) {
    return createDeco(pos, deco, depth, deco.config(), null);
  }

  /**
   * Creates a decorative entity with a specified rendering depth and collider. Uses the default
   * animation configuration.
   *
   * @param pos the world position where the decoration should be placed
   * @param deco the {@link Deco} definition containing asset path and configuration
   * @param depth the rendering depth determining draw order
   * @param solidCollider the {@link Rectangle} defining the collider
   * @return a decorative {@link Entity} rendered at the specified depth with collision
   */
  public static Entity createDeco(Point pos, Deco deco, int depth, Rectangle solidCollider) {
    return createDeco(pos, deco, depth, deco.config(), solidCollider);
  }
}
