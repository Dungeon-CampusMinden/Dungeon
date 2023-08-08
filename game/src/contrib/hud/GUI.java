package contrib.hud;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public abstract class GUI extends WidgetGroup {

    private DragAndDrop dragAndDrop;

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
}
