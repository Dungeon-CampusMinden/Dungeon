package contrib.utils.components.collide;

import contrib.components.CollideComponent;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;

import java.security.InvalidParameterException;

public abstract class Collider {

  protected Point position;
  protected Vector2 offset;

  public boolean collide(Entity entity) {
    return entity.fetch(CollideComponent.class).map((cc) -> this.collide(cc.collider())).orElse(false);
  }

  public boolean collide(Collider collider) {
    if (collider instanceof Hitbox hitbox) {
      return this.collide(hitbox);
    } else if (collider instanceof Hitcircle hitcircle) {
      return this.collide(hitcircle);
    } else {
      throw new InvalidParameterException(
        "Collisions against collider type " + collider.getClass().getSimpleName() + " are not implemented!"
      );
    }
  }

  public abstract boolean collide(Point point);

//  public abstract boolean collide(Rectangle rect);

  public abstract boolean collide(Point from, Point to);

  public abstract boolean collide(Hitbox hitbox);

//  public abstract boolean collide(Grid grid);

  public abstract boolean collide(Hitcircle hitcircle);

//  public abstract boolean collide(ColliderList list);

//  public abstract Collider Clone();

//  public abstract void Render(Camera camera, Color color);

  public abstract float width();
  public abstract void width(float value);

  public abstract float height();
  public abstract void height(float value);

  public abstract float top();
  public abstract void top(float value);

  public abstract float bottom();
  public abstract void bottom(float value);

  public abstract float left();
  public abstract void left(float value);

  public abstract float right();
  public abstract void right(float value);

  public void centerOrigin()
  {
    this.offset = Vector2.of((float) (-(double) this.width() / 2.0), (float) (-(double) this.height() / 2.0));
  }

  public float centerX(){
    return this.left() + this.width() / 2f;
  }
  public void centerX(float x){
    this.left(x - this.width() / 2f);
  }

  public float centerY(){
    return this.top() + this.height() / 2f;
  }
  public void centerY(float y){
    this.top(y - this.height() / 2f);
  }

  public Point topLeft(){
    return new Point(this.left(), this.top());
  }
  public void topLeft(Point point){
    this.left(point.x());
    this.top(point.y());
  }

  public Point topCenter(){
    return new Point(this.centerX(), this.top());
  }
  public void topCenter(Point point){
    this.centerX(point.x());
    this.top(point.y());
  }

  public Point topRight(){
    return new Point(this.right(), this.top());
  }
  public void topRight(Point point){
    this.right(point.x());
    this.top(point.y());
  }

  public Point centerLeft(){
    return new Point(this.left(), this.centerY());
  }
  public void centerLeft(Point point){
    this.left(point.x());
    this.centerY(point.y());
  }

  public Point center(){
    return new Point(this.centerX(), this.centerY());
  }
  public void center(Point point){
    this.centerX(point.x());
    this.centerY(point.y());
  }

  public Vector2 size(){
    return Vector2.of(this.width(), this.height());
  }

  public Vector2 halfSize(){
    return Vector2.of(this.width() / 2f, this.height() / 2f);
  }

  public Point centerRight(){
    return new Point(this.right(), this.centerY());
  }
  public void centerRight(Point point){
    this.right(point.x());
    this.centerY(point.y());
  }

  public Point bottomLeft(){
    return new Point(this.left(), this.bottom());
  }
  public void bottomLeft(Point point){
    this.left(point.x());
    this.bottom(point.y());
  }

  public Point bottomCenter(){
    return new Point(this.centerX(), this.bottom());
  }
  public void bottomCenter(Point point){
    this.centerX(point.x());
    this.bottom(point.y());
  }

  public Point bottomRight(){
    return new Point(this.right(), this.bottom());
  }
  public void bottomRight(Point point){
    this.right(point.x());
    this.bottom(point.y());
  }

//  public void Render(Camera camera) => this.Render(camera, Color.Red);

  public Point absolutePosition()
  {
    return new Point(this.absoluteX(), this.absoluteY());
  }

  public float absoluteX(){
    return this.position != null ? this.position.x() + this.offset.x() : this.offset.x();
  }

  public float absoluteY(){
    return this.position != null ? this.position.y() + this.offset.y() : this.offset.y();
  }

  public float absoluteTop(){
    return this.position != null ? this.position.y() + this.top() : this.top();
  }

  public float absoluteBottom(){
    return this.position != null ? this.position.y() + this.bottom() : this.bottom();
  }

  public float absoluteLeft(){
    return this.position != null ? this.position.x() + this.left() : this.left();
  }

  public float absoluteRight(){
    return this.position != null ? this.position.x() + this.right() : this.right();
  }

//  public Rectangle Bounds
//  {
//    get
//    {
//      return new Rectangle((int) this.AbsoluteLeft, (int) this.AbsoluteTop, (int) this.Width, (int) this.Height);
//    }
//  }
}
