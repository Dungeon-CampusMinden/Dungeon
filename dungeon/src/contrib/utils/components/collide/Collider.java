package contrib.utils.components.collide;

import contrib.components.CollideComponent;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * The base class for different collider shapes used in collision detection.
 *
 * <p>Colliders can be attached to entities via a {@link CollideComponent} to enable collision
 * detection with other entities or points in the game world.
 */
public abstract class Collider {

  /** The position of this collider in world space. Only respected by absolute calls. */
  protected Point position = new Point(0, 0);

  /** The scale of this collider in world space. Only respected by absolute calls. */
  protected Vector2 scale = Vector2.ONE;

  /** The offset of this collider relative to its position. */
  protected Vector2 offset = Vector2.ZERO;

  /**
   * Checks if this collider collides with the given entity.
   *
   * @param entity the entity to test collision against
   * @return {@code true} if the collider collides with the entity, otherwise {@code false}
   */
  public boolean collide(Entity entity) {
    return entity
        .fetch(CollideComponent.class)
        .map((cc) -> this.collide(cc.collider()))
        .orElse(false);
  }

  /**
   * Checks if this collider collides with another collider.
   *
   * @param collider the collider to test against
   * @return {@code true} if the colliders intersect, otherwise {@code false}
   * @throws InvalidParameterException if the collider type is not supported
   */
  public boolean collide(Collider collider) {
    if (collider instanceof Hitbox hitbox) {
      return this.collide(hitbox);
    } else if (collider instanceof Hitcircle hitcircle) {
      return this.collide(hitcircle);
    } else {
      throw new InvalidParameterException(
          "Collisions against collider type "
              + collider.getClass().getSimpleName()
              + " are not implemented!");
    }
  }

  /**
   * Checks if this collider contains the given point.
   *
   * @param point the point to test
   * @return {@code true} if the point lies within the collider, otherwise {@code false}
   */
  public abstract boolean collide(Point point);

  /**
   * Checks if this collider collides with a line segment.
   *
   * @param from the start point of the line
   * @param to the end point of the line
   * @return {@code true} if the collider intersects the line, otherwise {@code false}
   */
  public abstract boolean collide(Point from, Point to);

  /**
   * Checks if this collider collides with a hitbox.
   *
   * @param hitbox the hitbox to test against
   * @return {@code true} if a collision occurs, otherwise {@code false}
   */
  public abstract boolean collide(Hitbox hitbox);

  /**
   * Checks if this collider collides with a hitcircle.
   *
   * @param hitcircle the hitcircle to test against
   * @return {@code true} if a collision occurs, otherwise {@code false}
   */
  public abstract boolean collide(Hitcircle hitcircle);

  /**
   * Gets the width of this collider.
   *
   * @return the width
   */
  public abstract float width();

  /**
   * Sets the width of this collider.
   *
   * @param value the new width value
   */
  public abstract void width(float value);

  /**
   * Gets the height of this collider.
   *
   * @return the height
   */
  public abstract float height();

  /**
   * Sets the height of this collider.
   *
   * @param value the new height value
   */
  public abstract void height(float value);

  /**
   * Gets the top coordinate of this collider.
   *
   * @return the top Y coordinate
   */
  public abstract float top();

  /**
   * Sets the top coordinate of this collider.
   *
   * @param value the new top Y coordinate
   */
  public abstract void top(float value);

  /**
   * Gets the bottom coordinate of this collider.
   *
   * @return the bottom Y coordinate
   */
  public abstract float bottom();

  /**
   * Sets the bottom coordinate of this collider.
   *
   * @param value the new bottom Y coordinate
   */
  public abstract void bottom(float value);

  /**
   * Gets the left coordinate of this collider.
   *
   * @return the left X coordinate
   */
  public abstract float left();

  /**
   * Sets the left coordinate of this collider.
   *
   * @param value the new left X coordinate
   */
  public abstract void left(float value);

  /**
   * Gets the right coordinate of this collider.
   *
   * @return the right X coordinate
   */
  public abstract float right();

  /**
   * Sets the right coordinate of this collider.
   *
   * @param value the new right X coordinate
   */
  public abstract void right(float value);

  /** Centers the collider’s origin to its middle point. */
  public void centerOrigin() {
    this.offset =
        Vector2.of((float) (-(double) this.width() / 2.0), (float) (-(double) this.height() / 2.0));
  }

  /**
   * Gets the X coordinate of the collider’s center.
   *
   * @return the center X coordinate
   */
  public float centerX() {
    return this.left() + this.width() / 2f;
  }

  /**
   * Sets the collider’s center X coordinate.
   *
   * @param x the new center X value
   */
  public void centerX(float x) {
    this.left(x - this.width() / 2f);
  }

  /**
   * Gets the Y coordinate of the collider’s center.
   *
   * @return the center Y coordinate
   */
  public float centerY() {
    return this.bottom() + this.height() / 2f;
  }

