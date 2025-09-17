package entities;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory for creating tractor beam entities between two points.
 *
 * <p>The factory interpolates positions between a start point ({@code from}) and an end point
 * ({@code to}), creating a sequence of entities that visually represent a continuous tractor beam.
 * Each entity can apply a pulling force to other entities it collides with, based on the beam's
 * direction.
 */
public class TractorBeamFactory {

  private static final SimpleIPath TRACTOR_BEAM =
      new SimpleIPath("portal/portalTractorBeam_horizontal.png");

  private final Point from;
  private final Point to;
  private final int totalPoints;
  private int currentIndex = 0;
  private final Direction direction;

  /**
   * Creates a new {@code TractorBeamFactory} for generating tractor beam entities between the
   * specified points.
   *
   * @param from the starting point of the tractor beam
   * @param to the end point of the tractor beam
   */
  public TractorBeamFactory(Point from, Point to) {
    this.from = from;
    this.to = to;
    this.totalPoints = calculateNumberOfPoints(from, to);
    this.direction = calculateDirection(from, to);
  }

  /**
   * Calculates the number of points (entities) between {@code from} and {@code to}.
   *
   * @param from the starting point
   * @param to the end point
   * @return the number of interpolated points, including both start and end
   */
  private int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /**
   * Determines the main direction from {@code from} to {@code to}.
   *
   * @param from the starting point
   * @param to the end point
   * @return the primary direction (horizontal or vertical) toward {@code to}
   */
  private Direction calculateDirection(Point from, Point to) {
    float dx = to.x() - from.x();
    float dy = to.y() - from.y();

    if (Math.abs(dx) > Math.abs(dy)) {
      return dx > 0 ? Direction.RIGHT : Direction.LEFT;
    } else if (Math.abs(dy) > 0) {
      return dy > 0 ? Direction.UP : Direction.DOWN;
    } else {
      return Direction.NONE;
    }
  }

  /**
   * Checks whether more entities can be generated.
   *
   * @return {@code true} if additional entities are available, {@code false} otherwise
   */
  public boolean hasNext() {
    return currentIndex < totalPoints;
  }

  /**
   * Creates the next entity of the tractor beam.
   *
   * @return a new tractor beam entity, or {@code null} if no more entities can be created
   */
  public Entity createNextEntity() {
    if (!hasNext()) {
      return null;
    }

    // Interpolated position between from and to
    float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
    float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);

    Entity tractorBeam = new Entity("tractorBeam");
    tractorBeam.add(new PositionComponent(new Point(x, y)));
    tractorBeam.add(new DrawComponent(TRACTOR_BEAM));

    Direction beamDirection = calculateDirection(from, to);

    TriConsumer<Entity, Entity, Direction> action =
        (you, other, collisionDir) -> {
          other
              .fetch(VelocityComponent.class)
              .ifPresent(
                  vc -> {
                    float forceMagnitude = 20f;
                    Vector2 forceVector =
                        Vector2.of(
                            beamDirection.x() * forceMagnitude, beamDirection.y() * forceMagnitude);
                    vc.applyForce("tractorBeam", forceVector);
                  });
        };

    tractorBeam.add(new CollideComponent());
    tractorBeam
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.onHold(action);
            });

    currentIndex++;
    return tractorBeam;
  }

  /**
   * Creates a complete tractor beam between two points by generating all entities.
   *
   * @param from the starting point of the beam
   * @param to the end point of the beam
   * @return a list of all tractor beam entities
   */
  public static List<Entity> createFullTractorBeam(Point from, Point to) {
    TractorBeamFactory factory = new TractorBeamFactory(from, to);
    List<Entity> tractorBeamEntities = new ArrayList<>();

    while (factory.hasNext()) {
      tractorBeamEntities.add(factory.createNextEntity());
    }

    return tractorBeamEntities;
  }
}
