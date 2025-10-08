package contrib.utils.components.collide;
import core.utils.Point;
import core.utils.Vector2;

public class Hitbox extends Collider {

  private float width;
  private float height;

  public Hitbox(float width, float height)
  {
    this(width, height, 0.0f, 0.0f);
  }

  public Hitbox(float width, float height, float x, float y)
  {
    this.width = width;
    this.height = height;
    this.offset = Vector2.of(x, y);
  }

  @Override
  public float width(){
    return this.width;
  }
  @Override
  public void width(float width){
    this.width = width;
  }

  @Override
  public float height(){
    return this.height;
  }
  @Override
  public void height(float height){
    this.height = height;
  }

  @Override
  public float left(){
    return this.offset.x();
  }
  public void left(float x){
    this.offset = Vector2.of(x, this.offset.y());
  }

  public float top(){
    return this.offset.y();
  }
  public void top(float y){
    this.offset = Vector2.of(this.offset.x(), y);
  }

  public float right(){
    return this.offset.x() + this.width;
  }
  public void right(float x){
    this.offset = Vector2.of(x - this.width, this.offset.y());
  }

  public float bottom(){
    return this.offset.y() + this.height;
  }
  public void bottom(float y){
    this.offset = Vector2.of(this.offset.x(), y - this.height);
  }

  public boolean intersects(Hitbox hitbox)
  {
    return (double) this.absoluteLeft() < (double) hitbox.absoluteRight() && (double) this.absoluteRight() > (double) hitbox.absoluteLeft() && (double) this.absoluteBottom() > (double) hitbox.absoluteTop() && (double) this.absoluteTop() < (double) hitbox.absoluteBottom();
  }

  public boolean intersects(float x, float y, float width, float height)
  {
    return (double) this.absoluteRight() > (double) x && (double) this.absoluteBottom() > (double) y && (double) this.absoluteLeft() < (double) x + (double) width && (double) this.absoluteTop() < (double) y + (double) height;
  }

  @Override
  public Collider clone()
  {
    return (Collider) new Hitbox(this.width, this.height, this.offset.x(), this.offset.y());
  }

//  public void SetFromRectangle(Rectangle rect)
//  {
//    this.Position = new Vector2((float) rect.X, (float) rect.Y);
//    this.width = (float) rect.width;
//    this.height = (float) rect.height;
//  }

//  public void Set(float x, float y, float w, float h)
//  {
//    this.Position = new Vector2(x, y);
//    this.width = w;
//    this.height = h;
//  }


//  public void GetTopEdge(out Vector2 from, out Vector2 to)
//  {
//    from.X = this.absoluteLeft();
//    to.X = this.absoluteRight();
//    from.Y = to.Y = this.absoluteTop();
//  }
//
//  public void GetBottomEdge(out Vector2 from, out Vector2 to)
//  {
//    from.X = this.absoluteLeft();
//    to.X = this.absoluteRight();
//    from.Y = to.Y = this.absoluteBottom();
//  }
//
//  public void GetLeftEdge(out Vector2 from, out Vector2 to)
//  {
//    from.Y = this.absoluteTop();
//    to.Y = this.absoluteBottom();
//    from.X = to.X = this.absoluteLeft();
//  }
//
//  public void GetRightEdge(out Vector2 from, out Vector2 to)
//  {
//    from.Y = this.absoluteTop();
//    to.Y = this.absoluteBottom();
//    from.X = to.X = this.absoluteRight();
//  }

  @Override
  public boolean collide(Point point)
  {
    return CollisionUtils.rectCollidePoint(this.absoluteLeft(), this.absoluteTop(), this.width, this.height, point);
  }

//  @Override
//  public boolean collide(Rectangle rect)
//  {
//    return (double) this.absoluteRight() > (double) rect.Left && (double) this.absoluteBottom() > (double) rect.Top && (double) this.absoluteLeft() < (double) rect.Right && (double) this.absoluteTop() < (double) rect.Bottom;
//  }

  @Override
  public boolean collide(Point from, Point to)
  {
    return CollisionUtils.rectCollideLine(this.absoluteLeft(), this.absoluteTop(), this.width, this.height, from, to);
  }

  @Override
  public boolean collide(Hitbox hitbox){
    return this.intersects(hitbox);
  }

  @Override
  public boolean collide(Hitcircle hitcircle)
  {
    return CollisionUtils.rectCollideCircle(this.absoluteLeft(), this.absoluteTop(), this.width, this.height, hitcircle.absolutePosition(), hitcircle.radius());
  }

//  @Override
//  public boolean collide(colliderList list){
//    return list.Collide(this);
//  }

}
