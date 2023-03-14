package level.generator.perlinNoise;

import level.tools.Coordinate;

/**
 * This class serves as a configuration class for the {@link NoiseArea} class.
 *
 * <p>Several constructors are provided.
 */
public class NoiseAreaValues {
    final double min;
    final double max;
    final double[][] noiseValues;
    final boolean outerBound;
    final Coordinate startField;

    /**
     * @param min lowerBound
     * @param max upperBound
     * @param noiseValues noise
     * @param outerBound flag -> determines if the areas will be inside or outside the bound
     */
    public NoiseAreaValues(
            final double min,
            final double max,
            final double[][] noiseValues,
            final boolean outerBound) {
        this(min, max, noiseValues, null, outerBound);
    }

    /**
     * @param min lowerBound
     * @param max upperBound
     * @param noiseValues noise
     * @param startField coordinate to start at
     * @param outerBound flag -> determines if the areas will be inside or outside the bound
     */
    public NoiseAreaValues(
            final double min,
            final double max,
            final double[][] noiseValues,
            final Coordinate startField,
            final boolean outerBound) {
        this.min = min;
        this.max = max;
        this.noiseValues = noiseValues;
        this.outerBound = outerBound;
        this.startField = startField;
    }
}
