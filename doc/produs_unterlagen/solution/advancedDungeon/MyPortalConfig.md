
```java
  @Override
  public long cooldown() {
    return 500;
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
```
