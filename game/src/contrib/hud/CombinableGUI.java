package contrib.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

/** A GUI element that can be combined with other GUI elements. */
public abstract class CombinableGUI {

    private int x, y, width, height;
    private DragAndDrop dragAndDrop;
    private Actor actor; // Actor "dummy". Only used for DragAndDrop (thx GDX)

    /**
     * Set the drag and drop object
     *
     * @param dragAndDrop the drag and drop object
     */
    public void dragAndDrop(DragAndDrop dragAndDrop) {
        this.dragAndDrop = dragAndDrop;
        this.initDragAndDrop(this.dragAndDrop);
    }

    /**
     * Get the drag and drop object
     *
     * @return the drag and drop object
     */
    public DragAndDrop dragAndDrop() {
        return this.dragAndDrop;
    }

    /**
     * Initialize the drag and drop object
     *
     * @param dragAndDrop the drag and drop object to initialize
     */
    protected abstract void initDragAndDrop(DragAndDrop dragAndDrop);

    /**
     * Draw the element
     *
     * <p>This method should be used for drawing the main part of the element.
     *
     * @param batch the batch to draw to
     */
    protected abstract void draw(Batch batch);

    /**
     * Draw the top layer of the element
     *
     * <p>This method should be used for things like hover information that need to be on top of
     * everything else.
     *
     * @param batch the batch to draw to
     */
    protected void drawTopLayer(Batch batch) {}

    protected void drawDebug(Batch batch) {}

    /**
     * Calculate the preferred size of the gui element.
     *
     * <p>The calculation should be based on the available space. The element should not be greater
     * than the available space.
     *
     * @param availableSpace the available space for the element to be drawn in.
     * @return the preferred size of the element.
     */
    protected abstract Vector2 preferredSize(GUICombination.AvailableSpace availableSpace);

    /**
     * Get the x coordinate of the left edge of the element
     *
     * @return the x coordinate.
     */
    public final int x() {
        return this.x;
    }

    /**
     * Set the x coordinate of the left edge of the element
     *
     * @param x the x coordinate.
     */
    public final void x(int x) {
        this.actor.setPosition(x, this.y);
        this.x = x;
    }

    /**
     * Get the y coordinate of the top edge of the element
     *
     * @return the y coordinate.
     */
    public final int y() {
        return this.y;
    }

    /**
     * Set the y coordinate of the top edge of the element
     *
     * @param y the y coordinate.
     */
    public final void y(int y) {
        this.actor.setPosition(this.x, y);
        this.y = y;
    }

    /**
     * Get the width of the element
     *
     * @return the width.
     */
    public final int width() {
        return this.width;
    }

    /**
     * Set the width of the element
     *
     * @param width the width.
     */
    public final void width(int width) {
        this.actor.setSize(width, this.height);
        this.width = width;
    }

    /**
     * Get the height of the element
     *
     * @return the height.
     */
    public final int height() {
        return this.height;
    }

    /**
     * Set the height of the element
     *
     * @param height the height.
     */
    public final void height(int height) {
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
