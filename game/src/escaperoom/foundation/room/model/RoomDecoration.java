package escaperoom.foundation.room.model;

import feature.entities.deco.Deco;
import java.util.Objects;

/**
 * Static decoration anchored to one layout tile.
 *
 * @param deco decoration type
 * @param tile anchoring tile in top-to-bottom layout coordinates
 * @param offsetX horizontal offset within the tile in {@code [0, 1)}
 * @param offsetY upward offset within the tile in {@code [0, 1)}
 */
public record RoomDecoration(Deco deco, RoomPoint tile, float offsetX, float offsetY) {
  /** Creates one decoration. */
  public RoomDecoration {
    Objects.requireNonNull(deco, "deco");
    Objects.requireNonNull(tile, "tile");
    if (!(offsetX >= 0 && offsetX < 1 && offsetY >= 0 && offsetY < 1)) {
      throw new IllegalArgumentException("decoration offsets must lie within their tile");
    }
  }
}
