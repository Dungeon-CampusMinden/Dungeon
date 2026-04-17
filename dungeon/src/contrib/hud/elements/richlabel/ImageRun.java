package contrib.hud.elements.richlabel;

/**
 * An inline image reference with optional shake effect.
 *
 * @param path the asset path of the image
 * @param shake the shake effect parameters, or null for no shake
 */
public record ImageRun(String path, ShakeEffect shake) implements Run {}
