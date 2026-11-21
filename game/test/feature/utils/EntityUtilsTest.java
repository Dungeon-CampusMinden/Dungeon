package feature.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import org.junit.jupiter.api.Test;

class EntityUtilsTest {

  @Test
  void aTallObjectIsHitOnItsWholePictureNotOnlyOnItsFootCollider() {
    // Locker.png is 26x74 pixels: one tile wide, almost three tiles high.
    Entity locker = new Entity();
    locker.add(new PositionComponent(new Point(0, 0)));
    locker.add(new DrawComponent(new SimpleIPath("objects/tech/Locker.png")));
    locker.add(new CollideComponent(Vector2.ZERO, Vector2.of(1, 0.5f)));

    assertTrue(EntityUtils.isPointOverEntity(locker, new Point(0.5f, 0.2f)));
    assertTrue(EntityUtils.isPointOverEntity(locker, new Point(0.5f, 2.6f)));
    assertFalse(EntityUtils.isPointOverEntity(locker, new Point(0.5f, 3.1f)));
    assertFalse(EntityUtils.isPointOverEntity(locker, new Point(1.2f, 0.2f)));
  }
}
