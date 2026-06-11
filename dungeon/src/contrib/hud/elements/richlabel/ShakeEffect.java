package contrib.hud.elements.richlabel;

/**
 * Parameters for the shake text effect. Values are absolute (already multiplied from user input).
 *
 * @param strength the maximum pixel offset per frame
 * @param speed the speed multiplier for the shake frequency
 * @param phase phase offset so different shake blocks don't move in unison
 */
public record ShakeEffect(float strength, float speed, float phase) {}
