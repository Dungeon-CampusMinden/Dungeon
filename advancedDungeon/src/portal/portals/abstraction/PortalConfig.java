package portal.portals.abstraction;

import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;
import portal.abstraction.Hero;

public abstract class PortalConfig {
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.5, 0.5);
  private static final Vector2 HIT_BOX_OFFSET = Vector2.of(0.25, 0.25);

  /**
   * Constructor.
   *
   * @param hero The {@link Hero} instance this controller will manipulate.
   */
  public PortalConfig(Hero hero) {}

  public abstract long cooldown();

  public abstract float speed();

  public abstract float range();

  public final Vector2 hitBoxSize() {
    return HIT_BOX_SIZE;
  }

  public final Vector2 hitBoxOffset() {
    return HIT_BOX_OFFSET;
  }

  public abstract Supplier<Point> target();
}
