package contrib.entities.deco;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.AnimationConfig;

public class DecoFactory {

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

  public static Entity createDeco(Point pos, Deco deco, AnimationConfig config) {
    return createDeco(pos, deco, DepthLayer.BackgroundDeco.depth(), config, null);
  }

  public static Entity createDeco(Point pos, Deco deco, Rectangle solidCollider) {
    return createDeco(pos, deco, DepthLayer.BackgroundDeco.depth(), deco.config(), solidCollider);
  }

  public static Entity createDeco(Point pos, Deco deco) {
    return createDeco(pos, deco, deco.defaultDepth(), deco.config(), deco.defaultCollider());
  }

  public static Entity createDeco(Point pos, Deco deco, int depth) {
    return createDeco(pos, deco, depth, deco.config(), null);
  }

  public static Entity createDeco(Point pos, Deco deco, int depth, Rectangle solidCollider) {
    return createDeco(pos, deco, depth, deco.config(), solidCollider);
  }
}
