
```java

public class MyCubeSpawner extends CubeSpawner {

  private Entity spawned;

  @Override
  public void spawn() {
    // Update values on level case
    spawned = spawnCube(new Point(2, 2), 100f);
    Game.add(spawned);
  }

  private Entity spawnCube(Point p, float mass) {
    return Cube.portalCube(p, mass);
  }

  private Entity spawnSphere(Point p, float mass) {
    return Sphere.portalSphere(p, mass);
  }
}
```
