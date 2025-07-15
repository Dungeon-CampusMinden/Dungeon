package core.components;

import static org.junit.jupiter.api.Assertions.assertTrue;

import core.utils.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link PositionComponent} class. */
public class PositionComponentTest {

  private final Point position = new Point(3, 3);
  private PositionComponent positionComponent;

  /** WTF? . */
  @BeforeEach
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
