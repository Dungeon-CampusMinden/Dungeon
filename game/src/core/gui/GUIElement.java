package core.gui;

import core.gui.backend.BackendImage;
import core.utils.math.Vector3f;
import core.utils.math.Vector4f;

public abstract class GUIElement {

    protected Vector3f position;
    protected Vector3f size;
    protected Vector3f rotation;
    protected GUIElement parent;
    protected LayoutHint layoutHint;
    protected Vector4f backgroundColor;
    protected BackendImage backgroundImage;
    protected boolean valid = false;

    public GUIElement() {
        this.position = new Vector3f(0, 0, 0);
        this.size = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
    }

    public GUIElement(Vector3f position, Vector3f size, Vector3f rotation) {
        this.position = position;
        this.size = size;
        this.rotation = rotation;
    }

    /**
     * Get the position vector
     *
     * @return Vector
     */
    public final Vector3f position() {
        return this.position;
    }

    /**
     * Get the size vector
     *
     * @return Vector
     */
    public final Vector3f size() {
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
    public final void position(Vector3f position) {
        this.position = position;
    }

    /**
     * Set the size vector
     *
     * @param size Vector
     */
    public final void size(Vector3f size) {
        this.size = size;
    }

    /**
     * Set the rotation vector
     *
     * @param rotation Vector
     */
    public final void rotation(Vector3f rotation) {
        this.rotation = rotation;
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
    public final LayoutHint layoutHint() {
        return this.layoutHint;
    }

    /**
     * Set the current layout hint
     *
     * @param hint LayoutHint
     */
    public final void layoutHint(LayoutHint hint) {
        this.layoutHint = hint;
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
     * This method is called when there is an GUIEvent for this element.
     *
     * @param event GUIEvent
     */
    public void event(GUIEvent event) {}
}
