package de.fwatermann.dungine.utils;

/**
 * The BoundingBox2D class represents a 2D bounding box defined by its minimum and maximum
 * coordinates. It provides methods to get and set the coordinates, as well as to calculate the
 * width and height of the bounding box.
 */
public class BoundingBox2D {

  private int minX, minY;
  private int maxX, maxY;

  /**
   * Constructs a BoundingBox2D with the specified minimum and maximum coordinates.
   *
   * @param minX the minimum x-coordinate
   * @param minY the minimum y-coordinate
   * @param maxX the maximum x-coordinate
   * @param maxY the maximum y-coordinate
   */
  public BoundingBox2D(int minX, int minY, int maxX, int maxY) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  /**
   * Gets the minimum x-coordinate of the bounding box.
   *
   * @return the minimum x-coordinate
   */
  public int minX() {
    return this.minX;
  }

  /**
   * Sets the minimum x-coordinate of the bounding box.
   *
   * @param minX the new minimum x-coordinate
   * @return this BoundingBox2D instance for method chaining
   */
  public BoundingBox2D minX(int minX) {
    this.minX = minX;
    return this;
  }

  /**
   * Gets the minimum y-coordinate of the bounding box.
   *
   * @return the minimum y-coordinate
   */
  public int minY() {
    return this.minY;
  }

  /**
   * Sets the minimum y-coordinate of the bounding box.
   *
   * @param minY the new minimum y-coordinate
   * @return this BoundingBox2D instance for method chaining
   */
  public BoundingBox2D minY(int minY) {
    this.minY = minY;
    return this;
  }

  /**
   * Gets the maximum x-coordinate of the bounding box.
   *
   * @return the maximum x-coordinate
   */
  public int maxX() {
    return this.maxX;
  }

  /**
   * Sets the maximum x-coordinate of the bounding box.
   *
   * @param maxX the new maximum x-coordinate
   * @return this BoundingBox2D instance for method chaining
   */
  public BoundingBox2D maxX(int maxX) {
    this.maxX = maxX;
    return this;
  }

  /**
   * Gets the maximum y-coordinate of the bounding box.
   *
   * @return the maximum y-coordinate
   */
  public int maxY() {
    return this.maxY;
  }

  /**
   * Sets the maximum y-coordinate of the bounding box.
   *
   * @param maxY the new maximum y-coordinate
   * @return this BoundingBox2D instance for method chaining
   */
  public BoundingBox2D maxY(int maxY) {
    this.maxY = maxY;
    return this;
  }

  /**
   * Calculates the width of the bounding box.
   *
   * @return the width of the bounding box
   */
  public int width() {
    return this.maxX - this.minX;
  }

  /**
   * Calculates the height of the bounding box.
   *
   * @return the height of the bounding box
   */
  public int height() {
    return this.maxY - this.minY;
  }
}
