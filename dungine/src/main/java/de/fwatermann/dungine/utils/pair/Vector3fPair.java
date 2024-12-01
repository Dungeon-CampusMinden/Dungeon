package de.fwatermann.dungine.utils.pair;

import org.joml.Vector3f;

/**
 * A record that represents a pair of Vector3f objects. The Vector3fPair class provides methods to
 * create a pair of Vector3f objects and to retrieve them as a Pair.
 *
 * @param a the first Vector3f object
 * @param b the second Vector3f object
 */
public record Vector3fPair(Vector3f a, Vector3f b) {

  /**
   * Creates a new Vector3fPair with the specified Vector3f objects.
   *
   * @param a the first Vector3f object
   * @param b the second Vector3f object
   * @return a new Vector3fPair instance
   */
  public static Vector3fPair of(Vector3f a, Vector3f b) {
    return new Vector3fPair(a, b);
  }

  /**
   * Returns the pair of Vector3f objects as a Pair.
   *
   * @return a Pair containing the two Vector3f objects
   */
  public Pair<Vector3f, Vector3f> getPair() {
    return new Pair<Vector3f, Vector3f>(this.a, this.b);
  }
}
