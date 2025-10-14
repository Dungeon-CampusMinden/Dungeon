package contrib.utils.components.collide;

import core.utils.Point;
import core.utils.Vector2;

/** A circular collider used for collision detection based on a center position and radius. */
public class Hitcircle extends Collider {

  /** The radius of the hitcircle. */
  public float radius;

  /**
   * Creates a new {@code Hitcircle} with the given radius and offset position.
   *
   * @param radius The radius of the circle.
   * @param x The x-offset of the circle's center.
   * @param y The y-offset of the circle's center.
   */
  public Hitcircle(float radius, float x, float y) {
    this.radius = radius;
    this.offset = Vector2.of(x, y);
  }

  /**
   * Creates a new {@code Hitcircle} with the given radius and zero offset.
   *
   * @param radius The radius of the circle.
   */
  public Hitcircle(float radius) {
    this(radius, 0, 0);
  }

  /**
   * Returns the radius of the circle.
   *
   * @return The circle's radius.
   */
  public float radius() {
    return this.radius;
  }

  /**
   * Returns the width of the circle (diameter).
   *
   * @return The width of the circle.
   */
  @Override
  public float width() {
    return this.radius * 2f;
  }

  /**
   * Sets the width of the circle (diameter), adjusting the radius accordingly.
   *
   * @param value The new width of the circle.
   */
  @Override
  public void width(float value) {
    this.radius = value / 2f;
  }

  /**
   * Returns the height of the circle (diameter).
   *
   * @return The height of the circle.
   */
  @Override
  public float height() {
    return this.radius * 2f;
  }

  /**
   * Sets the height of the circle (diameter), adjusting the radius accordingly.
   *
   * @param value The new height of the circle.
   */
  @Override
  public void height(float value) {
    this.radius = value / 2f;
  }

  /**
   * Returns the x-coordinate of the left edge of the circle.
   *
   * @return The x-coordinate of the left edge.
   */
  @Override
  public float left() {
    return this.offset.x() - this.radius;
  }

  /**
   * Sets the x-coordinate of the left edge of the circle.
   *
   * @param x The new x-coordinate for the left edge.
   */
  @Override
  public void left(float x) {
    this.offset = Vector2.of(x + this.radius, this.offset.y());
  }

  /**
   * Returns the y-coordinate of the top edge of the circle.
   *
   * @return The y-coordinate of the top edge.
   */
  @Override
  public float top() {
    return this.offset.y() + this.radius;
  }

  /**
   * Sets the y-coordinate of the top edge of the circle.
   *
   * @param y The new y-coordinate for the top edge.
   */
  @Override
  public void top(float y) {
    this.offset = Vector2.of(this.offset.x(), y - this.radius);
  }

  /**
   * Returns the x-coordinate of the right edge of the circle.
   *
   * @return The x-coordinate of the right edge.
   */
  @Override
  public float right() {
    return this.offset.x() + this.radius;
  }

  /**
   * Sets the x-coordinate of the right edge of the circle.
   *
   * @param x The new x-coordinate for the right edge.
   */
  @Override
  public void right(float x) {
    this.offset = Vector2.of(x - this.radius, this.offset.y());
  }

  /**
   * Returns the y-coordinate of the bottom edge of the circle.
   *
   * @return The y-coordinate of the bottom edge.
   */
  @Override
  public float bottom() {
    return this.offset.y() - this.radius;
  }

  /**
   * Sets the y-coordinate of the bottom edge of the circle.
   *
   * @param y The new y-coordinate for the bottom edge.
   */
  @Override
  public void bottom(float y) {
    this.offset = Vector2.of(this.offset.x(), y + this.radius);
  }

  /**
   * Checks if this hitcircle collides with a given point.
   *
   * @param point The point to check for collision.
   * @return {@code true} if the circle collides with the point, {@code false} otherwise.
   */
  @Override
  public boolean collide(Point point) {
    return CollisionUtils.circleCollidesPoint(this.absolutePosition(), this.radius, point);
  }

  /**
   * Checks if this hitcircle collides with a line defined by two points.
   *
   * @param from The start point of the line.
   * @param to The end point of the line.
   * @return {@code true} if the circle collides with the line, {@code false} otherwise.
   */
  @Override
  public boolean collide(Point from, Point to) {
    return CollisionUtils.circleCollidesLine(this.absolutePosition(), this.radius, from, to);
  }

  /**
   * Checks if this hitcircle collides with another hitcircle.
   *
   * @param hitcircle The other hitcircle to check collision with.
   * @return {@code true} if the circles collide, {@code false} otherwise.
   */
  @Override
  public boolean collide(Hitcircle hitcircle) {
    double d = this.absolutePosition().distance(hitcircle.absolutePosition());
    return Math.pow(d, 2)
        < ((double) this.radius + (double) hitcircle.radius)
            * ((double) this.radius + (double) hitcircle.radius);
  }

  /**
   * Checks if this hitcircle collides with a hitbox.
   *
   * @param hitbox The hitbox to check collision with.
   * @return {@code true} if the circle collides with the hitbox, {@code false} otherwise.
   */
  @Override
  public boolean collide(Hitbox hitbox) {
    return hitbox.collide(this);
  }
}