  /**
   * Sets the collider’s center Y coordinate.
   *
   * @param y the new center Y value
   */
  public void centerY(float y) {
    this.bottom(y - this.height() / 2f);
  }

  /**
   * Gets the top-left corner of the collider.
   *
   * @return a vector representing the top-left corner
   */
  public Vector2 topLeft() {
    return Vector2.of(this.left(), this.top());
  }

  /**
   * Sets the top-left corner of the collider.
   *
   * @param point the new top-left coordinates
   */
  public void topLeft(Vector2 point) {
    this.left(point.x());
    this.top(point.y());
  }

  /**
   * Gets the top-center position of the collider.
   *
   * @return the top-center coordinates
   */
  public Vector2 topCenter() {
    return Vector2.of(this.centerX(), this.top());
  }

  /**
   * Sets the top-center position of the collider.
   *
   * @param point the new top-center coordinates
   */
  public void topCenter(Vector2 point) {
    this.centerX(point.x());
    this.top(point.y());
  }

  /**
   * Gets the top-right corner of the collider.
   *
   * @return a vector representing the top-right corner
   */
  public Vector2 topRight() {
    return Vector2.of(this.right(), this.top());
  }

  /**
   * Sets the top-right corner of the collider.
   *
   * @param point the new top-right coordinates
   */
  public void topRight(Vector2 point) {
    this.right(point.x());
    this.top(point.y());
  }

  /**
   * Gets the center-left position of the collider.
   *
   * @return the center-left coordinates
   */
  public Vector2 centerLeft() {
    return Vector2.of(this.left(), this.centerY());
  }

  /**
   * Sets the center-left position of the collider.
   *
   * @param point the new center-left coordinates
   */
  public void centerLeft(Vector2 point) {
    this.left(point.x());
    this.centerY(point.y());
  }

  /**
   * Gets the center position of the collider.
   *
   * @return the center coordinates
   */
  public Vector2 center() {
    return Vector2.of(this.centerX(), this.centerY());
  }

  /**
   * Sets the center position of the collider.
   *
   * @param point the new center coordinates
   */
  public void center(Vector2 point) {
    this.centerX(point.x());
    this.centerY(point.y());
  }

  /**
   * Gets the full size of the collider.
   *
   * @return a vector containing width and height
   */
  public Vector2 size() {
    return Vector2.of(this.width(), this.height());
  }

  /**
   * Gets half the size of the collider.
   *
   * @return a vector containing half-width and half-height
   */
  public Vector2 halfSize() {
    return Vector2.of(this.width() / 2f, this.height() / 2f);
  }

  /**
   * Gets the center-right position of the collider.
   *
   * @return the center-right coordinates
   */
  public Vector2 centerRight() {
    return Vector2.of(this.right(), this.centerY());
  }

  /**
   * Sets the center-right position of the collider.
   *
   * @param point the new center-right coordinates
   */
  public void centerRight(Vector2 point) {
    this.right(point.x());
    this.centerY(point.y());
  }

  /**
   * Gets the bottom-left corner of the collider.
   *
   * @return a vector representing the bottom-left corner
   */
  public Vector2 bottomLeft() {
    return Vector2.of(this.left(), this.bottom());
  }

  /**
   * Sets the bottom-left corner of the collider.
   *
   * @param point the new bottom-left coordinates
   */
  public void bottomLeft(Vector2 point) {
    this.left(point.x());
    this.bottom(point.y());
  }

  /**
   * Gets the bottom-center position of the collider.
   *
   * @return the bottom-center coordinates
   */
  public Vector2 bottomCenter() {
    return Vector2.of(this.centerX(), this.bottom());
  }

  /**
   * Sets the bottom-center position of the collider.
   *
   * @param point the new bottom-center coordinates
   */
  public void bottomCenter(Vector2 point) {
    this.centerX(point.x());
    this.bottom(point.y());
  }

  /**
   * Gets the bottom-right corner of the collider.
   *
   * @return a vector representing the bottom-right corner
   */
  public Vector2 bottomRight() {
    return Vector2.of(this.right(), this.bottom());
  }

  /**
   * Sets the bottom-right corner of the collider.
   *
   * @param point the new bottom-right coordinates
   */
  public void bottomRight(Vector2 point) {
    this.right(point.x());
    this.bottom(point.y());
  }

  /**
   * Gets the absolute position of the collider.
   *
   * @return the absolute position as a Point
   */
  public Point absolutePosition() {
    return new Point(this.absoluteX(), this.absoluteY());
  }

  /**
   * Gets the absolute X coordinate.
   *
   * @return the absolute X position
   */
  public float absoluteX() {
    return this.position != null
        ? this.position.x() + this.offset.x() * this.scale().x()
        : this.offset.x() * this.scale().x();
  }

  /**
   * Gets the absolute Y coordinate.
   *
   * @return the absolute Y position
   */
  public float absoluteY() {
    return this.position != null
        ? this.position.y() + this.offset.y() * this.scale().y()
        : this.offset.y() * this.scale().y();
  }

