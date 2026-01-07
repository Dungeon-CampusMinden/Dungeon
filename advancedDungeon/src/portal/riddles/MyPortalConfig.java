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
    return 10;
  }

  @Override
  public float speed() {
    return 10;
  }

  @Override
  public float range() {
    return Integer.MAX_VALUE;
  }

  @Override
  public Supplier<Point> target() {
    return () -> hero.getMousePosition();
  }
}
