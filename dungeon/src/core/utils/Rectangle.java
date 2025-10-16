package core.utils;

public record Rectangle(float width, float height, float x, float y) {

  public Rectangle(Vector2 size, Vector2 offset) {
    this(offset.x(), offset.y(), size.x(), size.y());
  }

  public Rectangle(float width, float height) {
    this(width, height, 0, 0);
  }

  public Rectangle(Vector2 size) {
    this(size.x(), size.y());
  }

  public Vector2 size() {
    return Vector2.of(width, height);
  }

  public Vector2 offset() {
    return Vector2.of(x, y);
  }
}
