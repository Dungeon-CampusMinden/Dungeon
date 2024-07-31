package level.utils;

import core.level.utils.Coordinate;

/**
 * The Teleporter class represents a teleporter in the game.
 *
 * <p>A teleporter is an object that can transport the player from one location to another. Each
 * teleporter has a 'from' and 'to' location, represented by Coordinate objects. A teleporter can be
 * one-way or two-way, as indicated by the 'isOneWay' boolean.
 */
public class Teleporter {

  /** The location from which the teleporter transports the player. */
  private final Coordinate from;

  /** The location to which the teleporter transports the player. */
  private final Coordinate to;

  /** Indicates whether the teleporter is one-way (true) or two-way (false). */
  private final boolean isOneWay;

  /**
   * Constructs a new Teleporter with the specified 'from' and 'to' locations and 'isOneWay' status.
   *
   * @param from The location from which the teleporter transports the player.
   * @param to The location to which the teleporter transports the player.
   * @param isOneWay Indicates whether the teleporter is one-way (true) or two-way (false).
   */
  public Teleporter(Coordinate from, Coordinate to, boolean isOneWay) {
    this.from = from;
    this.to = to;
    this.isOneWay = isOneWay;
  }

  /**
   * Constructs a new two-way Teleporter with the specified 'from' and 'to' locations.
   *
   * @param from The location from which the teleporter transports the player.
   * @param to The location to which the teleporter transports the player.
   */
  public Teleporter(Coordinate from, Coordinate to) {
    this(from, to, false);
  }

  /**
   * Returns the 'from' location of the teleporter.
   *
   * @return The 'from' location of the teleporter.
   */
  public Coordinate from() {
    return from;
  }

  /**
   * Returns the 'to' location of the teleporter.
   *
   * @return The 'to' location of the teleporter.
   */
  public Coordinate to() {
    return to;
  }

  /**
   * Returns whether the teleporter is one-way.
   *
   * @return True if the teleporter is one-way, false otherwise.
   */
  public boolean isOneWay() {
    return isOneWay;
  }

  /**
   * Returns the current destination of the teleporter based on the given current location.
   *
   * <p>If the current location is the 'from' location, the 'to' location is returned. If the
   * current location is the 'to' location and the teleporter is not one-way, the 'from' location is
   * returned. If neither of these conditions are met, null is returned.
   *
   * @param currentPos The current location.
   * @return The current destination of the teleporter, or null if there is no valid destination.
   */
  public Coordinate getCurrentDestination(Coordinate currentPos) {
    if (currentPos.equals(from)) {
      return to;
    } else if (currentPos.equals(to) && !isOneWay) {
      return from;
    }
    return null;
  }

  /**
   * Determines whether the specified object is equal to this Teleporter.
   *
   * <p>An object is equal to this Teleporter if it is also a Teleporter and its 'from' and 'to'
   * locations and 'isOneWay' status are equal to those of this Teleporter.
   *
   * @param obj The object to compare with this Teleporter.
   * @return True if the specified object is equal to this Teleporter, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Teleporter other)) {
      return false;
    }
    return from.equals(other.from) && to.equals(other.to) && isOneWay == other.isOneWay;
  }
}
