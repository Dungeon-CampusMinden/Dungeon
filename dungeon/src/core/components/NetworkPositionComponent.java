package core.components;

import core.Component;
import core.network.DefaultSnapshotTranslator;
import core.systems.NetworkPositionSmoothingSystem;
import core.utils.Point;

/**
 * Stores the latest authoritative network position for client-side visual smoothing.
 *
 * <p>The {@link DefaultSnapshotTranslator} updates this target from incoming snapshots, while
 * {@link NetworkPositionSmoothingSystem} moves the visible {@link PositionComponent} toward it.
 */
public final class NetworkPositionComponent implements Component {

  private Point targetPosition;

  /**
   * Creates a new network position target.
   *
   * @param targetPosition the latest authoritative position received from the server
   */
  public NetworkPositionComponent(Point targetPosition) {
    this.targetPosition = new Point(targetPosition);
  }

  /**
   * Returns the latest authoritative target position.
   *
   * @return a copy of the target position
   */
  public Point targetPosition() {
    return new Point(targetPosition);
  }

  /**
   * Updates the authoritative target position.
   *
   * @param targetPosition the new target position
   */
  public void targetPosition(Point targetPosition) {
    this.targetPosition = new Point(targetPosition);
  }
}