  /**
   * Gets the absolute top Y coordinate.
   *
   * @return the absolute top position
   */
  public float absoluteTop() {
    return this.position != null
        ? this.position.y() + this.top() * this.scale().y()
        : this.top() * this.scale().y();
  }

  /**
   * Gets the absolute bottom Y coordinate.
   *
   * @return the absolute bottom position
   */
  public float absoluteBottom() {
    return this.position != null
        ? this.position.y() + this.bottom() * this.scale().y()
        : this.bottom() * this.scale().y();
  }

  /**
   * Gets the absolute left X coordinate.
   *
   * @return the absolute left position
   */
  public float absoluteLeft() {
    return this.position != null
        ? this.position.x() + this.left() * this.scale().x()
        : this.left() * this.scale().x();
  }

  /**
   * Gets the absolute right X coordinate.
   *
   * @return the absolute right position
   */
  public float absoluteRight() {
    return this.position != null
        ? this.position.x() + this.right() * this.scale().x()
        : this.right() * this.scale().x();
  }

  /**
   * Gets the absolute bottom-left point of the collider.
   *
   * @return the bottom-left point in absolute coordinates
   */
  public Point absoluteBottomLeft() {
    return new Point(this.absoluteLeft(), this.absoluteBottom());
  }

  /**
   * Gets the absolute bottom-center point of the collider.
   *
   * @return the bottom-center point in absolute coordinates
   */
  public Point absoluteBottomCenter() {
    return new Point(this.absoluteCenter().x(), this.absoluteBottom());
  }

  /**
   * Gets the absolute bottom-right point of the collider.
   *
   * @return the bottom-right point in absolute coordinates
   */
  public Point absoluteBottomRight() {
    return new Point(this.absoluteRight(), this.absoluteBottom());
  }

  /**
   * Gets the absolute center-left point of the collider.
   *
   * @return the center-left point in absolute coordinates
   */
  public Point absoluteCenterLeft() {
    return new Point(this.absoluteLeft(), this.absoluteCenter().y());
  }

  /**
   * Gets the absolute center point of the collider.
   *
   * @return the center point in absolute coordinates
   */
  public Point absoluteCenter() {
    return new Point(
        this.absoluteX() + (this.width() * this.scale().x()) / 2f,
        this.absoluteY() + (this.height() * this.scale().y()) / 2f);
  }

  /**
   * Gets the absolute center-right point of the collider.
   *
   * @return the center-right point in absolute coordinates
   */
  public Point absoluteCenterRight() {
    return new Point(this.absoluteRight(), this.absoluteCenter().y());
  }

  /**
   * Gets the absolute top-left point of the collider.
   *
   * @return the top-left point in absolute coordinates
   */
  public Point absoluteTopLeft() {
    return new Point(this.absoluteLeft(), this.absoluteTop());
  }

  /**
   * Gets the absolute top-center point of the collider.
   *
   * @return the top-center point in absolute coordinates
   */
  public Point absoluteTopCenter() {
    return new Point(this.absoluteCenter().x(), this.absoluteTop());
  }

  /**
   * Gets the absolute top-right point of the collider.
   *
   * @return the top-right point in absolute coordinates
   */
  public Point absoluteTopRight() {
    return new Point(this.absoluteRight(), this.absoluteTop());
  }

  /**
   * Get the four corners of the collider’s bounds.
   *
   * @return list of all four corners in order: bottom-left, bottom-right, top-left, top-right
   */
  public List<Vector2> corners() {
    return List.of(bottomLeft(), bottomRight(), topLeft(), topRight());
  }

  /**
   * Get the four corners of the collider’s bounds, scaled properly.
   *
   * @return list of all four corners in order: bottom-left, bottom-right, top-left, top-right
   */
  public List<Vector2> cornersScaled() {
    return List.of(
        bottomLeft().scale(this.scale),
        bottomRight().scale(this.scale),
        topLeft().scale(this.scale),
        topRight().scale(this.scale));
  }

  /**
   * Gets the base position of the collider.
   *
   * @return the current position
   */
  public Point position() {
    return this.position;
  }

  /**
   * Sets the base position of the collider.
   *
   * @param position the new position
   */
  public void position(Point position) {
    this.position = position;
  }

  /**
   * Gets the collider’s offset.
   *
   * @return the current offset vector
   */
  public Vector2 offset() {
    return this.offset;
  }

  /**
   * Sets the collider’s offset.
   *
   * @param offset the new offset vector
   */
  public void offset(Vector2 offset) {
    this.offset = offset;
  }

  /**
   * Gets the collider’s scale.
   *
   * @return the current scale vector
   */
  public Vector2 scale() {
    return this.scale;
  }

  /**
   * Sets the collider’s scale.
   *
   * @param scale the new scale vector
   */
  public void scale(Vector2 scale) {
    this.scale = scale;
  }
}
