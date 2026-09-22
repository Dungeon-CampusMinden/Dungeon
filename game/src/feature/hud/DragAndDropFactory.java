package feature.hud;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

/** Creates drag-and-drop controllers with defaults or settings for an individual controller. */
public final class DragAndDropFactory {
  private DragAndDropFactory() {}

  /**
   * @return a controller without a minimum drag duration or movement distance
   */
  public static DragAndDrop create() {
    return create(0, 0, false);
  }

  /**
   * @param dragTime minimum drag duration in milliseconds before a drop is accepted
   * @param tapSquareSize half-size of the area in pixels the pointer must leave to start dragging
   * @param keepWithinStage whether the drag preview stays within the stage
   * @return a controller using the supplied settings
   */
  public static DragAndDrop create(int dragTime, float tapSquareSize, boolean keepWithinStage) {
    DragAndDrop dragAndDrop = new DragAndDrop();
    dragAndDrop.setDragTime(dragTime);
    dragAndDrop.setTapSquareSize(tapSquareSize);
    dragAndDrop.setKeepWithinStage(keepWithinStage);
    return dragAndDrop;
  }
}
