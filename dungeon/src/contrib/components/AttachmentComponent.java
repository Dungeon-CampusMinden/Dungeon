package contrib.components;

import contrib.systems.AttachmentSystem;
import core.Component;
import core.components.PositionComponent;
import core.utils.Vector2;

public class AttachmentComponent implements Component {

  private Vector2 offset;
  private boolean isRotatingWithOrigin = false;

  public AttachmentComponent(PositionComponent copy, PositionComponent origin) {
    this(Vector2.ZERO, copy, origin);
  }

  public AttachmentComponent(Vector2 offset, PositionComponent copy, PositionComponent origin) {
    this.offset = offset;
    AttachmentSystem.registerAttachment(copy, origin);
  }

  public Vector2 getOffset() {
    return offset;
  }

  public void setOffset(Vector2 offset) {
    this.offset = offset;
  }

  public boolean isRotatingWithOrigin() {
    return isRotatingWithOrigin;
  }

  public void setRotatingWithOrigin(boolean isRotatingWithOrigin) {
    this.isRotatingWithOrigin = isRotatingWithOrigin;
  }
}
