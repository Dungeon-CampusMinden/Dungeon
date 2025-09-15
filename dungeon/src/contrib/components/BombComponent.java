package contrib.components;

import core.Component;

public class BombComponent implements Component {

  private final float radius;
  private final int damage;
  private final long fuseMs;

  public BombComponent(float radius, int damage, long fuseMs) {
    this.radius = radius;
    this.damage = damage;
    this.fuseMs = fuseMs;
  }

  public float radius() {
    return radius;
  }

  public int damage() {
    return damage;
  }

  public long fuseMs() {
    return fuseMs;
  }
}
