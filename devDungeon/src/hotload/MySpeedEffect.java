package hotload;

import core.Entity;
import core.components.VelocityComponent;
import item.effects.SpeedEffect;
import java.util.function.Supplier;

public class MySpeedEffect implements Supplier<SpeedEffect> {
  public static String debug = "12";

  public MySpeedEffect() {
    System.out.println("WITHOUT ARGS");
  }

  public MySpeedEffect(String debugValue, int number) {
    System.out.println("With args " + debugValue + " " + number);
  }

  @Override
  public SpeedEffect get() {
    return new MyEffect();
  }

  class MyEffect extends SpeedEffect {
    MyEffect() {
      super(2, 2);
    }

    @Override
    public void applySpeedEffect(Entity target) {
      System.out.println("MY SUPP " + debug);
      target.fetch(VelocityComponent.class).get().xVelocity(150);
    }
  }
}
