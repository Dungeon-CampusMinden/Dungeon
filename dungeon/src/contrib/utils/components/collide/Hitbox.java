package contrib.utils.components.collide;

import core.utils.Point;
import core.utils.Vector2;

/** A rectangular collider used for collision detection based on width, height, and offset. */
public class Hitbox extends Collider {

  /** The width of the hitbox. */
  private float width;

  /** The height of the hitbox. */
  private float height;

  /**
   * Creates a new {@code Hitbox} with the given size and offset.
   *
   * @param size The size of the hitbox as a {@link Vector2}.
   * @param offset The offset of the hitbox as a {@link Vector2}.
   */
  public Hitbox(Vector2 size, Vector2 offset) {
    this(size.x(), size.y(), offset.x(), offset.y());
  }

  /**
   * Creates a new {@code Hitbox} with the given width and height and zero offset.
   *
   * @param width The width of the hitbox.
   * @param height The height of the hitbox.
   */
  public Hitbox(float width, float height) {
    this(width, height, 0.0f, 0.0f);
  }

  /**
   * Creates a new {@code Hitbox} with the given width, height, and offset.
   *
   * @param width The width of the hitbox.
   * @param height The height of the hitbox.
   * @param x The x-offset of the hitbox.
   * @param y The y-offset of the hitbox.
   */
  public Hitbox(float width, float height, float x, float y) {
    this.width = width;
    this.height = height;
    this.offset = Vector2.of(x, y);
  }

  /**
   * Returns the width of the hitbox.
   *
   * @return The width of the hitbox.
   */
  @Override
  public float width() {
    return this.width;
  }

  /**
   * Sets the width of the hitbox.
   *
   * @param width The new width of the hitbox.
   */
  @Override
  public void width(float width) {
    this.width = width;
  }

  /**
   * Returns the height of the hitbox.
   *
   * @return The height of the hitbox.
   */
  @Override
  public float height() {
    return this.height;
  }

  /**
   * Sets the height of the hitbox.
   *
   * @param height The new height of the hitbox.
   */
  @Override
  public void height(float height) {
    this.height = height;
  }

  /**
   * Returns the x-coordinate of the left edge of the hitbox.
   *
   * @return The x-coordinate of the left edge.
   */
  @Override
  public float left() {
    return this.offset.x();
  }

  /**
   * Sets the x-coordinate of the left edge of the hitbox.
   *
   * @param x The new x-coordinate for the left edge.
   */
  public void left(float x) {
    this.offset = Vector2.of(x, this.offset.y());
  }

  /**
   * Returns the y-coordinate of the top edge of the hitbox.
   *
   * @return The y-coordinate of the top edge.
   */
  public float top() {
    return this.offset.y() + this.height;
  }

  /**
   * Sets the y-coordinate of the top edge of the hitbox.
   *
   * @param y The new y-coordinate for the top edge.
   */
  public void top(float y) {
    this.offset = Vector2.of(this.offset.x(), y - this.height);
  }

  /**
   * Returns the x-coordinate of the right edge of the hitbox.
   *
   * @return The x-coordinate of the right edge.
   */
  public float right() {
    return this.offset.x() + this.width;
  }

  /**
   * Sets the x-coordinate of the right edge of the hitbox.
   *
   * @param x The new x-coordinate for the right edge.
   */
  public void right(float x) {
    this.offset = Vector2.of(x - this.width, this.offset.y());
  }

  /**
   * Returns the y-coordinate of the bottom edge of the hitbox.
   *
   * @return The y-coordinate of the bottom edge.
   */
  public float bottom() {
    return this.offset.y();
  }

  /**
   * Sets the y-coordinate of the bottom edge of the hitbox.
   *
   * @param y The new y-coordinate for the bottom edge.
   */
  public void bottom(float y) {
    this.offset = Vector2.of(this.offset.x(), y);
  }

  /**
   * Checks whether this hitbox intersects another hitbox.
   *
   * @param hitbox The other hitbox to check intersection with.
   * @return {@code true} if the hitboxes intersect, {@code false} otherwise.
   */
  public boolean intersects(Hitbox hitbox) {
    return this.absoluteLeft() < hitbox.absoluteRight()
        && this.absoluteRight() > hitbox.absoluteLeft()
        && this.absoluteBottom() < hitbox.absoluteTop()
        && this.absoluteTop() > hitbox.absoluteBottom();
  }

  /**
   * Checks whether this hitbox intersects with a rectangle defined by position and size.
   *
   * @param x The x-coordinate of the rectangle.
   * @param y The y-coordinate of the rectangle.
   * @param width The width of the rectangle.
   * @param height The height of the rectangle.
   * @return {@code true} if the hitbox intersects the rectangle, {@code false} otherwise.
   */
  public boolean intersects(float x, float y, float width, float height) {
    return this.absoluteRight() > x
        && this.absoluteLeft() < x + width
        && this.absoluteBottom() < y + height
        && this.absoluteTop() > y;
  }

  /**
   * Creates a copy of this hitbox with the same size and offset.
   *
   * @return A cloned {@code Hitbox} instance.
   */
  @Override
  public Collider clone() {
    return new Hitbox(this.width, this.height, this.offset.x(), this.offset.y());
  }

  /**
   * Checks if this hitbox collides with a given point.
   *
   * @param point The point to check for collision.
   * @return {@code true} if the hitbox collides with the point, {@code false} otherwise.
   */
  @Override
  public boolean collide(Point point) {
    return CollisionUtils.rectCollidesPoint(
        this.absoluteLeft(), this.absoluteTop(), this.width, this.height, point);
  }

  /**
   * Checks if this hitbox collides with a line defined by two points.
   *
   * @param from The start point of the line.
   * @param to The end point of the line.
   * @return {@code true} if the hitbox collides with the line, {@code false} otherwise.
   */
  @Override
  public boolean collide(Point from, Point to) {
    return CollisionUtils.rectCollidesLine(
        this.absoluteLeft(), this.absoluteTop(), this.width, this.height, from, to);
  }

  /**
   * Checks if this hitbox collides with another hitbox.
   *
   * @param hitbox The hitbox to check for collision.
   * @return {@code true} if the hitboxes collide, {@code false} otherwise.
   */
  @Override
  public boolean collide(Hitbox hitbox) {
    return this.intersects(hitbox);
  }

  /**
   * Checks if this hitbox collides with a hitcircle.
   *
   * @param hitcircle The hitcircle to check for collision.
   * @return {@code true} if the hitbox collides with the hitcircle, {@code false} otherwise.
   */
  @Override
  public boolean collide(Hitcircle hitcircle) {
    return CollisionUtils.rectCollidesCircle(
        this.absoluteLeft(),
        this.absoluteTop(),
        this.width,
        this.height,
        hitcircle.absolutePosition(),
        hitcircle.radius());
  }
}
