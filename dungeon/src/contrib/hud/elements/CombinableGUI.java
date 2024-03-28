package contrib.hud.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

/**
 * A GUI element that can be combined with other GUI elements using {@link GUICombination}. Gui
 * Elements that should be displayed together should extend this class.
 *
 * <p>This class is used to create GUI elements that can be combined in one {@link GUICombination}
 * to be displayed together. The {@link GUICombination} will call the methods of this class to draw
 * the element and to calculate the preferred size.
 *
 * <p>Also this class provides a {@link DragAndDrop} object that can be used to drag and drop
 * elements between multiple {@link CombinableGUI CombinableGUIs}.
 *
 * <p>The method {@link CombinableGUI#preferredSize(GUICombination.AvailableSpace)} is called by the
 * parent {@link GUICombination} if the available space for a GUI element changes and the size needs
 * to be recalculated. The method should calculate the preferred size of the element based on the
 * available space and return it as a {@link Vector2}. It should not be greater than the available
 * space.
 */
public abstract class CombinableGUI {

  // Position of the GUI-Element and its size
  private int x, y, width, height;
  // Drag and Drop context object for the GUICombination
  private DragAndDrop dragAndDrop;
  // Actor "dummy". Only used for DragAndDrop (thx GDX <3)
  private Actor actor;

  /**
   * Set the drag and drop object. This should not be called directly as it is called by the parent
   * {@link GUICombination} on initialization.
   *
   * @param dragAndDrop the drag and drop object
   */
  public void dragAndDrop(final DragAndDrop dragAndDrop) {
    this.dragAndDrop = dragAndDrop;
    this.initDragAndDrop(this.dragAndDrop);
  }

  /**
   * Get the drag and drop object.
   *
   * @return the drag and drop object
   */
  public DragAndDrop dragAndDrop() {
    return this.dragAndDrop;
  }

  /**
   * Initialize the drag and drop object.
   *
   * @param dragAndDrop the drag and drop object to initialize
   */
  protected abstract void initDragAndDrop(final DragAndDrop dragAndDrop);

  /**
   * Draw the element.
   *
   * <p>This method should be used for drawing the main part of the element.
   *
   * @param batch the batch to draw to
   */
  protected abstract void draw(final Batch batch);

  /**
   * Draw the top layer of the element.
   *
   * <p>This method should be used for things like hover information that need to be on top of
   * everything else.
   *
   * @param batch the batch to draw to
   */
  protected void drawTopLayer(final Batch batch) {}

  /**
   * Draw debug information for the element.
   *
   * <p>This method should be used for drawing debug information like borders. It will only be
   * called if the parent {@link GUICombination} is in debug mode.
   *
   * <p>The default implementation does nothing.
   */
  protected void drawDebug() {}

  /**
   * Calculate the preferred size of the gui element.
   *
   * <p>The calculation should be based on the available space. The element should not be greater
   * than the available space.
   *
   * @param availableSpace the available space for the element to be drawn in.
   * @return the preferred size of the element.
   */
  protected abstract Vector2 preferredSize(final GUICombination.AvailableSpace availableSpace);

  /** Called when the bounds of the element change. */
  protected void boundsUpdate() {}

  /**
   * Get the x coordinate of the left edge of the element.
   *
   * @return the x coordinate.
   */
  public final int x() {
    return this.x;
  }

  /**
   * Set the x coordinate of the left edge of the element.
   *
   * @param x the x coordinate.
   */
  public final void x(int x) {
    this.actor.setPosition(x, this.y);
    this.x = x;
  }

  /**
   * Get the y coordinate of the top edge of the element.
   *
   * @return the y coordinate.
   */
  public final int y() {
    return this.y;
  }

  /**
   * Set the y coordinate of the top edge of the element.
   *
   * @param y the y coordinate.
   */
  public final void y(int y) {
    this.actor.setPosition(this.x, y);
    this.y = y;
  }

  /**
   * Get the width of the element.
   *
   * @return the width.
   */
  public final int width() {
    return this.width;
  }

  /**
   * Set the width of the element.
   *
   * @param width the width.
   */
  public void width(int width) {
    this.actor.setSize(width, this.height);
    this.width = width;
  }

  /**
   * Get the height of the element.
   *
   * @return the height.
   */
  public final int height() {
    return this.height;
  }

  /**
   * Set the height of the element.
   *
   * @param height the height.
   */
  public void height(int height) {
    this.actor.setSize(this.width, height);
    this.height = height;
  }

  /**
   * Generate a GDX-Actor for the element with the current position and size.
   *
   * <p>This Actor may be used for GDX-functionality like DragAndDrop.
   *
   * @return an Actor for the element.
   */
  protected Actor actor() {
    if (this.actor == null) {
      this.actor = new Actor();
      this.actor.setPosition(this.x, this.y);
      this.actor.setSize(this.width, this.height);
    }
    return this.actor;
  }
}
