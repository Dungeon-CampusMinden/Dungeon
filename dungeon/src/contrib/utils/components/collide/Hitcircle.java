package contrib.utils.components.collide;

import core.utils.Point;
import core.utils.Vector2;

public class Hitcircle extends Collider {
  public float radius;

  public Hitcircle(float radius, float x, float y)
  {
    this.radius = radius;
    this.offset = Vector2.of(x, y);
  }
  public Hitcircle(float radius)
  {
    this(radius, 0, 0);
  }

  public float radius(){
    return this.radius;
  }

  @Override
  public float width(){
    return this.radius * 2f;
  }
  @Override
  public void width(float value){
    this.radius = value / 2f;
  }

  @Override
  public float height(){
    return this.radius * 2f;
  }
  @Override
  public void height(float value){
    this.radius = value / 2f;
  }

  @Override
  public float left(){
    return this.offset.x() - this.radius;
  }
  @Override
  public void left(float x){
    this.offset = Vector2.of(x + this.radius, this.offset.y());
  }

  @Override
  public float top(){
    return this.offset.y() - this.radius;
  }
  @Override
  public void top(float y){
    this.offset = Vector2.of(this.offset.x(), y + this.radius);
  }

  @Override
  public float right(){
    return this.offset.x() + this.radius;
  }
  @Override
  public void right(float x){
    this.offset = Vector2.of(x - this.radius, this.offset.y());
  }

  @Override
  public float bottom(){
    return this.offset.y() + this.radius;
  }
  @Override
  public void bottom(float y){
    this.offset = Vector2.of(this.offset.x(), y - this.radius);
  }

  @Override
  public Collider clone()
  {
    return (Collider) new Hitcircle(this.radius, this.position.x(), this.position.y());
  }

  @Override
  public boolean collide(Point point)
  {
    return CollisionUtils.circleCollidePoint(this.absolutePosition(), this.radius, point);
  }

//  public boolean collide(Rectangle rect)
//  {
//    return Monocle.Collide.RectToCircle(rect, this.AbsolutePosition, this.radius);
//  }

  @Override
  public boolean collide(Point from, Point to)
  {
    return CollisionUtils.circleCollideLine(this.absolutePosition(), this.radius, from, to);
  }

  @Override
  public boolean collide(Hitcircle hitcircle)
  {
    double d = this.absolutePosition().distance(hitcircle.absolutePosition());
    return Math.pow(d, 2) < ((double) this.radius + (double) hitcircle.radius) * ((double) this.radius + (double) hitcircle.radius);
  }

  @Override
  public boolean collide(Hitbox hitbox){
    return hitbox.collide(this);
  }

//  @Override
//  public boolean collide(ColliderList list) => list.collide(this);
}
