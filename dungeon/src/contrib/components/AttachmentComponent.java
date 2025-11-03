package contrib.components;

import contrib.systems.AttachmentSystem;
import core.Component;
import core.components.PositionComponent;
import core.utils.Vector2;

/** This component attaches two entities where the origin provides the position for the copy. */
public class AttachmentComponent implements Component {

  private Vector2 offset;
  private boolean isRotatingWithOrigin = false;
  private float scale = 1f;

  /**
   * Creates a new AttachmentComponent.
   *
   * <p>If the offset is Vector2.ZERO the copied position will be placed in front of the origin
   * position which can be scaled with the scale parameter. It automatically registers to the {@link
   * AttachmentSystem}.
   *
   * @param offset offset from the origins position.
   * @param copy the PositionComponent that gets updated.
   * @param origin the PositionComponent that provides the position for the copy.
   */
  public AttachmentComponent(Vector2 offset, PositionComponent copy, PositionComponent origin) {
    this.offset = offset;
    if (offset == Vector2.ZERO) {
      this.isRotatingWithOrigin = true;
    }
    AttachmentSystem.registerAttachment(copy, origin);
  }

  /**
   * Returns the offset of the attachment.
   *
   * @return offset of the attachment.
   */
  public Vector2 getOffset() {
    return offset;
  }

  /**
   * Sets the offset of the attachment.
   *
   * @param offset new offset of the attachment.
   */
  public void setOffset(Vector2 offset) {
    this.offset = offset;
  }

  /**
   * Returns if the copied position is rotating with the origin.
   *
   * @return true if the copy is rotating with the origin, otherwise false.
   */
  public boolean isRotatingWithOrigin() {
    return isRotatingWithOrigin;
  }

  /**
   * Sets if the copied position is rotating with the origin.
   *
   * @param isRotatingWithOrigin true if the copy is rotating with the origin, otherwise false.
   */
  public void setRotatingWithOrigin(boolean isRotatingWithOrigin) {
    this.isRotatingWithOrigin = isRotatingWithOrigin;
  }

  /**
   * Returns the scale for distances between the copy and origin.
   *
   * @return the scale of the distance between the copy and origin.
   */
  public float getScale() {
    return scale;
  }

  /**
   * Updates the scale for the distance between the copy and origin.
   *
   * @param scale new scale for the distance between the copy and origin.
   */
  public void setScale(float scale) {
    this.scale = scale;
  }
}
