package core.gui;

import core.gui.backend.BackendImage;
import core.gui.math.Vector2f;
import core.gui.math.Vector2i;
import core.gui.math.Vector3f;
import core.gui.math.Vector4f;

public abstract class GUIElement {

    protected Vector2f position;
    protected Vector2f size;
    protected Vector3f rotation;
    protected GUIContainer parent;
    protected ILayoutHint layoutHint;
    protected Vector4f backgroundColor;
    protected BackendImage backgroundImage;
    protected boolean valid = false;
    protected Vector2f minimalSize;

    public GUIElement() {
        this.position = new Vector2f(0, 0);
        this.size = new Vector2f(0, 0);
        this.minimalSize = new Vector2f(0, 0);
        this.rotation = new Vector3f(0, 0, 0);
    }

    public GUIElement(Vector2f position, Vector2f size, Vector2f minimalSize, Vector3f rotation) {
        this.position = position;
        this.size = size;
        this.rotation = rotation;
        this.minimalSize = minimalSize;
    }

    /**
     * Get the minimal size of the element.
     *
     * @return {@link Vector2f} representing the minimal size.
     */
    public Vector2f minimalSize() {
        return this.minimalSize;
    }

    /**
     * Get the preferred size of the element.
     *
     * @return {@link Vector2f} representing the preferred size.
     */
    public abstract Vector2f preferredSize();

    /**
     * Get the position vector
     *
     * @return Vector
     */
    public final Vector2f position() {
        return this.position;
    }

    /**
     * Get the absolute position vector
     *
     * @return Vector
     */
    public final Vector2f absolutePosition() {
        if (this.parent == null) return this.position;
        return this.parent.absolutePosition().copy().add(this.position);
    }

    /**
     * Get the size vector
     *
     * @return Vector
     */
    public final Vector2f size() {
        return this.size;
    }

    /**
     * Get the rotation vector
     *
     * @return Vector
     */
    public final Vector3f rotation() {
        return this.rotation;
    }

    /**
     * Set the position vector
     *
     * @param position Vector
     */
    public void position(Vector2f position) {
        this.position = position;
        this.invalidate();
    }

    /**
     * Set the size vector
     *
     * @param size Vector
     */
    public void size(Vector2f size) {
        this.size = size;
        this.invalidate();
    }

    /**
     * Set the minimal size.
     *
     * @param minimalSize Vector2f
     */
    public final void minimalSize(Vector2f minimalSize) {
        this.minimalSize = minimalSize;
        this.invalidate();
    }

    /**
     * Set the rotation vector
     *
     * @param rotation Vector
     */
    public final void rotation(Vector3f rotation) {
        this.rotation = rotation;
        this.invalidate();
    }

    /**
     * Get the parent element
     *
     * @return GUIElement
     */
    public final GUIElement parent() {
        return this.parent;
    }

    /**
     * Get the current layout hint
     *
     * @return LayoutHint
     */
    public final ILayoutHint layoutHint() {
        return this.layoutHint;
    }

    /**
     * Set the current layout hint
     *
     * @param hint LayoutHint
     */
    public final GUIElement layoutHint(ILayoutHint hint) {
        this.layoutHint = hint;
        this.invalidate();
        return this;
    }

    /**
     * Get the background color
     *
     * @return {@link Vector4f}
     */
    public Vector4f backgroundColor() {
        return backgroundColor;
    }

    /**
     * Set the background color
     *
     * @param backgroundColor {@link Vector4f}
     * @return Self {@link GUIElement}
     */
    public GUIElement backgroundColor(Vector4f backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.invalidate();
        return this;
    }

    /**
     * Get the background image
     *
     * @return {@link BackendImage}
     */
    public BackendImage backgroundImage() {
        return backgroundImage;
    }

    /**
     * Get if this element is valid or if it has to be redrawn/updated.
     *
     * @return true if valid otherwise false.
     */
    public boolean valid() {
        return valid;
    }

    /**
     * This method should be called when something about the element changes (e.g. size change) that
     * requires a redraw.
     */
    public final void invalidate() {
        this.valid = false;
        if (this.parent != null) this.parent.invalidate();
    }

    /**
     * Packs the element to its preferred or minimum size
     *
     * <p>The default behavior is to do nothing.
     */
    public void pack() {}

    /**
     * This method is called when the element receives an event.
     *
     * @param event GUIEvent
     */
    public void event(GUIEvent event) {}

    /**
     * Check whether the given point is inside the element.
     *
     * @param point Point to check
     * @return true if the point is inside the element otherwise false.
     */
    protected boolean isPointIn(Vector2i point) {
        Vector2i absolutePosition = this.absolutePosition().toInt();
        Vector2i absoluteSize = this.size.toInt();
        return point.x() >= absolutePosition.x()
                && point.x() <= absolutePosition.x() + absoluteSize.x()
                && point.y() >= absolutePosition.y()
                && point.y() <= absolutePosition.y() + absoluteSize.y();
    }

    /** This method is called when the element should be updated. */
    public void update() {}
}
