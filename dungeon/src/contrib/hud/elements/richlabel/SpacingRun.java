package contrib.hud.elements.richlabel;

/**
 * A layout-only run that adjusts spacing for all subsequent runs. Does not produce any visible
 * output itself.
 *
 * @param wordSpaceMultiplier multiplier for word spacing (1.0 = default), or -1 if unchanged
 * @param lineSpaceMultiplier multiplier for line spacing (1.0 = default), or -1 if unchanged
 */
public record SpacingRun(float wordSpaceMultiplier, float lineSpaceMultiplier) implements Run {}
