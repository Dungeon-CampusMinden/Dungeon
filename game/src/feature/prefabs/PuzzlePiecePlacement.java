package feature.prefabs;

import engine.utils.Point;
import feature.entities.deco.Deco;
import java.util.Objects;

/**
 * World placement and representation for one image-puzzle fragment.
 *
 * @param position world spawn position
 * @param destination world item, chest, or deco inventory
 * @param deco decoration used when the destination is a deco inventory
 */
public record PuzzlePiecePlacement(Point position, Destination destination, Deco deco) {

  /** How the piece is placed into the world. */
  public enum Destination {
    /** The piece is placed directly on the ground as a world item. */
    WORLD_ITEM("world-item"),
    /** The piece is placed inside a chest. */
    CHEST("chest"),
    /** The piece is put into an inventory attached to a decoration. */
    DECO_INVENTORY("deco-inventory");

    private final String id;

    Destination(String id) {
      this.id = id;
    }

    /**
     * Returns the stable serialized ID.
     *
     * @return destination ID
     */
    public String id() {
      return id;
    }

    /**
     * Resolves a serialized destination ID.
     *
     * @param id serialized destination ID
     * @return matching destination
     */
    public static Destination fromId(String id) {
      if ("container".equals(id)) return DECO_INVENTORY;
      for (Destination destination : values()) {
        if (destination.id.equals(id)) return destination;
      }
      throw new IllegalArgumentException("Unknown puzzle piece destination: '" + id + "'");
    }
  }

  /** Validates the placement. */
  public PuzzlePiecePlacement {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(destination, "destination");
    Objects.requireNonNull(deco, "deco");
    if (!Float.isFinite(position.x()) || !Float.isFinite(position.y())) {
      throw new IllegalArgumentException("puzzle piece position must be finite");
    }
  }

  /**
   * Creates a placement using the default deco inventory decoration.
   *
   * @param position world spawn position
   * @param destination world destination
   */
  public PuzzlePiecePlacement(Point position, Destination destination) {
    this(position, destination, Deco.BookshelfLarge);
  }

  /** Creates a world-item placement at the given point. */
  public static PuzzlePiecePlacement worldItem(Point position) {
    return new PuzzlePiecePlacement(position, Destination.WORLD_ITEM, Deco.BookshelfLarge);
  }

  /** Creates the default placement for a piece index. */
  public static PuzzlePiecePlacement defaultFor(int pieceIndex) {
    if (pieceIndex < 0) throw new IllegalArgumentException("pieceIndex must not be negative");
    int column = pieceIndex % 4;
    int row = pieceIndex / 4;
    return worldItem(new Point(column * 1.5f, row * 1.5f));
  }

  /** Returns this placement at another world position. */
  public PuzzlePiecePlacement withPosition(Point newPosition) {
    return new PuzzlePiecePlacement(newPosition, destination, deco);
  }

  /** Returns this placement with another destination. */
  public PuzzlePiecePlacement withDestination(Destination newDestination) {
    return new PuzzlePiecePlacement(position, newDestination, deco);
  }

  /** Returns this placement with another deco for its inventory destination. */
  public PuzzlePiecePlacement withDeco(Deco newDeco) {
    return new PuzzlePiecePlacement(position, destination, newDeco);
  }
}
