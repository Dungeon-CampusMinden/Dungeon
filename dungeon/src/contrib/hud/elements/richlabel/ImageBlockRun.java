package contrib.hud.elements.richlabel;

/**
 * A block-level image that occupies its own line and is centered horizontally.
 *
 * @param path the asset path of the image (required)
 * @param widthSpec the width specification: null for full-width, a plain number for pixels, or a
 *     string ending in "%" for a percentage of the available width
 */
public record ImageBlockRun(String path, String widthSpec) implements Run {}
