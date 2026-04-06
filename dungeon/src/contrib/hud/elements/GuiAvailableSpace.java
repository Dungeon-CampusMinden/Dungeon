package contrib.hud.elements;

/**
 * Backend-neutral description of the layout space available to a combined HUD widget.
 *
 * @param x left edge of the available area
 * @param y bottom edge of the available area
 * @param width width of the available area
 * @param height height of the available area
 */
public record GuiAvailableSpace(int x, int y, int width, int height) {}
