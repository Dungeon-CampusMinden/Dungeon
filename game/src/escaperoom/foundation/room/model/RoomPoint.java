package escaperoom.foundation.room.model;

/**
 * One zero-based tile coordinate in a top-to-bottom room layout.
 *
 * @param x column from the left
 * @param y row from the top
 */
public record RoomPoint(int x, int y) {
  /** Creates a nonnegative room coordinate. */
  public RoomPoint {
    if (x < 0 || y < 0) {
      throw new IllegalArgumentException("room coordinates must be nonnegative");
    }
  }
}
