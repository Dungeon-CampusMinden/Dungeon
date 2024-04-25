package core.components;

import static junit.framework.TestCase.assertTrue;

import core.utils.Point;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link PositionComponent} class. */
public class PositionComponentTest {

  private final Point position = new Point(3, 3);
  private PositionComponent positionComponent;

  /** WTF? . */
  @Before
  public void setup() {
    positionComponent = new PositionComponent(position);
  }

  /** WTF? . */
  @Test
  public void setPosition() {
    assertTrue(position.equals(positionComponent.position()));
    Point newPoint = new Point(3, 4);
    positionComponent.position(newPoint);
    assertTrue(newPoint.equals(positionComponent.position()));
  }
}
