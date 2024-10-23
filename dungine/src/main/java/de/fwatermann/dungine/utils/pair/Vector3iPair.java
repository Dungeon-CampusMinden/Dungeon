package de.fwatermann.dungine.utils.pair;

import org.joml.Vector3i;

/**
 * A record that represents a pair of Vector3i objects. The Vector3iPair class provides methods to
 * create a pair of Vector3i objects and to retrieve them as a Pair.
 *
 * @param a the first Vector3i object
 * @param b the second Vector3i object
 */
public record Vector3iPair(Vector3i a, Vector3i b) {

  /**
   * Creates a new Vector3iPair with the specified Vector3i objects.
   *
   * @param a the first Vector3i object
   * @param b the second Vector3i object
   * @return a new Vector3iPair instance
   */
  public static Vector3iPair of(Vector3i a, Vector3i b) {
    return new Vector3iPair(a, b);
  }

  /**
   * Returns the pair of Vector3i objects as a Pair.
   *
   * @return a Pair containing the two Vector3i objects
   */
  public Pair<Vector3i, Vector3i> getPair() {
    return new Pair<Vector3i, Vector3i>(this.a, this.b);
  }
}
