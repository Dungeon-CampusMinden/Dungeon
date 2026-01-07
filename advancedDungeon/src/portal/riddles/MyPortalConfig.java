package portal.riddles;

import core.utils.Point;
import java.util.function.Supplier;
import portal.abstraction.Hero;
import portal.portals.abstraction.PortalConfig;

public class MyPortalConfig extends PortalConfig {
  private Hero hero;

  public MyPortalConfig(Hero hero) {
    super(hero);
    this.hero = hero;
  }

  @Override
  public long cooldown() {
    return Integer.MAX_VALUE;
  }

  @Override
  public float speed() {
    return 0;
  }

  @Override
  public float range() {
    return 0;
  }

  @Override
  public Supplier<Point> target() {
    return () -> new Point(0, 0);
  }
}
